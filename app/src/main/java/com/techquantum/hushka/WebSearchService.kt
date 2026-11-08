package com.techquantum.hushka

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

object WebSearchService {
    private val client = OkHttpClient()

    suspend fun search(query: String): String? = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://api.duckduckgo.com/?q=$encodedQuery&format=json&no_html=1&skip_disambig=1"
            
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) return@withContext null
            
            val json = JSONObject(response.body?.string() ?: "")
            val abstractText = json.optString("AbstractText")
            
            if (abstractText.isNotBlank()) {
                return@withContext abstractText
            }

            // اگر خلاصه نبود، از موضوعات مرتبط استفاده می‌کنیم
            val relatedTopics = json.optJSONArray("RelatedTopics")
            if (relatedTopics != null && relatedTopics.length() > 0) {
                val builder = StringBuilder()
                for (i in 0 until minOf(3, relatedTopics.length())) {
                    val topic = relatedTopics.getJSONObject(i)
                    val text = topic.optString("Text")
                    if (text.isNotBlank()) {
                        builder.append("- $text\n")
                    }
                }
                if (builder.isNotEmpty()) return@withContext builder.toString()
            }
            
            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
