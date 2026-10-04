package com.techquantum.promptvault

data class HomeUiState(
    val prompts: List<PromptEntity> = emptyList(),
    val filteredPrompts: List<PromptEntity> = emptyList(),
    val query: String = "",
    val selectedProvider: String = ALL_PROVIDER,
    val selectedDomain: String = ALL_DOMAIN,
    val selectedCategory: String = ALL_CATEGORY,
    val selectedCategories: Set<String> = emptySet(),
    val selectedCollection: String = ALL_COLLECTION,
    val pinnedOnly: Boolean = false,
    val recentOnly: Boolean = false,
    val favoritesOnly: Boolean = false,
    val sortMode: String = SORT_LATEST,
    val isUpdating: Boolean = false,
    val updateMessageKey: String = "",
    val updateAddedCount: Int = 0,
    val updateError: String = "",
    val newPromptId: Long? = null
)

const val ALL_PROVIDER = "__all_provider__"
const val ALL_DOMAIN = "__all_domain__"
const val ALL_CATEGORY = "__all__"
const val ALL_COLLECTION = "__all_collection__"
const val SORT_LATEST = "latest"
const val SORT_TITLE = "title"
const val SORT_CATEGORY = "category"
const val SORT_FAVORITES = "favorites"

fun promptDomain(category: String): String =
    category.substringBefore('.', category).ifBlank { "other" }
