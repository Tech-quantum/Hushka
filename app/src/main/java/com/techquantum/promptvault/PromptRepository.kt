package com.techquantum.promptvault

import kotlinx.coroutines.flow.Flow

interface PromptRepository {
    val prompts: Flow<List<PromptEntity>>

    suspend fun initializeDefaults()
    suspend fun addPrompt(prompt: PromptEntity): Long
    suspend fun updatePrompt(prompt: PromptEntity)
    suspend fun deletePrompt(prompt: PromptEntity)
    suspend fun toggleFavorite(prompt: PromptEntity)
    suspend fun togglePinned(prompt: PromptEntity)
    suspend fun markUsed(prompt: PromptEntity)
    suspend fun updateLibrary(): LibraryUpdateResult
}

data class LibraryUpdateResult(
    val addedCount: Int,
    val firstNewPromptId: Long? = null
)
