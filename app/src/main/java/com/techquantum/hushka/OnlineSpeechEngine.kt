package com.techquantum.hushka

/**
 * موتورهای تشخیص گفتار آنلاین (ضبط کوتاه میکروفون).
 *
 * مسیر آفلاین (Vosk) اینجا نیست چون مدل محلی است و کلید نمی‌خواهد.
 */
enum class OnlineSpeechEngine(
    val id: String,
    val displayName: String
) {
    /** Groq Whisper — سریع‌ترین و پیش‌فرض. */
    GROQ("groq", "Groq"),

    /** Google Gemini — پشتیبانی رسمی فارسی؛ مسیر دوم. */
    GEMINI("gemini", "Gemini");

    companion object {
        fun fromId(id: String): OnlineSpeechEngine =
            values().firstOrNull { it.id == id } ?: GROQ
    }
}
