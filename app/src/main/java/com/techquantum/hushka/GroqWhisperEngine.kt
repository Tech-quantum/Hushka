package com.techquantum.hushka

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object GroqWhisperEngine {
    private const val API_URL = "https://api.groq.com/openai/v1/audio/transcriptions"
    private const val MODEL = "whisper-large-v3-turbo"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun transcribe(apiKey: String, audioFile: File, language: String? = null): String? = withContext(Dispatchers.IO) {
        try {
            val fileBody = audioFile.asRequestBody("audio/*".toMediaType())
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.name, fileBody)
                .addFormDataPart("model", MODEL)
                .apply { if (language != null) addFormDataPart("language", language) }
                .build()
            
            val request = Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(multipartBody)
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (!response.isSuccessful) return@withContext null
            
            val json = JSONObject(responseBody ?: return@withContext null)
            json.optString("text", "").takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }
}
