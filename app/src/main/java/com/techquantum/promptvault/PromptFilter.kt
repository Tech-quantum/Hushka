package com.techquantum.promptvault

object PromptFilter {
    private const val RECENT_WINDOW_MS = 7L * 24L * 60L * 60L * 1000L

    fun apply(
        prompts: List<PromptEntity>,
        query: String,
        selectedProvider: String,
        selectedDomain: String,
        selectedCategory: String,
        selectedCategories: Set<String>,
        selectedCollection: String,
        favoritesOnly: Boolean,
        pinnedOnly: Boolean,
        recentOnly: Boolean,
        sortMode: String,
        nowMs: Long = System.currentTimeMillis()
    ): List<PromptEntity> {
        val base = prompts.filter {
            (selectedProvider == ALL_PROVIDER || it.provider == selectedProvider) &&
                (selectedDomain == ALL_DOMAIN || promptDomain(it.category) == selectedDomain) &&
                (selectedCategory == ALL_CATEGORY || it.category == selectedCategory) &&
                (selectedCategories.isEmpty() || it.category in selectedCategories) &&
                (selectedCollection == ALL_COLLECTION || it.collection == selectedCollection) &&
                (!favoritesOnly || it.favorite) &&
                (!pinnedOnly || it.pinned) &&
                (!recentOnly || isRecent(it.lastUsedAt, nowMs))
        }

        if (query.isBlank()) return sort(base, sortMode)

        val smartRanked = SmartSearchEngine.rank(base, query)
        return when (sortMode) {
            SORT_TITLE -> smartRanked.sortedBy { it.title.lowercase() }
            SORT_CATEGORY -> smartRanked.sortedBy { it.category.lowercase() }
            SORT_FAVORITES -> smartRanked.sortedWith(
                compareByDescending<PromptEntity> { it.favorite }.thenByDescending { it.id }
            )
            else -> smartRanked
        }
    }

    private fun isRecent(lastUsedAt: Long, nowMs: Long): Boolean =
        lastUsedAt > 0L && lastUsedAt <= nowMs && nowMs - lastUsedAt <= RECENT_WINDOW_MS

    private fun sort(prompts: List<PromptEntity>, sortMode: String): List<PromptEntity> = when (sortMode) {
        SORT_TITLE -> prompts.sortedBy { it.title.lowercase() }
        SORT_CATEGORY -> prompts.sortedBy { it.category.lowercase() }
        SORT_FAVORITES -> prompts.sortedWith(
            compareByDescending<PromptEntity> { it.favorite }.thenByDescending { it.id }
        )
        else -> prompts.sortedByDescending { it.id }
    }
}
