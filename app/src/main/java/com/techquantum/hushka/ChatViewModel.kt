package com.techquantum.hushka

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(
    application: Application,
    private val initialPromptText: String
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _uiState = MutableStateFlow(
        ChatUiState(
            promptTitle = initialPromptText.take(60).replace("\n", " ").trim(),
            initialPrompt = initialPromptText,
            provider = AiProvider.fromId(AppSettings.getAiProvider(context)),
            model = AppSettings.getAiModel(context).ifBlank {
                AiProvider.fromId(AppSettings.getAiProvider(context)).defaultModel
            },
            hasApiKey = AppSettings.getAiApiKey(context).isNotBlank()
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        if (_uiState.value.hasApiKey && initialPromptText.isNotBlank()) {
            sendInitialPrompt()
        }
    }

    private fun sendInitialPrompt() {
        val prompt = _uiState.value.initialPrompt
        if (prompt.isBlank()) return
        _uiState.value = _uiState.value.copy(
            messages = listOf(ChatMessage(role = ChatRole.USER, content = prompt)),
            error = null
        )
        sendToAi()
    }

    fun onInputChange(value: String) {
        _uiState.value = _uiState.value.copy(inputText = value)
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isLoading) return
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + ChatMessage(
                role = ChatRole.USER, content = text
            ),
            inputText = "",
            error = null
        )
        sendToAi()
    }

    private fun sendToAi() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val apiKey = AppSettings.getAiApiKey(context)
            if (apiKey.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, hasApiKey = false, error = "no_api_key"
                )
                return@launch
            }
            val provider = _uiState.value.provider
            val model = _uiState.value.model.ifBlank { provider.defaultModel }
            val result = withContext(Dispatchers.IO) {
                AiService.sendMessage(
                    provider = provider, apiKey = apiKey,
                    model = model, messages = _uiState.value.messages
                )
            }
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + ChatMessage(
                            role = ChatRole.ASSISTANT, content = response
                        ),
                        isLoading = false, error = null
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + ChatMessage(
                            role = ChatRole.ASSISTANT,
                            content = e.message ?: "Unknown error",
                            isError = true
                        ),
                        isLoading = false, error = e.message
                    )
                }
            )
        }
    }

    fun regenerate() {
        if (_uiState.value.isLoading) return
        val messages = _uiState.value.messages.toMutableList()
        while (messages.isNotEmpty() && messages.last().role == ChatRole.ASSISTANT) {
            messages.removeAt(messages.size - 1)
        }
        if (messages.isEmpty()) return
        _uiState.value = _uiState.value.copy(messages = messages, error = null)
        sendToAi()
    }

    fun clearChat() {
        _uiState.value = _uiState.value.copy(
            messages = emptyList(), inputText = "", error = null, isLoading = false
        )
    }

    fun updateProvider(provider: AiProvider) {
        AppSettings.setAiProvider(context, provider.id)
        AppSettings.setAiModel(context, provider.defaultModel)
        _uiState.value = _uiState.value.copy(
            provider = provider, model = provider.defaultModel,
            hasApiKey = AppSettings.getAiApiKey(context).isNotBlank()
        )
    }
}
