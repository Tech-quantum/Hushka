package com.techquantum.hushka

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
        if (containsPhrase(title, query)) score += 80
        if (containsPhrase(category, query)) score += 70
        if (containsPhrase(tags, query)) score += 60
        if (containsPhrase(text, query)) score += 45
        // توکن‌ها با مرز کلمه سنجیده می‌شوند تا «api» داخل «capital» نیفتد.
        for (token in queryTokens) when {
            containsWord(title, token) -> score += 28
            containsWord(category, token) -> score += 24
            containsWord(tags, token) -> score += 20
            containsWord(text, token) -> score += 10
        }
        for (phrase in queryPhrases) {
            // تک‌کلمه‌ای‌ها همان توکن‌ها هستند و بالاتر حساب شده‌اند.
            if (phrase.split(WS_REGEX).size < 2) continue
            if (containsPhrase(category, phrase)) score += 45
            if (containsPhrase(tags, phrase)) score += 35
            if (containsPhrase(title, phrase)) score += 30
            if (containsPhrase(text, phrase)) score += 15
        }
        for (token in queryTokens.filter { it.length >= 4 }) {
            if (similar(token, title) || similar(token, category) || similar(token, tags)) score += 8
        }
        return score
    }

    /**
     * آیا عبارت در متن هست؟ هم برای عبارت‌های چندکلمه‌ای و هم تک‌کلمه‌ای،
     * تطبیق باید روی مرز کلمه باشد تا «api» داخل «capital» نیفتد.
     */
    private fun containsPhrase(field: String, phrase: String): Boolean {
        if (phrase.isBlank()) return false
        val words = field.split(WS_REGEX)
        val phraseWords = phrase.split(WS_REGEX).filter { it.isNotBlank() }
        if (phraseWords.isEmpty()) return false
        if (phraseWords.size == 1) return words.any { it == phraseWords[0] }
        outer@ for (i in 0..words.size - phraseWords.size) {
            for (j in phraseWords.indices) {
                if (words[i + j] != phraseWords[j]) continue@outer
            }
            return true
        }
        return false
    }

    /** آیا توکن به‌عنوان یک کلمه‌ی کامل (نه بخشی از کلمه‌ی دیگر) در متن هست؟ */
    private fun containsWord(field: String, token: String): Boolean =
        token.isNotBlank() && field.split(WS_REGEX).any { it == token }

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
        tokens.forEach { token -> 
            SYNONYMS[token]?.let { synonyms ->
                result.addAll(synonyms)
                // جستجوی زنجیره‌ای برای پیدا کردن مترادفِ مترادف‌ها
                synonyms.forEach { syn -> 
                    SYNONYMS[syn]?.let { result.addAll(it) }
                }
            } 
        }
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
        // --- تصویر و طراحی ---
        "عکس" to setOf("تصویر", "image", "photo", "picture", "تصاویر", "عکس‌ها"),
        "تصویر" to setOf("عکس", "image", "photo", "picture", "تصاویر", "نقاشی", "طراحی"),
        "طراحی" to setOf("دیزاین", "design", "drawing", "رسم", "نقاشی"),
        "دیزاین" to setOf("طراحی", "design", "ui", "ux"),
        "لوگو" to setOf("logo", "نشان", "آرم"),
        "بنر" to setOf("banner", "پوستر", "poster"),

        // --- ویدیو و صدا ---
        "ویدیو" to setOf("ویدئو", "فیلم", "video", "movie", "clip", "کلیپ"),
        "ویدئو" to setOf("ویدیو", "فیلم", "video", "movie", "clip", "کلیپ"),
        "فیلم" to setOf("ویدیو", "ویدئو", "video", "movie", "سینما"),
        "صدا" to setOf("موسیقی", "audio", "sound", "voice", "صوت"),
        "موسیقی" to setOf("صدا", "music", "audio", "آهنگ", "نوا"),

        // --- برنامه‌نویسی و فنی ---
        "کد" to setOf("برنامه", "برنامه نویسی", "code", "programming", "coding", "اسکریپت", "script"),
        "برنامه" to setOf("کد", "programming", "code", "app", "application", "اپلیکیشن"),
        "برنامه نویسی" to setOf("کد", "کدنویسی", "programming", "coding", "developer", "توسعه"),
        "باگ" to setOf("bug", "خطا", "error", "مشکل", "issue"),
        "خطا" to setOf("باگ", "error", "bug", "exception"),
        "دیتابیس" to setOf("database", "db", "داده", "data"),

        // --- نوشتن و محتوا ---
        "نویسندگی" to setOf("نوشتن", "writing", "متن", "text", "مقاله", "article", "blog", "بلاگ"),
        "نوشتن" to setOf("نویسندگی", "writing", "تایپ", "type", "متن"),
        "متن" to setOf("نوشته", "text", "content", "محتوا", "مقاله"),
        "محتوا" to setOf("content", "متن", "مطلب", "پست", "post"),
        "ترجمه" to setOf("translate", "translation", "برگردان", "interpret"),

        // --- بازاریابی و کسب و کار ---
        "بازاریابی" to setOf("مارکتینگ", "marketing", "تبلیغات", "advertising", "تبلیغ"),
        "تبلیغات" to setOf("بازاریابی", "advertising", "marketing", "کمپین", "campaign"),
        "فروش" to setOf("sale", "sales", "فروشنده", "خرید", "buy", "purchase"),
        "کسب و کار" to setOf("کسب‌وکار", "business", "بیزینس", "شرکت", "company", "استارتاپ", "startup"),
        "سئو" to setOf("seo", "search engine optimization", "بهینه سازی"),

        // --- تحصیل و شغل ---
        "آموزش" to setOf("education", "یادگیری", "learning", "درس", "lesson", "تدریس", "teaching"),
        "یادگیری" to setOf("آموزش", "learning", "education", "مطالعه", "study"),
        "رزومه" to setOf("resume", "cv", "کارنامه", "سوابق"),
        "شغل" to setOf("job", "کار", "کاریابی", "استخدام", "employment", "حرفه", "career"),
        "مصاحبه" to setOf("interview", "مصاحبه کاری", "گفتگو"),

        // --- عمومی و متفرقه ---
        "ماشین" to setOf("خودرو", "اتومبیل", "car", "automobile", "vehicle", "وسیله نقلیه"),
        "خودرو" to setOf("ماشین", "اتومبیل", "car", "vehicle"),
        "خانه" to setOf("منزل", "house", "home", "آپارتمان", "apartment"),
        "غذا" to setOf("food", "خوراک", "آشپزی", "cooking", "دستور پخت", "recipe"),
        "آشپزی" to setOf("غذا", "cooking", "food", "پخت", "baking"),
        "سفر" to setOf("travel", "گردشگری", "tourism", "مسافرت", "trip"),
        "سلامتی" to setOf("health", "بهداشت", "درمان", "treatment", "پزشکی", "medical"),
        "ورزش" to setOf("sport", "exercise", "تمرین", "fitness", "تناسب اندام"),
        "روانشناسی" to setOf("psychology", "روان", "ذهن", "mind", "روح", "soul"),
        "پول" to setOf("money", "مالی", "finance", "سرمایه", "investment", "اقتصاد", "economy"),
        "تحقیق" to setOf("research", "پژوهش", "بررسی", "study", "جستجو", "search"),
        "داستان" to setOf("story", "قصه", "حکایت", "روایت", "narrative"),
        "شعر" to setOf("poem", "poetry", "شاعر", "poet")
    )
}
