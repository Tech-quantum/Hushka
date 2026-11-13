package com.techquantum.hushka

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class MusicGenResult(
    val audioBytes: ByteArray? = null,
    val httpCode: Int? = null,
    val error: String? = null
) {
    val isSuccess: Boolean
        get() = audioBytes != null
}

object MusicGenService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateMusic(prompt: String, apiKey: String): MusicGenResult =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("inputs", prompt)
                    put("options", JSONObject().apply {
                        put("wait_for_model", true)
                    })
                }

                val body = json.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("https://api-inference.huggingface.co/models/facebook/musicgen-small")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body

                    if (!response.isSuccessful) {
                        val rawError = responseBody?.string().orEmpty()
                        val serverError = try {
                            JSONObject(rawError).optString("error")
                                .takeIf { it.isNotBlank() }
                        } catch (_: Exception) {
                            null
                        }

                        val message = (serverError ?: rawError)
                            .ifBlank { "Empty error response" }
                            .take(300)

                        return@withContext MusicGenResult(
                            httpCode = response.code,
                            error = message
                        )
                    }

                    val contentType = responseBody?.contentType()?.toString().orEmpty()
                    val bytes = responseBody?.bytes()
                    if (bytes == null || bytes.isEmpty()) {
                        return@withContext MusicGenResult(
                            httpCode = response.code,
                            error = "Empty audio response"
                        )
                    }

                    val startsWithJson = bytes.firstOrNull()?.toInt()?.toChar() == '{'

                    if (contentType.contains("json", ignoreCase = true) || startsWithJson) {
                        val raw = bytes.toString(Charsets.UTF_8)
                        val message = try {
                            JSONObject(raw).optString("error")
                                .takeIf { it.isNotBlank() }
                        } catch (_: Exception) {
                            null
                        } ?: "Server returned JSON instead of audio"

                        return@withContext MusicGenResult(
                            httpCode = response.code,
                            error = message.take(300)
                        )
                    }

                    MusicGenResult(audioBytes = bytes, httpCode = response.code)
                }
            } catch (e: Exception) {
                MusicGenResult(
                    error = "Connection error: ${e.javaClass.simpleName}"
                )
            }
        }
}
