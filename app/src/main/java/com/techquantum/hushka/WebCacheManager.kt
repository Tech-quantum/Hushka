package com.techquantum.hushka

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * کش فایل‌محور برای نتایج واکشی وب
 * هر منبع یک فایل JSON جدا دارد در filesDir.
 */
object WebCacheManager {

    private const val TAG = "WebCacheManager"
    private const val VALIDITY_MS = 24L * 60L * 60L * 1000L

    // ⚡ in-memory cache برای دسترسی سریع در طول سشن
    private val memoryCache = java.util.concurrent.ConcurrentHashMap<String, List<PromptEntity>>()

    private fun fileFor(context: Context, source: String): File =
        File(context.filesDir, "web_cache_$source.json")

    fun get(context: Context, source: String): List<PromptEntity>? = try {
        // ⚡ اول چک کن توی حافظه هست یا نه
        memoryCache[source]?.let {
            Log.d(TAG, "memory hit: $source (${it.size} prompts)")
            return it
        }

        val f = fileFor(context, source)
        if (!f.exists()) {
            Log.d(TAG, "cache miss (file not exists): $source")
            null
        } else {
            val age = System.currentTimeMillis() - f.lastModified()
            if (age > VALIDITY_MS) {
                Log.d(TAG, "cache expired: $source (age=${age}ms)")
                null
            } else {
                val text = f.readText(Charsets.UTF_8)
                val list = jsonToPrompts(text)
                // ⚡ ذخیره در حافظه برای دفعه بعد
                memoryCache[source] = list
                Log.d(TAG, "disk hit: $source (${list.size} prompts)")
                list
            }
        }
    } catch (e: Throwable) {
        Log.e(TAG, "cache read failed: $source", e)
        null
    }

    fun put(context: Context, source: String, prompts: List<PromptEntity>) {
        try {
            // ⚡ ذخیره در حافظه
            memoryCache[source] = prompts
            // 💾 ذخیره در فایل
            val f = fileFor(context, source)
            f.writeText(promptsToJson(prompts), Charsets.UTF_8)
            Log.d(TAG, "cache write: $source (${prompts.size} prompts)")
        } catch (e: Throwable) {
            Log.e(TAG, "cache write failed: $source", e)
        }
    }

    fun clearAll(context: Context) {
        try {
            // ⚡ پاک کردن حافظه
            memoryCache.clear()
            // 💾 پاک کردن فایل‌ها
            val files = context.filesDir.listFiles { f ->
                f.name.startsWith("web_cache_") && f.name.endsWith(".json")
            } ?: emptyArray()
            files.forEach { it.delete() }
            Log.d(TAG, "cache cleared: ${files.size} files + memory")
        } catch (e: Throwable) {
            Log.e(TAG, "cache clear failed", e)
        }
    }

    private fun promptsToJson(prompts: List<PromptEntity>): String {
        val arr = JSONArray()
        prompts.forEach { p ->
            arr.put(JSONObject().apply {
                put("title", p.title)
                put("category", p.category)
                put("text", p.text)
                put("tags", p.tags)
            })
        }
        return arr.toString()
    }

    private fun jsonToPrompts(json: String): List<PromptEntity> {
        val arr = JSONArray(json)
        val list = ArrayList<PromptEntity>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                PromptEntity(
                    id = -(i + 1).toLong(),
                    title = o.optString("title"),
                    category = o.optString("category"),
                    text = o.optString("text"),
                    tags = o.optString("tags"),
                    source = "web_cache"
                )
            )
        }
        return list
    }
}
