package com.techquantum.hushka

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

    suspend fun getCachedWebPrompts(source: String): List<PromptEntity>?
    suspend fun cacheWebPrompts(source: String, prompts: List<PromptEntity>)
    suspend fun clearWebCache()
}

data class LibraryUpdateResult(
    val addedCount: Int,
    val firstNewPromptId: Long? = null
)
