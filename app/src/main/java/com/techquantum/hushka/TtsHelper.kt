package com.techquantum.hushka

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
            }
        }
    }

    fun speak(text: String, langCode: String) {
        if (!isInitialized || text.isBlank()) return

        // نگاشت کد زبان اپ به لوکال اندروید
        val locale = when (langCode) {
            "fa" -> Locale("fa", "IR")
            "ar" -> Locale("ar", "EG") // عربی استاندارد
            "tr" -> Locale("tr", "TR")
            "en" -> Locale.US
            else -> Locale.getDefault()
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // اگر موتور TTS دستگاه زبان مورد نظر را نداشت، به زبان پیش‌فرض برمی‌گردیم
            tts?.setLanguage(Locale.getDefault())
        }

        // متوقف کردن هر صدای در حال پخش
        tts?.stop()
        
        // پخش صدای جدید
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "hushka_tts_id")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
