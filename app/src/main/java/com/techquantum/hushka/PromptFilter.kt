package com.techquantum.hushka

object PromptFilter {
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
        recentOnly: Boolean
    ): List<PromptEntity> {
        var result = prompts

        // فیلترها
        if (query.isNotBlank()) {
            val q = query.lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                it.text.lowercase().contains(q) ||
                it.tags.lowercase().contains(q)
            }
        }

        if (selectedProvider != ALL_PROVIDER && selectedProvider.isNotBlank()) {
            result = result.filter { it.provider.equals(selectedProvider, true) }
        }

        if (selectedCollection.isNotBlank() && selectedCollection != ALL_COLLECTION) {
            result = result.filter { it.collection == selectedCollection }
        }

        if (selectedCategory != ALL_CATEGORY && selectedCategory.isNotBlank()) {
            result = result.filter { it.category == selectedCategory }
        }

        if (selectedCategories.isNotEmpty()) {
            result = result.filter { it.category in selectedCategories }
        }

        if (favoritesOnly) {
            result = result.filter { it.favorite }
        }

        if (pinnedOnly) {
            result = result.filter { it.pinned }
        }

        // مرتب‌سازی
        result = when (sortMode) {
            SORT_RELEVANT -> {
                if (query.isNotBlank()) {
                    result.sortedByDescending { p ->
                        val q = query.lowercase()
                        var score = 0
                        if (p.title.lowercase().contains(q)) score += 3
                        if (p.tags.lowercase().contains(q)) score += 2
                        if (p.text.lowercase().contains(q)) score += 1
                        score
                    }
                } else {
                    result.sortedByDescending { it.id }
                }
            }
            SORT_MOST_VIEWED -> result.sortedByDescending { it.lastUsedAt }
            SORT_LATEST -> result.sortedByDescending { it.id }
            SORT_FAVORITES -> result.sortedByDescending { if (it.favorite) 1 else 0 }
            SORT_TITLE -> result.sortedBy { it.title }
            SORT_CATEGORY -> result.sortedBy { it.category }
            SORT_FASTEST -> result.sortedBy { it.title.length }
            SORT_HIGHEST_SCORE -> result.sortedByDescending {
                analyzePromptAdvanced(it.text).score
            }
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
