package com.techquantum.promptvault

object SmartSearchEngine {

    data class Match(val prompt: PromptEntity, val score: Int)

    fun rank(prompts: List<PromptEntity>, query: String): List<PromptEntity> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return prompts
        val queryTokens = expandTokens(tokenize(normalizedQuery))
        val queryPhrases = expandPhrases(normalizedQuery)
        return prompts.mapNotNull { prompt ->
            val score = score(prompt, normalizedQuery, queryTokens, queryPhrases)
            if (score > 0) Match(prompt, score) else null
        }.sortedWith(compareByDescending<Match> { it.score }.thenByDescending { it.prompt.id })
            .map { it.prompt }
    }

    private fun score(prompt: PromptEntity, query: String, queryTokens: Set<String>, queryPhrases: Set<String>): Int {
        val title = normalize(prompt.title)
        val category = normalize(prompt.category).replace('.', ' ').replace('-', ' ')
        val tags = normalize(prompt.tags)
        val text = normalize(prompt.text)
        var score = 0
        if (title == query) score += 120
        if (title.contains(query)) score += 80
        if (category.contains(query)) score += 70
        if (tags.contains(query)) score += 60
        if (text.contains(query)) score += 45
        for (token in queryTokens) when {
            title.contains(token) -> score += 28
            category.contains(token) -> score += 24
            tags.contains(token) -> score += 20
            text.contains(token) -> score += 10
        }
        for (phrase in queryPhrases) {
            if (category.contains(phrase)) score += 45
            if (tags.contains(phrase)) score += 35
            if (title.contains(phrase)) score += 30
            if (text.contains(phrase)) score += 15
        }
        for (token in queryTokens.filter { it.length >= 4 }) {
            if (similar(token, title) || similar(token, category) || similar(token, tags)) score += 8
        }
        return score
    }

    private fun similar(token: String, field: String): Boolean =
        field.split(WS_REGEX).any { word ->
            levenshtein(token, word) <= when {
                token.length >= 8 -> 2
                token.length >= 5 -> 1
                else -> 0
            }
        }

    private fun expandTokens(tokens: List<String>): Set<String> {
        val result = tokens.toMutableSet()
        tokens.forEach { token -> SYNONYMS[token]?.let(result::addAll) }
        return result
    }

    private fun expandPhrases(query: String): Set<String> {
        val phrases = mutableSetOf(query)
        CATEGORY_ALIASES.forEach { (alias, category) ->
            if (query.contains(alias)) {
                val name = normalize(category)
                phrases += name
                CATEGORY_DOMAIN[name]?.let(phrases::add)
            }
        }
        return phrases
    }

    private fun tokenize(value: String): List<String> =
        value.split(NON_WORD_REGEX).map { it.trim() }.filter { it.length >= 2 }

    fun normalize(value: String): String =
        value.lowercase()
            .replace('ي', 'ی').replace('ى', 'ی').replace('ك', 'ک')
            .replace('ۀ', 'ه').replace('ة', 'ه')
            .replace(WS_REGEX, " ").trim()

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        var previous = IntArray(b.length + 1) { it }
        var current = IntArray(b.length + 1)
        for (i in a.indices) {
            current[0] = i + 1
            for (j in b.indices) {
                val cost = if (a[i] == b[j]) 0 else 1
                current[j + 1] = minOf(current[j] + 1, previous[j + 1] + 1, previous[j] + cost)
            }
            val temp = previous; previous = current; current = temp
        }
        return previous[b.length]
    }

    private val WS_REGEX = Regex("\\s+")
    private val NON_WORD_REGEX = Regex("[^\\p{L}\\p{N}]+")

    // Library categories ("Programming") and catalog domains ("coding.debugging") use different names.
    private val CATEGORY_DOMAIN = mapOf(
        "programming" to "coding",
        "design & ux" to "ui",
        "music & sound" to "audio"
    )

    private val CATEGORY_ALIASES = mapOf(
        "تصویر" to "Image", "عکس" to "Image", "عکاسی" to "Image", "image" to "Image", "photo" to "Image",
        "ویدیو" to "Video", "ویدئو" to "Video", "فیلم" to "Video", "video" to "Video",
        "صدا" to "Audio", "موسیقی" to "Music & Sound",
        "برنامه نویسی" to "Programming", "برنامه‌نویسی" to "Programming", "کدنویسی" to "Programming",
        "کد" to "Programming", "programming" to "Programming", "code" to "Programming",
        "امنیت" to "Security", "security" to "Security",
        "بازاریابی" to "Marketing", "marketing" to "Marketing", "سئو" to "SEO", "seo" to "SEO",
        "رزومه" to "Career & Resume", "شغل" to "Career & Resume", "کاریابی" to "Career & Resume",
        "education" to "Education", "آموزش" to "Education",
        "تحقیق" to "Research", "پژوهش" to "Research", "research" to "Research",
        "طراحی" to "Design & UX", "دیزاین" to "Design & UX", "design" to "Design & UX",
        "کسب و کار" to "Business", "کسب‌وکار" to "Business", "business" to "Business",
        "سفر" to "Travel", "travel" to "Travel"
    )

    private val SYNONYMS = mapOf(
        "عکس" to setOf("تصویر", "image", "photo"), "تصویر" to setOf("عکس", "image", "photo"),
        "ساخت" to setOf("تولید", "ایجاد", "generate", "create"),
        "تولید" to setOf("ساخت", "ایجاد", "generate", "create"),
        "ایجاد" to setOf("ساخت", "تولید", "generate", "create"),
        "کد" to setOf("برنامه", "برنامه نویسی", "code", "programming"),
        "برنامه" to setOf("کد", "programming", "code"),
        "ویدیو" to setOf("ویدئو", "فیلم", "video"), "ویدئو" to setOf("ویدیو", "فیلم", "video"),
        "فیلم" to setOf("ویدیو", "ویدئو", "video"), "محتوا" to setOf("content"),
        "نویسندگی" to setOf("نوشتن", "writing"), "نوشتن" to setOf("نویسندگی", "writing"),
        "بازاریابی" to setOf("مارکتینگ", "marketing"), "مارکتینگ" to setOf("بازاریابی", "marketing")
    )
}
