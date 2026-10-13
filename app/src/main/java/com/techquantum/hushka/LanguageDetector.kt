package com.techquantum.hushka

/**
 * تشخیص خودکار زبان متن بر اساس کاراکترها.
 * - fa: متن فارسی (دارای حروف پ چ ژ گ)
 * - ar: متن عربی (حروف فارسی-عربی بدون حروف خاص فارسی)
 * - tr: متن ترکی (حروف خاص ترکی)
 * - en: بقیه (پیش‌فرض)
 */
object LanguageDetector {

    fun detect(text: String): String {
        if (text.isBlank()) return "en"

        val letters = text.count { it.isLetter() }
        if (letters == 0) return "en"

        // حروف فارسی/عربی در بازه \u0600..\u06FF
        val persianArabicCount = text.count { it in '\u0600'..'\u06FF' }
        if (persianArabicCount.toFloat() / letters > 0.3f) {
            // تفکیک فارسی از عربی: حروف مخصوص فارسی
            val persianSpecific = text.count { it in "پچژگ" }
            return if (persianSpecific > 0) "fa" else "ar"
        }

        // حروف خاص ترکی
        val turkishCount = text.count { it in "çğıöşüÇĞİÖŞÜ" }
        if (turkishCount.toFloat() / letters > 0.02f) return "tr"

        return "en"
    }
}