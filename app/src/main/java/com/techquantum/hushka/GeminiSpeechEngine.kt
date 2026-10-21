package com.techquantum.hushka

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * مسیر دوم تشخیص گفتار آنلاین با Google Gemini.
 *
 * از مدل اختصاصی گفتار (`gemini-3.5-transcribe`) استفاده می‌کند که پشتیبانی
 * رسمی از فارسی (`fa-IR`) دارد. مثل مسیر Groq یک فایل صوتی می‌گیرد و متن
 * برمی‌گرداند.
 *
 * فایل صوتی به‌صورت inline (base64) فرستاده می‌شود؛ چون ضبط‌های ما حداکثر
 * چند صد کیلوبایت هستند و اینطور به یک درخواست HTTP نیاز داریم، نه دو تا
 * (بدون File API).
 */
object GeminiSpeechEngine {
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/interactions"
    private const val MODEL = "gemini-3.5-transcribe"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    suspend fun transcribe(apiKey: String, audioFile: File, language: String? = null): String? =
        withContext(Dispatchers.IO) {
            try {
                if (!audioFile.exists() || audioFile.length() == 0L) return@withContext null

                val base64Audio = Base64.encodeToString(audioFile.readBytes(), Base64.NO_WRAP)

                val body = JSONObject().apply {
                    put("model", MODEL)
                    put("input", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "audio")
                            put("data", base64Audio)
                            put("mime_type", mimeTypeFor(audioFile))
                        })
                    })
                    bcp47For(language)?.let { code ->
                        put("generation_config", JSONObject().apply {
                            put("transcription_config", JSONObject().apply {
                                put("language_codes", JSONArray().apply { put(code) })
                            })
                        })
                    }
                }

                val request = Request.Builder()
                    .url(API_URL)
                    .addHeader("x-goog-api-key", apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body.toString().toRequestBody(JSON))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                if (!response.isSuccessful) return@withContext null

                val json = JSONObject(responseBody ?: return@withContext null)
                extractText(json)?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                null
            }
        }

    private fun extractText(json: JSONObject): String? {
        json.optString("output_text", "").takeIf { it.isNotBlank() }?.let { return it }

        val steps = json.optJSONArray("steps") ?: return null
        val builder = StringBuilder()
        for (i in 0 until steps.length()) {
            val step = steps.optJSONObject(i) ?: continue
            val content = step.optJSONArray("content") ?: continue
            for (j in 0 until content.length()) {
                val item = content.optJSONObject(j) ?: continue
                if (item.optString("type") == "text" || item.has("text")) {
                    val text = item.optString("text", "")
                    if (text.isNotBlank()) {
                        if (builder.isNotEmpty()) builder.append(' ')
                        builder.append(text)
                    }
                }
            }
        }
        return builder.toString().takeIf { it.isNotBlank() }
    }

    private fun mimeTypeFor(file: File): String = when (file.extension.lowercase()) {
        "wav" -> "audio/wav"
        "mp3" -> "audio/mp3"
        "m4a" -> "audio/m4a"
        "aac" -> "audio/aac"
        "ogg" -> "audio/ogg"
        "flac" -> "audio/flac"
        "opus" -> "audio/opus"
        "webm" -> "audio/webm"
        else -> "audio/m4a"
    }

    private fun bcp47For(language: String?): String? = when (language?.lowercase()) {
        "fa" -> "fa-IR"
        "en" -> "en-US"
        "ar" -> "ar-EG"
        "tr" -> "tr-TR"
        else -> null
    }
}
