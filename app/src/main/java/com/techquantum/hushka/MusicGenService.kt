package com.techquantum.hushka

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object MusicGenService {
    // تایم‌اوت طولانی چون تولید موسیقی زمان‌بر است
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateMusic(prompt: String, apiKey: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("inputs", prompt)
                // پارامترهای اختیاری برای تنظیمات بیشتر
                put("options", JSONObject().apply {
                    put("wait_for_model", true)
                })
            }
            
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://api-inference.huggingface.co/models/facebook/musicgen-small")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(body)
                .build()
                
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                return@withContext response.body?.bytes()
            }
            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
