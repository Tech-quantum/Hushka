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
        
        val userMsg = ChatMessage(role = ChatRole.USER, content = text)
        
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMsg,
            secondaryMessages = if (_uiState.value.isCompareMode) _uiState.value.secondaryMessages + userMsg else _uiState.value.secondaryMessages,
            inputText = "",
            error = null
        )
        sendToAi()
        if (_uiState.value.isCompareMode) {
            sendToSecondaryAi()
        }
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
            
            val assistantMsg = ChatMessage(role = ChatRole.ASSISTANT, content = "")
            val currentMessages = _uiState.value.messages + assistantMsg
            _uiState.value = _uiState.value.copy(messages = currentMessages)

            try {
                AiService.streamMessage(
                    provider = provider, apiKey = apiKey,
                    model = model, messages = _uiState.value.messages.dropLast(1)
                ).collect { chunk ->
                    val msgs = _uiState.value.messages.toMutableList()
                    if (msgs.isNotEmpty()) {
                        val lastMsg = msgs.last()
                        msgs[msgs.size - 1] = lastMsg.copy(content = lastMsg.content + chunk)
                        _uiState.value = _uiState.value.copy(messages = msgs)
                    }
                }
                
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                
            } catch (e: Exception) {
                val msgs = _uiState.value.messages.toMutableList()
                if (msgs.isNotEmpty() && msgs.last().role == ChatRole.ASSISTANT) {
                    val lastMsg = msgs.last()
                    msgs[msgs.size - 1] = lastMsg.copy(
                        content = if (lastMsg.content.isBlank()) (e.message ?: "Unknown error") else lastMsg.content,
                        isError = lastMsg.content.isBlank()
                    )
                }
                _uiState.value = _uiState.value.copy(
                    messages = msgs, isLoading = false, error = e.message
                )
            }
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

    fun toggleCompareMode() {
        _uiState.value = _uiState.value.copy(isCompareMode = !_uiState.value.isCompareMode)
    }

    fun updateSecondaryProvider(provider: AiProvider) {
        _uiState.value = _uiState.value.copy(
            secondaryProvider = provider,
            secondaryModel = provider.defaultModel
        )
    }

    private fun sendToSecondaryAi() {
        viewModelScope.launch {
            val apiKey = AppSettings.getAiApiKey(context)
            if (apiKey.isBlank()) return@launch

            val provider = _uiState.value.secondaryProvider
            val model = _uiState.value.secondaryModel.ifBlank { provider.defaultModel }
            val assistantMsg = ChatMessage(role = ChatRole.ASSISTANT, content = "")
            val currentMessages = _uiState.value.secondaryMessages + assistantMsg
            _uiState.value = _uiState.value.copy(secondaryMessages = currentMessages)

            try {
                AiService.streamMessage(
                    provider = provider, apiKey = apiKey,
                    model = model, messages = _uiState.value.secondaryMessages.dropLast(1)
                ).collect { chunk ->
                    val msgs = _uiState.value.secondaryMessages.toMutableList()
                    if (msgs.isNotEmpty()) {
                        val lastMsg = msgs.last()
                        msgs[msgs.size - 1] = lastMsg.copy(content = lastMsg.content + chunk)
                        _uiState.value = _uiState.value.copy(secondaryMessages = msgs)
                    }
                }
            } catch (e: Exception) {
                // مدیریت خطا (اختیاری)
            }
        }
    }
}
