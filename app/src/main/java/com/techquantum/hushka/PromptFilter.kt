package com.techquantum.hushka

import java.util.concurrent.ConcurrentHashMap

/**
 * کش امتیاز آنالیز پرامپت‌ها.
 * امتیاز هر پرامپت فقط یک بار محاسبه می‌شود و تا وقتی متن تغییر نکرده از کش خوانده می‌شود،
 * پس مرتب‌سازی «بیشترین امتیاز» دیگر برای هر مقایسه آنالیز کامل اجرا نمی‌کند.
 */
object PromptScoreCache {
    private val cache = ConcurrentHashMap<Long, Pair<Int, Int>>()

    fun scoreOf(prompt: PromptEntity): Int {
        val textHash = prompt.text.hashCode()
        val cached = cache[prompt.id]
        if (cached != null && cached.first == textHash) return cached.second
        val score = analyzePromptAdvanced(prompt.text).score
        cache[prompt.id] = textHash to score
        return score
    }
}

object PromptFilter {
    /** بازه‌ی «اخیراً استفاده‌شده»: هفت روز. */
    const val RECENT_WINDOW_MS = 7L * 24L * 60L * 60L * 1000L

    fun apply(
        prompts: List<PromptEntity>,
        query: String,
        selectedProvider: String,
        selectedDomain: String,
        selectedCategory: String,
        selectedCategories: Set<String>,
        selectedCollection: String,
        favoritesOnly: Boolean,
        sortMode: String,
        pinnedOnly: Boolean,
        recentOnly: Boolean,
        selectedLanguage: String = ALL_LANGUAGE,
        selectedTags: Set<String> = emptySet(),
        tagFilterMode: String = TAG_MODE_OR
    ): List<PromptEntity> {
        var result = prompts

        // فیلترها
        // جست‌وجوی متنی با موتور هوشمند: تحمل غلط تایپی، مترادف‌ها و مرز کلمه.
        // موتور قبلاً نوشته شده بود ولی جست‌وجو از یک contains ساده استفاده می‌کرد،
        // بنابراین «marketng» چیزی پیدا نمی‌کرد و «api» داخل «capital» هم match می‌شد.
        if (query.isNotBlank()) {
            result = SmartSearchEngine.rank(result, query)
        }

        if (selectedLanguage != ALL_LANGUAGE && selectedLanguage.isNotBlank()) {
            result = result.filter { LanguageDetector.detect(it.text) == selectedLanguage }
        }

        if (selectedProvider != ALL_PROVIDER && selectedProvider.isNotBlank()) {
            result = result.filter { it.provider.equals(selectedProvider, true) }
        }

        if (selectedCollection.isNotBlank() && selectedCollection != ALL_COLLECTION) {
            result = result.filter { it.collection == selectedCollection }
        }

        if (selectedCategory != ALL_CATEGORY && selectedCategory.isNotBlank()) {
            result = result.filter {
                it.category == selectedCategory || it.category.startsWith("$selectedCategory.")
            }
        }

        if (selectedCategories.isNotEmpty()) {
            result = result.filter { it.category in selectedCategories }
        }

        if (selectedTags.isNotEmpty()) {
            result = result.filter { prompt ->
                val promptTags = prompt.tags.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                val selTagsLower = selectedTags.map { it.lowercase() }
                
                if (tagFilterMode == TAG_MODE_AND) {
                    selTagsLower.all { promptTags.contains(it) }
                } else {
                    selTagsLower.any { promptTags.contains(it) }
                }
            }
        }

        if (favoritesOnly) {
            result = result.filter { it.favorite }
        }

        if (pinnedOnly) {
            result = result.filter { it.pinned }
        }

        if (recentOnly) {
            // پرامپت‌هایی که در ۷ روز گذشته استفاده شده‌اند.
            val cutoff = System.currentTimeMillis() - RECENT_WINDOW_MS
            result = result.filter { it.lastUsedAt >= cutoff }
        }

        // مرتب‌سازی
        result = when (sortMode) {
            SORT_RELEVANT -> {
                // نتیجه‌ی جست‌وجو از قبل بر اساس امتیاز مرتب شده است؛ دوباره مرتب نکن.
                if (query.isNotBlank()) result else result.sortedByDescending { it.id }
            }
            SORT_MOST_VIEWED -> result.sortedByDescending { it.lastUsedAt }
            SORT_LATEST -> result.sortedByDescending { it.id }
            SORT_FAVORITES -> result.sortedByDescending { if (it.favorite) 1 else 0 }
            SORT_TITLE -> result.sortedBy { it.title }
            SORT_CATEGORY -> result.sortedBy { it.category }
            SORT_FASTEST -> result.sortedBy { it.title.length }
            SORT_HIGHEST_SCORE -> result.sortedByDescending { PromptScoreCache.scoreOf(it) }
            SORT_RECOMMENDED -> result.sortedByDescending {
                var score = 0
                if (it.favorite) score += 5
                if (it.pinned) score += 4
                if (it.lastUsedAt > 0) score += 1
                score
            }
            SORT_FEATURED -> result.sortedByDescending { if (it.pinned) 1 else 0 }
            else -> result.sortedByDescending { it.id }
        }

        return result
    }
}
