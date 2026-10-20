package com.techquantum.hushka

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        recentOnly: Boolean = false
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
        sortMode = sortMode
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
    fun searchToleratesTyposViaSmartEngine() {
        // رگرسیون: جست‌وجوی قبلی یک contains ساده بود و غلط تایپی را تحمل نمی‌کرد.
        // موتور برای کلمات ۸ حرفی و بیشتر، فاصله‌ی ویرایشی ۲ را می‌پذیرد.
        val result = apply(query = "kotlinn", sortMode = SORT_RELEVANT)
        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test
    fun searchDoesNotMatchSubstringsInsideOtherWords() {
        // «اپ» نباید داخل «اپلیکیشن» یا کلمات دیگر بیفتد؛ فقط مرز کلمه‌ی درست.
        val withAlpha = prompts + PromptEntity(
            id = 4,
            title = "Capital Market Analyst",
            category = "Finance",
            text = "Explain the capital markets crash"
        )
        val result = PromptFilter.apply(
            prompts = withAlpha,
            query = "api",
            selectedProvider = ALL_PROVIDER,
            selectedDomain = ALL_DOMAIN,
            selectedCategory = ALL_CATEGORY,
            selectedCategories = emptySet(),
            selectedCollection = ALL_COLLECTION,
            favoritesOnly = false,
            pinnedOnly = false,
            recentOnly = false,
            sortMode = SORT_RELEVANT
        )
        assertTrue("capital نباید با api پیدا شود", result.none { it.id == 4L })
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
        val now = System.currentTimeMillis()
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
            sortMode = SORT_LATEST
        )

        assertEquals(listOf(1L), result.map { it.id })
    }
}
