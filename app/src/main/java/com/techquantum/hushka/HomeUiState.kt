package com.techquantum.hushka

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
    val selectedLanguage: String = ALL_LANGUAGE,
    val selectedTags: Set<String> = emptySet(),
    val tagFilterMode: String = TAG_MODE_OR,
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
const val ALL_LANGUAGE = "__all_language__"
const val TAG_MODE_OR = "OR"
const val TAG_MODE_AND = "AND"
const val SORT_RELEVANT = "relevant"
const val SORT_MOST_VIEWED = "most_viewed"
const val SORT_LATEST = "latest"
const val SORT_FAVORITES = "favorites"
const val SORT_TITLE = "title"
const val SORT_CATEGORY = "category"
const val SORT_FASTEST = "fastest"
const val SORT_HIGHEST_SCORE = "highest_score"
const val SORT_RECOMMENDED = "recommended"
const val SORT_FEATURED = "featured"

fun promptDomain(category: String): String =
    category.substringBefore('.', category).ifBlank { "other" }
