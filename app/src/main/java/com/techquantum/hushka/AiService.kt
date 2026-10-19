package com.techquantum.hushka

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    /**
     * استریم پاسخ از هوش مصنوعی (کلمه به کلمه)
     */
    fun streamMessage(
        provider: AiProvider,
        apiKey: String,
        model: String,
        messages: List<ChatMessage>
    ): Flow<String> = flow {
        val request = when (provider) {
            AiProvider.GEMINI -> buildGeminiStreamRequest(apiKey, model, messages)
            else -> buildOpenAiStreamRequest(provider, apiKey, model, messages)
        }

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw Exception("HTTP ${response.code}: ${errorBody.take(200)}")
            }

            val source = response.body?.source() ?: throw Exception("Empty response body")

            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()
                    if (data == "[DONE]") break
                    
                    try {
                        val json = JSONObject(data)
                        if (provider == AiProvider.GEMINI) {
                            val text = json.optJSONArray("candidates")
                                ?.optJSONObject(0)
                                ?.optJSONObject("content")
                                ?.optJSONArray("parts")
                                ?.optJSONObject(0)
                                ?.optString("text") ?: ""
                            if (text.isNotEmpty()) emit(text)
                        } else {
                            val delta = json.optJSONArray("choices")
                                ?.optJSONObject(0)
                                ?.optJSONObject("delta")
                            val content = delta?.optString("content") ?: ""
                            if (content.isNotEmpty()) emit(content)
                        }
                    } catch (e: Exception) {
                        // نادیده گرفتن خطاهای پارس کردن JSON ناقص در استریم
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun buildOpenAiStreamRequest(
        provider: AiProvider,
        apiKey: String,
        model: String,
        messages: List<ChatMessage>
    ): Request {
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
            put("stream", true)
        }

        return Request.Builder()
            .url("${provider.baseUrl}v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody(JSON))
            .build()
    }

    private fun buildGeminiStreamRequest(
        apiKey: String,
        model: String,
        messages: List<ChatMessage>
    ): Request {
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

        val url = "${AiProvider.GEMINI.baseUrl}v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"
        
        return Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody(JSON))
            .build()
    }

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
            Result.failure(e)
        }
    }

    private fun callOpenAiCompatible(provider: AiProvider, apiKey: String, model: String, messages: List<ChatMessage>): String {
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
        }
        val request = Request.Builder()
            .url("${provider.baseUrl}v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody(JSON))
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string() ?: throw Exception("Empty response")
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}: ${text.take(200)}")
            return JSONObject(text).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
        }
    }

    private fun callGemini(apiKey: String, model: String, messages: List<ChatMessage>): String {
        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                messages.forEach { msg ->
                    put(JSONObject().apply {
                        put("role", if (msg.role == ChatRole.USER) "user" else "model")
                        put("parts", JSONArray().apply { put(JSONObject().put("text", msg.content)) })
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
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}: ${text.take(200)}")
            val parts = JSONObject(text).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts")
            return parts.getJSONObject(0).getString("text")
        }
    }

    suspend fun testConnection(provider: AiProvider, apiKey: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val testMessages = listOf(ChatMessage(role = ChatRole.USER, content = "Reply with just: OK"))
                val result = sendMessage(provider, apiKey, provider.defaultModel, testMessages)
                Result.success(result.isSuccess)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
