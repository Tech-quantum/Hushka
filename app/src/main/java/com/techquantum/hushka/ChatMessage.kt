package com.techquantum.hushka

/**
 * یک پیام چت (کاربر یا AI)
 */
data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val role: ChatRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val promptId: Long = 0L
)

enum class ChatRole {
    USER,
    ASSISTANT,
    SYSTEM
}

/**
 * وضعیت صفحه چت
 */
data class ChatUiState(
    val promptTitle: String = "",
    val initialPrompt: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val provider: AiProvider = AiProvider.GEMINI,
    val model: String = "",
    val hasApiKey: Boolean = false
)
