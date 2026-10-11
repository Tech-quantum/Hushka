package com.techquantum.hushka

import android.content.Context
import org.json.JSONObject

/**
 * تشخیص تعریف نقش در پرامپت و پیشنهاد نقش مناسب.
 *
 * قوانین از فایل assets/analysis_rules.json خوانده می‌شود، پس با ویرایش همان فایل
 * (بدون دست‌زدن به کد) می‌توان الگوها و کلیدواژه‌ها را تغییر داد.
 * اگر فایل در دسترس نباشد، الگوهای پیش‌فرض داخل کد استفاده می‌شود.
 */
object RoleSuggester {

    private val DEFAULT_PATTERNS = listOf(
        "you are", "act as", "your role", "تو یک", "شما یک", "أنت ", "sen bir"
    )

    private data class Rules(
        val patterns: List<String>,
        val roles: Map<String, List<String>>
    )

    @Volatile
    private var rules: Rules = Rules(DEFAULT_PATTERNS, emptyMap())

    /** یک بار در شروع برنامه صدا زده می‌شود. */
    fun init(context: Context) {
        rules = try {
            val json = context.applicationContext.assets
                .open("analysis_rules.json")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val root = JSONObject(json)

            val patternsArray = root.optJSONArray("role_patterns")
            val patterns = if (patternsArray != null) {
                List(patternsArray.length()) { patternsArray.getString(it).lowercase() }
            } else {
                DEFAULT_PATTERNS
            }

            val roleMap = linkedMapOf<String, List<String>>()
            val rolesArray = root.optJSONArray("roles")
            if (rolesArray != null) {
                for (i in 0 until rolesArray.length()) {
                    val obj = rolesArray.getJSONObject(i)
                    val kw = obj.optJSONArray("keywords")
                    roleMap[obj.getString("id")] = if (kw != null) {
                        List(kw.length()) { kw.getString(it).lowercase() }
                    } else {
                        emptyList()
                    }
                }
            }
            Rules(patterns.ifEmpty { DEFAULT_PATTERNS }, roleMap)
        } catch (e: Exception) {
            Rules(DEFAULT_PATTERNS, emptyMap())
        }
    }

    /** آیا متن پرامپت از قبل نقشی تعریف کرده است؟ (فارسی، انگلیسی، عربی، ترکی) */
    fun hasRoleDefinition(text: String): Boolean {
        val lower = text.lowercase()
        return rules.patterns.any { lower.contains(it) }
    }

    /** مناسب‌ترین نقش را از روی عنوان، متن، برچسب‌ها و دسته‌بندی پیدا می‌کند. */
    fun suggest(prompt: PromptEntity): RoleTemplate? {
        val haystack = "${prompt.title} ${prompt.text} ${prompt.tags} ${prompt.category}".lowercase()

        var bestId: String? = null
        var bestScore = 0
        rules.roles.forEach { (id, keywords) ->
            val score = keywords.count { matches(haystack, it) }
            if (score > bestScore) {
                bestScore = score
                bestId = id
            }
        }

        val id = bestId
            ?: RoleTemplates.suggestForCategory(prompt.category).takeIf { it != "custom" }

        return id
            ?.let { RoleTemplates.findById(it) }
            ?.takeIf { it.promptPrefix.isNotBlank() }
    }

    /** نقش را به ابتدای پرامپت اضافه می‌کند. */
    fun applyRole(text: String, role: RoleTemplate): String {
        return role.promptPrefix.trim() + "\n\n" + text.trimStart()
    }

    // کلیدواژه‌ی لاتین باید کلمه‌ی مستقل باشد (مثلاً "api" داخل "capital" حساب نشود)
    private fun matches(haystack: String, keyword: String): Boolean {
        val isLatin = keyword.all { it.code < 128 }
        return if (isLatin && keyword.all { it.isLetter() }) {
            Regex("(?<![a-z])" + Regex.escape(keyword) + "(?![a-z])").containsMatchIn(haystack)
        } else {
            haystack.contains(keyword)
        }
    }
}
