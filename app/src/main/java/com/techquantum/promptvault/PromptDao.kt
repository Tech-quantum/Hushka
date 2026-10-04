package com.techquantum.promptvault

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY favorite DESC, id DESC")
    fun observeAll(): Flow<List<PromptEntity>>

    @Insert
    suspend fun insert(prompt: PromptEntity): Long

    @Update
    suspend fun update(prompt: PromptEntity)

    @Delete
    suspend fun delete(prompt: PromptEntity)

    @Query("SELECT COUNT(*) FROM prompts")
    suspend fun count(): Int

    @Query("SELECT title FROM prompts")
    suspend fun getAllTitles(): List<String>

    @Query("SELECT source FROM prompts WHERE source LIKE 'prompts.chat:%'")
    suspend fun getLibraryKeys(): List<String>

    @Query("SELECT * FROM prompts WHERE source LIKE 'prompts.chat:%'")
    suspend fun getLibraryPrompts(): List<PromptEntity>

    @Query("SELECT catalogId FROM prompts WHERE catalogId != ''")
    suspend fun getCatalogIds(): List<String>
}
