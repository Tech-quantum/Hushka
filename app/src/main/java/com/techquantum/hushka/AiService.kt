package com.techquantum.hushka

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * سرویس یکپارچه برای همه پرووایدرها (OpenAI-compatible + Gemini)
 */
object AiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    /**
     * ارسال پیام و دریافت پاسخ
     */
    suspend fun sendMessage(
        provider: AiProvider,
        apiKey: String,
        model: String,
        messages: List<ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val responseText = when (provider) {
                AiProvider.GEMINI -> callGemini(apiKey, model, messages)
                else -> callOpenAiCompatible(provider, apiKey, model, messages)
            }
            Result.success(responseText)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ==================== OpenAI / Groq / DeepSeek ====================
    private fun callOpenAiCompatible(
        provider: AiProvider,
        apiKey: String,
        model: String,
        messages: List<ChatMessage>
    ): String {
        val body = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                messages.forEach { msg ->
                    put(JSONObject().apply {
                        put("role", when (msg.role) {
                            ChatRole.USER -> "user"
                            ChatRole.ASSISTANT -> "assistant"
                            ChatRole.SYSTEM -> "system"
                        })
                        put("content", msg.content)
                    })
                }
            })
            put("temperature", 0.7)
            put("max_tokens", 2048)
        }

        val request = Request.Builder()
            .url("${provider.baseUrl}v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            val text = response.body?.string() ?: throw Exception("Empty response")
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${text.take(200)}")
            }
            val json = JSONObject(text)
            val choices = json.optJSONArray("choices") ?: throw Exception("No choices")
            if (choices.length() == 0) throw Exception("Empty choices")
            return choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }

    // ==================== Gemini ====================
    private fun callGemini(
        apiKey: String,
        model: String,
        messages: List<ChatMessage>
    ): String {
        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                messages.forEach { msg ->
                    put(JSONObject().apply {
                        put("role", if (msg.role == ChatRole.USER) "user" else "model")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", msg.content))
                        })
                    })
                }
            })
        }

        val url = "${AiProvider.GEMINI.baseUrl}v1beta/models/$model:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            val text = response.body?.string() ?: throw Exception("Empty response")
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${text.take(200)}")
            }
            val json = JSONObject(text)
            val candidates = json.optJSONArray("candidates") ?: throw Exception("No candidates")
            if (candidates.length() == 0) throw Exception("Empty candidates")
            val parts = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
            if (parts.length() == 0) throw Exception("Empty parts")
            return parts.getJSONObject(0).getString("text")
        }
    }

    /**
     * تست سریع اتصال با API Key
     */
    suspend fun testConnection(provider: AiProvider, apiKey: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val testMessages = listOf(
                    ChatMessage(
                        role = ChatRole.USER,
                        content = "Reply with just: OK"
                    )
                )
                val result = sendMessage(provider, apiKey, provider.defaultModel, testMessages)
                Result.success(result.isSuccess)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
