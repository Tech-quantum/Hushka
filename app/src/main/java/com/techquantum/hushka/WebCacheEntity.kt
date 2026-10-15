package com.techquantum.hushka

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "web_cache")
data class WebCacheEntity(
    @PrimaryKey val source: String,
    val payload: String,
    val cachedAt: Long
)
