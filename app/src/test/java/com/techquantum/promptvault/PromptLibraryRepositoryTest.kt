package com.techquantum.promptvault

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PromptLibraryRepositoryTest {

    @Test
    fun substringsInsideOtherWordsDoNotMatch() {
        // "api" inside "capital", "ui" inside "build"/"guide" used to cause wrong categories
        assertEquals("Finance", PromptLibraryRepository.categoryFor("Historian", "Explain the capital markets crash as a financial historian"))
        assertEquals("General", PromptLibraryRepository.categoryFor("Chef", "Act as a personal chef and build a weekly meal plan"))
        assertEquals("General", PromptLibraryRepository.categoryFor("Coach", "Give me a quick guide to staying focused"))
    }

    @Test
    fun wholeWordsStillMatch() {
        assertEquals("Programming", PromptLibraryRepository.categoryFor("Python Developer", "Write clean code"))
        assertEquals("Programming", PromptLibraryRepository.categoryFor("Helper", "Design a REST api for orders"))
        assertEquals("Design & UX", PromptLibraryRepository.categoryFor("Helper", "Review the ui of this screen"))
        assertEquals("SEO", PromptLibraryRepository.categoryFor("Helper", "Suggest a keyword for my site"))
    }

    @Test
    fun aiTagOnlyMatchesTheWordAi() {
        assertFalse("ai" in PromptLibraryRepository.tagsFor("Summary", "Please maintain a plain, said summary").split(", "))
        assertEquals("ai, chatgpt, python", PromptLibraryRepository.tagsFor("AI tutor", "Use ChatGPT to learn python"))
    }
}
