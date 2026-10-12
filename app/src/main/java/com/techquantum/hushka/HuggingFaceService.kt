package com.techquantum.hushka

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

data class TranslationRequest(
    val inputs: String,
    val options: TranslationOptions? = null
)

data class TranslationOptions(
    val wait_for_model: Boolean = true
)

data class TranslationResponse(
    val translation_text: String?
)

interface HuggingFaceApi {
    @POST("models/{model}")
    suspend fun translate(
        @Path("model") model: String,
        @Header("Authorization") token: String,
        @Body request: TranslationRequest
    ): List<TranslationResponse>
}

object HuggingFaceService {
    private const val BASE_URL = "https://api-inference.huggingface.co/"
    private const val MODEL = "Helsinki-NLP/opus-mt-en-fa" // مدل ترجمه انگلیسی به فارسی

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val api: HuggingFaceApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(HuggingFaceApi::class.java)

    suspend fun translateText(text: String, token: String): String? {
        return try {
            val response = api.translate(
                model = MODEL,
                token = "Bearer $token",
                request = TranslationRequest(inputs = text)
            )
            response.firstOrNull()?.translation_text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
