package com.techquantum.hushka

/**
 * سرویس‌های هوش مصنوعی پشتیبانی‌شده
 */
enum class AiProvider(
    val id: String,
    val displayName: String,
    val emoji: String,
    val baseUrl: String,
    val defaultModel: String,
    val models: List<String>,
    val keyUrl: String
) {
    GEMINI(
        id = "gemini",
        displayName = "Google Gemini",
        emoji = "🔵",
        baseUrl = "https://generativelanguage.googleapis.com/",
        defaultModel = "gemini-1.5-flash",
        models = listOf(
            "gemini-1.5-flash",
            "gemini-1.5-pro",
            "gemini-2.0-flash-exp"
        ),
        keyUrl = "https://aistudio.google.com/apikey"
    ),
    OPENAI(
        id = "openai",
        displayName = "OpenAI",
        emoji = "🟢",
        baseUrl = "https://api.openai.com/",
        defaultModel = "gpt-4o-mini",
        models = listOf(
            "gpt-4o-mini",
            "gpt-4o",
            "gpt-4-turbo",
            "gpt-3.5-turbo"
        ),
        keyUrl = "https://platform.openai.com/api-keys"
    ),
    GROQ(
        id = "groq",
        displayName = "Groq (سریع)",
        emoji = "⚡",
        baseUrl = "https://api.groq.com/openai/",
        defaultModel = "llama-3.3-70b-versatile",
        models = listOf(
            "llama-3.3-70b-versatile",
            "llama-3.1-8b-instant",
            "mixtral-8x7b-32768"
        ),
        keyUrl = "https://console.groq.com/keys"
    ),
    DEEPSEEK(
        id = "deepseek",
        displayName = "DeepSeek",
        emoji = "🐋",
        baseUrl = "https://api.deepseek.com/",
        defaultModel = "deepseek-chat",
        models = listOf(
            "deepseek-chat",
            "deepseek-reasoner"
        ),
        keyUrl = "https://platform.deepseek.com/api_keys"
    );

    companion object {
        fun fromId(id: String): AiProvider =
            values().firstOrNull { it.id == id } ?: GEMINI
    }
}
