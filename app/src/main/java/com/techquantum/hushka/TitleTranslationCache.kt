package com.techquantum.hushka

import java.util.concurrent.ConcurrentHashMap

object TitleTranslationCache {
    private val cache = ConcurrentHashMap<String, String>()

    suspend fun translate(text: String, targetLang: String): String {
        if (text.isBlank()) return text
        
        val finalTarget = if (targetLang == ALL_LANGUAGE || targetLang.isBlank()) "fa" else targetLang
        val sourceLang = LanguageDetector.detect(text)

        // اگر زبان متن با زبان مقصد یکی بود، نیازی به ترجمه نیست
        if (sourceLang == finalTarget) {
            return text
        }

        val key = "${finalTarget}_${text}"
        cache[key]?.let { return it }

        val translated = TranslationService.translateText(text, sourceLang, finalTarget)
        val result = if (translated.isNullOrBlank() || translated == text) text else translated

        cache[key] = result
        return result
    }
}
