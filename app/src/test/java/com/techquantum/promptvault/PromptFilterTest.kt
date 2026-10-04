package com.techquantum.promptvault

import org.junit.Assert.assertEquals
import org.junit.Test

class PromptFilterTest {

    private val prompts = listOf(
        PromptEntity(
            id = 1,
            title = "Alpha",
            category = "Programming",
            text = "Write Kotlin code",
            tags = "Kotlin, Android",
            favorite = false
        ),
        PromptEntity(
            id = 2,
            title = "Beta",
            category = "Writing",
            text = "Write a blog post",
            favorite = true
        ),
        PromptEntity(
            id = 3,
            title = "Gamma",
            category = "Programming",
            text = "Build a Compose screen",
            tags = "Compose",
            favorite = true
        )
    )

    private fun apply(
        query: String = "",
        selectedCategory: String = ALL_CATEGORY,
        favoritesOnly: Boolean = false,
        sortMode: String = SORT_LATEST,
        selectedProvider: String = ALL_PROVIDER,
        selectedDomain: String = ALL_DOMAIN,
        selectedCategories: Set<String> = emptySet(),
        selectedCollection: String = ALL_COLLECTION,
        pinnedOnly: Boolean = false,
        recentOnly: Boolean = false,
        nowMs: Long = 1_000_000L
    ) = PromptFilter.apply(
        prompts = prompts,
        query = query,
        selectedProvider = selectedProvider,
        selectedDomain = selectedDomain,
        selectedCategory = selectedCategory,
        selectedCategories = selectedCategories,
        selectedCollection = selectedCollection,
        favoritesOnly = favoritesOnly,
        pinnedOnly = pinnedOnly,
        recentOnly = recentOnly,
        sortMode = sortMode,
        nowMs = nowMs
    )

    @Test
    fun filtersByCategory() {
        val result = apply(selectedCategory = "Programming")
        assertEquals(listOf(3L, 1L), result.map { it.id })
    }

    @Test
    fun searchesAcrossTitleTagsAndText() {
        val result = apply(query = "compose")
        assertEquals(listOf(3L), result.map { it.id })
    }

    @Test
    fun favoritesOnlyReturnsFavorites() {
        val result = apply(favoritesOnly = true)
        assertEquals(listOf(3L, 2L), result.map { it.id })
    }

    @Test
    fun titleSortIsAscending() {
        val result = apply(sortMode = SORT_TITLE)
        assertEquals(listOf("Alpha", "Beta", "Gamma"), result.map { it.title })
    }

    @Test
    fun recentOnlyReturnsPromptsUsedWithinSevenDays() {
        val now = 1_000_000L
        val recent = prompts[0].copy(lastUsedAt = now - 1_000L)
        val old = prompts[1].copy(lastUsedAt = now - 8L * 24L * 60L * 60L * 1000L)
        val neverUsed = prompts[2].copy(lastUsedAt = 0L)

        val result = PromptFilter.apply(
            prompts = listOf(recent, old, neverUsed),
            query = "",
            selectedProvider = ALL_PROVIDER,
            selectedDomain = ALL_DOMAIN,
            selectedCategory = ALL_CATEGORY,
            selectedCategories = emptySet(),
            selectedCollection = ALL_COLLECTION,
            favoritesOnly = false,
            pinnedOnly = false,
            recentOnly = true,
            sortMode = SORT_LATEST,
            nowMs = now
        )

        assertEquals(listOf(1L), result.map { it.id })
    }
}
