package com.techquantum.hushka

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WebCacheDao {
    @Query("SELECT * FROM web_cache WHERE source = :source LIMIT 1")
    suspend fun get(source: String): WebCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: WebCacheEntity)

    @Query("DELETE FROM web_cache WHERE source = :source")
    suspend fun delete(source: String)

    @Query("DELETE FROM web_cache")
    suspend fun clearAll()
}
