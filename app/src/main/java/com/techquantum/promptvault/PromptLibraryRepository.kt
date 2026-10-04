package com.techquantum.promptvault

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class RemotePrompt(
    val remoteKey: String,
    val title: String,
    val text: String,
    val category: String,
    val tags: String,
    val source: String,
    val license: String
)

object PromptLibraryRepository {
    private const val CSV_URL = "https://raw.githubusercontent.com/f/prompts.chat/main/prompts.csv"
    const val sourceName = "prompts.chat"
    const val licenseName = "CC0 1.0"

    fun fetch(): List<RemotePrompt> {
        val connection = (URL(CSV_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 20_000
            readTimeout = 60_000
            setRequestProperty("User-Agent", "PromptVault/1.0")
            setRequestProperty("Accept", "text/csv")
        }
        try {
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("HTTP " + connection.responseCode)
            }
            val csv = BufferedReader(
                InputStreamReader(connection.inputStream, Charsets.UTF_8)
            ).use { it.readText() }
            return parseCsv(csv)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseCsv(csv: String): List<RemotePrompt> {
        val rows = parseRows(csv)
        if (rows.isEmpty()) return emptyList()
        val header = rows.first().map { it.trim().removePrefix("\uFEFF").lowercase() }
        val actIndex = header.indexOf("act")
        val promptIndex = header.indexOf("prompt")
        if (actIndex < 0 || promptIndex < 0) {
            throw IllegalStateException("Unsupported CSV format")
        }

        return rows.drop(1).mapNotNull { row ->
            val title = row.getOrNull(actIndex)?.trim().orEmpty()
            val text = row.getOrNull(promptIndex)?.trim().orEmpty()
            if (title.isBlank() || text.isBlank()) return@mapNotNull null

            val remoteKey = sha256(title + "\n" + text)
            RemotePrompt(
                remoteKey = remoteKey,
                title = title,
                text = text,
                category = categoryFor(title, text),
                tags = tagsFor(title, text),
                source = sourceName,
                license = licenseName
            )
        }.distinctBy { it.remoteKey }
    }

    private fun parseRows(csv: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var i = 0

        fun finishField() {
            row += field.toString()
            field.setLength(0)
        }

        fun finishRow() {
            finishField()
            if (row.any { it.isNotBlank() }) rows += row.toList()
            row.clear()
        }

        while (i < csv.length) {
            val c = csv[i]
            when {
                c == '"' && quoted && i + 1 < csv.length && csv[i + 1] == '"' -> {
                    field.append('"')
                    i++
                }
                c == '"' -> quoted = !quoted
                c == ',' && !quoted -> finishField()
                (c == '\n' || c == '\r') && !quoted -> {
                    if (c == '\r' && i + 1 < csv.length && csv[i + 1] == '\n') i++
                    finishRow()
                }
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) finishRow()
        return rows
    }

    private val categoryRules = listOf(
            "Programming" to listOf("code", "coding", "developer", "programming", "python", "javascript", "java ", "kotlin", "sql", "react", "api"),
            "DevOps & Cloud" to listOf("devops", "docker", "kubernetes", "terraform", "aws", "azure", "cloud", "linux terminal"),
            "Security" to listOf("security", "cyber", "penetration", "vulnerability", "malware", "encryption"),
            "Data & Analytics" to listOf("data analyst", "data analysis", "analytics", "excel", "spreadsheet", "statistics", "sql terminal"),
            "Research" to listOf("research", "researcher", "literature review", "fact check", "academic"),
            "Education" to listOf("teacher", "tutor", "student", "instructor", "lesson", "learning", "professor"),
            "Writing" to listOf("writer", "writing", "editor", "proofreader", "essay", "copywriter", "literary"),
            "Translation & Language" to listOf("translator", "translation", "language", "pronunciation", "grammar", "vocabulary"),
            "Marketing" to listOf("marketing", "advertiser", "advertising", "campaign", "brand"),
            "SEO" to listOf("seo", "keyword", "search engine"),
            "Sales" to listOf("sales", "selling", "salesperson", "cold outreach"),
            "Customer Support" to listOf("customer support", "customer service", "support agent", "help desk"),
            "Career & Resume" to listOf("career", "resume", "cv", "interview", "job seeker", "recruiter"),
            "Finance" to listOf("finance", "financial", "accountant", "investment", "stock", "budget"),
            "Legal" to listOf("legal", "lawyer", "attorney", "contract", "compliance"),
            "Health & Wellness" to listOf("doctor", "medical", "nutrition", "dietitian", "fitness", "wellness", "psychologist"),
            "Travel" to listOf("travel", "trip", "tourist", "ticket advisor", "itinerary"),
            "Creative & Brainstorming" to listOf("creative", "brainstorm", "idea generator", "story", "storyteller"),
            "Social Media" to listOf("social media", "instagram", "twitter", "linkedin", "tiktok"),
            "Design & UX" to listOf("ux", "ui", "designer", "design"),
            "3D & Game" to listOf("game", "unity", "unreal", "3d"),
            "Music & Sound" to listOf("music", "song", "composer", "sound", "audio"),
            "Image" to listOf("image", "photo", "photography", "artist", "illustration", "midjourney", "stable diffusion"),
            "Video" to listOf("video", "filmmaker", "screenwriter", "cinema"),
            "Audio" to listOf("audio", "podcast", "voice", "speech"),
            "Problem Solving" to listOf("problem solving", "troubleshoot", "debugger", "diagnose"),
            "Strategy" to listOf("strategy", "strategist", "business strategy", "okrs", "swot"),
            "Business" to listOf("business", "entrepreneur", "manager", "consultant", "ceo"),
            "Productivity" to listOf("productivity", "planner", "organizer", "time management"),
            "Documentation" to listOf("documentation", "technical writer", "readme"),
            "Testing & QA" to listOf("tester", "testing", "qa", "quality assurance"),
            "Automation" to listOf("automation", "workflow", "automate"),
            "Content" to listOf("content creator", "content", "blog", "newsletter"),
            "E-commerce" to listOf("ecommerce", "e-commerce", "shopify", "product listing"),
            "Branding" to listOf("branding", "brand identity", "logo")
    )

    // Whole-word matching (plural "s"/"es" allowed). Plain substring matching made "ui" match
    // "build"/"guide" and "api" match "capital"/"rapid".
    private fun wordRegex(keys: List<String>): Regex =
        Regex("(?<![a-z0-9])(?:" + keys.joinToString("|") { Regex.escape(it.trim()) } + ")(?:s|es)?(?![a-z0-9])")

    private val categoryMatchers: List<Pair<String, Regex>> by lazy {
        categoryRules.filter { it.second.isNotEmpty() }.map { (name, keys) -> name to wordRegex(keys) }
    }

    internal fun categoryFor(title: String, text: String): String {
        val s = (title + " " + text).lowercase()
        return categoryMatchers.firstOrNull { (_, regex) -> regex.containsMatchIn(s) }?.first ?: "General"
    }

    private val tagKeywords = listOf(
        "ai", "chatgpt", "claude", "gemini", "coding", "python", "javascript",
        "sql", "marketing", "seo", "writing", "research", "education",
        "business", "finance", "design", "image", "video", "audio",
        "productivity", "automation", "career", "travel", "health", "security"
    )

    private val tagMatchers: List<Pair<String, Regex>> by lazy {
        tagKeywords.map { it to wordRegex(listOf(it)) }
    }

    internal fun tagsFor(title: String, text: String): String {
        val s = (title + " " + text).lowercase()
        return tagMatchers.filter { (_, regex) -> regex.containsMatchIn(s) }
            .map { it.first }.take(6).joinToString(", ")
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}