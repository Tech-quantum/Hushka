package com.techquantum.promptvault

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartSearchEngineTest {

    private val prompts = listOf(
        PromptEntity(
            id = 1,
            title = "Professional Image Generator",
            category = "Image",
            tags = "photo, portrait",
            text = "Create a professional studio portrait"
        ),
        PromptEntity(
            id = 2,
            title = "Kotlin Code Reviewer",
            category = "Programming",
            tags = "kotlin, code",
            text = "Review Android source code and suggest improvements"
        ),
        PromptEntity(
            id = 3,
            title = "Marketing Copywriter",
            category = "Marketing",
            tags = "content, marketing",
            text = "Write persuasive marketing copy"
        )
    )

    @Test
    fun categoryIntentFindsProgrammingPrompts() {
        val result = SmartSearchEngine.rank(prompts, "پرامپت برنامه نویسی")
        assertEquals(2L, result.first().id)
    }

    @Test
    fun synonymSearchFindsImagePrompt() {
        val result = SmartSearchEngine.rank(prompts, "ساخت عکس حرفه ای")
        assertEquals(1L, result.first().id)
    }

    @Test
    fun englishAndPersianSearchAreNormalized() {
        val result = SmartSearchEngine.rank(prompts, "PHOTO")
        assertTrue(result.any { it.id == 1L })
    }

    @Test
    fun typoToleranceFindsLongWords() {
        val result = SmartSearchEngine.rank(prompts, "marketng")
        assertTrue(result.any { it.id == 3L })
    }
}
