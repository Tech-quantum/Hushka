package com.techquantum.hushka

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface TranslationApi {
    @GET("get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String = "en|fa"
    ): MyMemoryResponse
}

data class MyMemoryResponse(
    val responseData: MyMemoryData?,
    val responseStatus: Int?,
    val responseDetails: String?
)

data class MyMemoryData(
    val translatedText: String?,
    val match: Double?
)

object TranslationService {

    private const val BASE_URL = "https://api.mymemory.translated.net/"
    private const val MAX_CHARS = 480

    private val api: TranslationApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TranslationApi::class.java)
    }

    /**
     * تبدیل markup های [xxx] و #xx به placeholder های امن قبل از ترجمه
     * سپس برمی‌گردونیم به شکل اصلی
     */
    private fun protectMarkups(text: String): Pair<String, Map<String, String>> {
        val placeholders = mutableMapOf<String, String>()
        var counter = 0
        // الگوهای رایج: [متن] یا #عدد یا [متن:عدد]
        val regex = Regex("\\[[^\\]]{1,30}\\]|#[0-9]+")
        val protected = regex.replace(text) { match ->
            val key = "__MRK${counter}__"
            placeholders[key] = match.value
            counter++
            key
        }
        return protected to placeholders
    }

    private fun restoreMarkups(text: String, placeholders: Map<String, String>): String {
        var result = text
        placeholders.forEach { (key, original) ->
            result = result.replace(key, original)
        }
        return result
    }

    /**
     * ترجمه با زبان‌های دلخواه (منبع و مقصد)
     */
    suspend fun translateText(
        text: String,
        sourceLang: String = "en",
        targetLang: String = "fa"
    ): String? = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext null

        try {
            val (protectedText, placeholders) = protectMarkups(text)
            val chunks = protectedText.chunked(MAX_CHARS)
            val translatedParts = mutableListOf<String>()
            val langPair = "$sourceLang|$targetLang"

            for ((index, chunk) in chunks.withIndex()) {
                val response = api.translate(chunk, langPair)
                val translated = response.responseData?.translatedText

                if (translated.isNullOrBlank() ||
                    translated.equals(chunk, ignoreCase = true)
                ) {
                    translatedParts.add(chunk)
                } else {
                    translatedParts.add(translated)
                }

                if (index < chunks.size - 1) {
                    delay(300)
                }
            }

            restoreMarkups(translatedParts.joinToString(" "), placeholders)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * نسخه سازگاری با کد قدیم (en → fa)
     */
    suspend fun translateText(text: String): String? =
        translateText(text, "en", "fa")
}
