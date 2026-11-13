package com.techquantum.hushka

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.util.concurrent.TimeUnit

data class MusicGenResult(
    val audioBytes: ByteArray? = null,
    val httpCode: Int? = null,
    val error: String? = null,
    val fileExtension: String = "wav"
) {
    val isSuccess: Boolean
        get() = audioBytes != null
}

object MusicGenService {
    private const val BASE_URL = "https://sezoha-hushka-musicgen.hf.space"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateMusic(prompt: String): MusicGenResult =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().put("prompt", prompt.trim())
                val body = json.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("$BASE_URL/gradio_api/call/v2/generate_music")
                    .post(body)
                    .build()

                val eventId = client.newCall(request).execute().use { response ->
                    val raw = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext MusicGenResult(
                            httpCode = response.code,
                            error = raw.take(300).ifBlank { "Request failed" }
                        )
                    }
                    JSONObject(raw).optString("event_id")
                        .takeIf { it.isNotBlank() }
                        ?: return@withContext MusicGenResult(
                            httpCode = response.code,
                            error = "Missing generation event ID"
                        )
                }

                val resultRequest = Request.Builder()
                    .url("$BASE_URL/gradio_api/call/generate_music/$eventId")
                    .get()
                    .build()

                val outputUrl = client.newCall(resultRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext MusicGenResult(
                            httpCode = response.code,
                            error = response.body?.string().orEmpty()
                                .take(300).ifBlank { "Generation result request failed" }
                        )
                    }

                    var completedData: String? = null
                    var serverError: String? = null
                    var currentEvent = ""

                    response.body?.source()?.use { source ->
                        while (!source.exhausted()) {
                            val line = source.readUtf8Line() ?: continue

                            when {
                                line.startsWith("event:") ->
                                    currentEvent = line.substringAfter(":").trim()

                                line.startsWith("data:") -> {
                                    val data = line.substringAfter(":").trim()
                                    when (currentEvent) {
                                        "complete" -> {
                                            completedData = data
                                            break
                                        }
                                        "error" -> {
                                            serverError = data
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (serverError != null) {
                        return@withContext MusicGenResult(
                            error = serverError!!.take(300)
                        )
                    }

                    val data = completedData
                        ?: return@withContext MusicGenResult(
                            error = "Generation timed out or stream ended unexpectedly"
                        )

                    val outputs = JSONArray(data)
                    val output = outputs.optJSONObject(0)
                        ?: return@withContext MusicGenResult(
                            error = "Invalid audio response"
                        )

                    output.optString("url")
                        .takeIf { it.isNotBlank() }
                        ?: return@withContext MusicGenResult(
                            error = "Audio URL missing from server response"
                        )
                }

                val audioUrl = URI(BASE_URL).resolve(outputUrl).toString()
                val audioRequest = Request.Builder().url(audioUrl).get().build()

                client.newCall(audioRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext MusicGenResult(
                            httpCode = response.code,
                            error = "Audio download failed: HTTP ${response.code}"
                        )
                    }

                    val bytes = response.body?.bytes()
                    if (bytes == null || bytes.isEmpty()) {
                        return@withContext MusicGenResult(
                            httpCode = response.code,
                            error = "Downloaded audio is empty"
                        )
                    }

                    val path = URI(audioUrl).path.orEmpty()
                    val extension = path.substringAfterLast('.', "wav")
                        .takeIf { it.matches(Regex("[a-zA-Z0-9]{2,5}")) }
                        ?.lowercase() ?: "wav"

                    MusicGenResult(
                        audioBytes = bytes,
                        httpCode = response.code,
                        fileExtension = extension
                    )
                }
            } catch (e: Exception) {
                MusicGenResult(
                    error = "Connection error: ${e.javaClass.simpleName}: ${e.message.orEmpty().take(180)}"
                )
            }
        }
}
