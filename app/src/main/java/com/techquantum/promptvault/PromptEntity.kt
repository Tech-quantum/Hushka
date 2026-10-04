package com.techquantum.promptvault

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val catalogId: String = "",
    val provider: String = "",
    val modality: String = "",
    val title: String,
    val category: String,
    val text: String,
    val tags: String = "",
    val favorite: Boolean = false,
    val source: String = "local",
    val license: String = "",
    val collection: String = "",
    val pinned: Boolean = false,
    val lastUsedAt: Long = 0L
)
