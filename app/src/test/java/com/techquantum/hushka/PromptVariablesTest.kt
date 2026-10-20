package com.techquantum.hushka

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptVariablesTest {

    @Test
    fun extractsPersianBracketVariables() {
        // رگرسیون: نسخه‌ی قبلی فقط [A-Z_]+ را می‌شناخت و متغیرهای فارسی را از دست می‌داد.
        val text = "برای تبلیغ [محصول] با بودجه [بودجه] یک تست A/B طراحی کن"
        assertEquals(listOf("محصول", "بودجه"), PromptVariables.extract(text))
        assertEquals(2, PromptVariables.count(text))
    }

    @Test
    fun extractsEnglishAndCurlyVariables() {
        val text = "You are [ROLE]. Output {FORMAT} for [TASK]"
        assertEquals(listOf("ROLE", "TASK", "FORMAT"), PromptVariables.extract(text))
    }

    @Test
    fun deduplicatesRepeatedVariables() {
        val text = "[TASK] then [TASK] again"
        assertEquals(listOf("TASK"), PromptVariables.extract(text))
    }

    @Test
    fun variablePlaceholdersMayContainCommas() {
        // داده‌های واقعی شامل placeholderهایی مثل [کانال، مثلاً اینستاگرام] هستند
        val text = "انتشار در [کانال، مثلاً اینستاگرام یا تلگرام] با [بودجه]"
        assertEquals(
            listOf("کانال، مثلاً اینستاگرام یا تلگرام", "بودجه"),
            PromptVariables.extract(text)
        )
    }

    @Test
    fun unmatchedBracketsAreIgnored() {
        assertEquals(emptyList<String>(), PromptVariables.extract("no variables here]]"))
    }

    @Test
    fun promptWithoutVariablesReturnsEmpty() {
        assertTrue(PromptVariables.extract("فقط یک متن ساده بدون متغیر").isEmpty())
    }
}
