package com.techquantum.hushka

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

data class PresentationSlide(
    val title: String,
    val content: String,
    val imageUrl: String
)

object PresentationGenerator {
    suspend fun generateSlides(
        context: Context,
        topic: String,
        slideCount: Int
    ): Result<List<PresentationSlide>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = AppSettings.getAiApiKey(context)
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("کلید API در تنظیمات وارد نشده"))
            }

            val provider = AiProvider.fromId(AppSettings.getAiProvider(context))
            val model = AppSettings.getAiModel(context).ifBlank { provider.defaultModel }

            val systemInstruction = "You are a presentation designer. Given a topic, create exactly $slideCount slides.\n" +
                "Format each slide EXACTLY as:\n" +
                "SLIDE: <title>\n" +
                "CONTENT: <2-3 concise lines>\n" +
                "---\n" +
                "Do not add any greetings or extra text. Start directly with SLIDE:"

            val messages = listOf(
                ChatMessage(role = ChatRole.SYSTEM, content = systemInstruction),
                ChatMessage(role = ChatRole.USER, content = "Topic: $topic\nSlide count: $slideCount")
            )

            val sb = StringBuilder()
            AiService.streamMessage(
                provider = provider,
                apiKey = apiKey,
                model = model,
                messages = messages
            ).collect { chunk ->
                sb.append(chunk)
            }

            val rawOutput = sb.toString().trim()
            val parsed = parseSlides(rawOutput)

            if (parsed.isEmpty()) {
                return@withContext Result.failure(Exception("پاسخ AI قابل تجزیه نبود"))
            }

            val withImages = parsed.mapIndexed { idx, s ->
                val encoded = URLEncoder.encode("${s.title} presentation slide", "UTF-8")
                val seed = kotlin.math.abs(topic.hashCode() + idx * 7919)
                val url = "https://image.pollinations.ai/prompt/$encoded?width=800&height=600&nologo=true&seed=$seed"
                s.copy(imageUrl = url)
            }

            Result.success(withImages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseSlides(raw: String): List<PresentationSlide> {
        val result = mutableListOf<PresentationSlide>()
        val blocks = raw.split(Regex("(?i)SLIDE\\s*:"))
        for (block in blocks) {
            if (block.isBlank()) continue
            val lines = block.trim().lines()
            if (lines.isEmpty()) continue
            val title = lines.first().trim().removeSuffix(":").take(80)
            val contentIdx = block.indexOf("CONTENT:", ignoreCase = true)
            val content = if (contentIdx >= 0) {
                block.substring(contentIdx + 8)
                    .split("---").firstOrNull()?.trim() ?: ""
            } else {
                lines.drop(1).joinToString(" ").trim()
            }
            if (title.isNotBlank() && content.isNotBlank()) {
                result.add(PresentationSlide(title, content, ""))
            }
        }
        return result
    }
}
