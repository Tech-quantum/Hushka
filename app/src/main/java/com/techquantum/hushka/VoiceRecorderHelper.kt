package com.techquantum.hushka

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object VoiceRecorderHelper {
    private var mediaRecorder: MediaRecorder? = null
    private var audioRecord: AudioRecord? = null
    private var isRecordingVosk = false

    /** حداکثر زمان ضبط آفلاین. */
    private const val MAX_RECORD_MS = 5000L

    /** کم‌ترین زمانی که کاربر باید فرصت حرف‌زدن داشته باشد، حتی در سکوت کامل. */
    private const val MIN_SPEECH_MS = 1200L

    /** اگر این مدت پشت‌سرهم صدای قابل‌توجهی نیاید، ضبط تمام می‌شود. */
    private const val SILENCE_STOP_MS = 1500L

    /**
     * آستانه‌ی «بلندی صدا» برای تشخیص سکوت.
     *
     * مقدار RMS بافر PCM-16 را با آستانه‌ای ثابت و کوچک مقایسه می‌کنیم؛
     * هدف فقط تشخیص «صدای محیط در حد نویز» از «گفتار» است، نه اندازه‌گیری دقیق دسی‌بل.
     */
    private const val RMS_THRESHOLD = 1000.0

    /**
     * RMS بلوک صوتی را حساب می‌کند. یک بافر بزرگ‌تر یعنی نمونه‌ی بیشتر و تخمین پایدارتر.
     * برای اینکه یک اسپایک کوتاه کل بافر را «بلند» نکند، بافر را به چند تکه می‌شکنیم
     * و اگر تکه‌ای بلند بود، True برمی‌گردانیم.
     */
    private fun isLoud(buffer: ByteArray, read: Int, threshold: Double): Boolean {
        val samples = read / 2 // PCM 16-bit = 2 bytes per sample
        if (samples <= 0) return false
        val chunkSize = (samples / 4).coerceAtLeast(1)
        var i = 0
        while (i < samples) {
            val end = (i + chunkSize).coerceAtMost(samples)
            var sum = 0.0
            for (j in i until end) {
                val lo = buffer[2 * j].toInt() and 0xFF
                val hi = buffer[2 * j + 1].toInt()
                val sample = ((hi shl 8) or lo).toShort().toInt()
                sum += sample.toDouble() * sample
            }
            val rms = kotlin.math.sqrt(sum / (end - i))
            if (rms >= threshold) return true
            i = end
        }
        return false
    }

    // ─── ۱. ضبط برای Groq (آنلاین) ───
    fun startGroqRecording(
        context: Context,
        apiKey: String,
        language: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val audioFile = File(context.cacheDir, "voice_input.m4a")
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }

            // ضبط به مدت ۵ ثانیه
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    mediaRecorder?.stop()
                    mediaRecorder?.release()
                    mediaRecorder = null

                    CoroutineScope(Dispatchers.Main).launch {
                        val text = GroqWhisperEngine.transcribe(apiKey, audioFile, language)
                        if (text != null) onResult(text)
                        else onError("خطا در تبدیل صوت به متن (Groq)")
                    }
                } catch (e: Exception) {
                    onError("خطا در توقف ضبط: ${e.message}")
                }
            }, 5000) // 5 ثانیه

        } catch (e: Exception) {
            onError("خطا در شروع ضبط: ${e.message}")
        }
    }

    // ─── ۲. ضبط برای Vosk (آفلاین) ───
    fun startVoskRecording(
        context: Context,
        language: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val ready = VoskOfflineEngine.init(context, language)
        if (!ready) {
            onError(
                context.getString(
                    R.string.voice_model_unavailable,
                    VoskOfflineEngine.lastError ?: ""
                )
            )
            return
        }

        val sampleRate = 16000
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            onError("دسترسی به میکروفون داده نشده است")
            return
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        isRecordingVosk = true
        val buffer = ByteArray(bufferSize)

        audioRecord?.startRecording()

        // اجرای ضبط در پس‌زمینه (حداکثر ۵ ثانیه، ولی با تشخیص سکوت زودتر تمام می‌شود)
        CoroutineScope(Dispatchers.IO).launch {
            val startTime = System.currentTimeMillis()

            // تشخیص سکوت: اگر کاربر بعد از شروع، مدتی (حداقل MIN_SPEECH_MS) هیچ
            // صدایی تولید نکند، ضبط را تمام می‌کنیم تا ۵ ثانیه معطل نماند.
            var lastLoudAt = startTime

            var gotResult = false
            while (isRecordingVosk && System.currentTimeMillis() - startTime < MAX_RECORD_MS) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    if (isLoud(buffer, read, RMS_THRESHOLD)) {
                        lastLoudAt = System.currentTimeMillis()
                    }
                    val result = VoskOfflineEngine.processAudio(buffer)
                    if (result != null) {
                        gotResult = true
                        CoroutineScope(Dispatchers.Main).launch { onResult(result) }
                        break
                    }
                    // اگر کاربر مدتی ساکت بوده و حرف زده هم تمام شده، بس است.
                    val now = System.currentTimeMillis()
                    if (now - lastLoudAt > SILENCE_STOP_MS && now - startTime > MIN_SPEECH_MS) {
                        break
                    }
                }
            }

            // اگر تا پایان زمان هیچ جمله‌ی کاملی تشخیص داده نشد (مثلاً کاربر پیوسته
            // و بدون مکث حرف زد)، نتیجه‌ی نهایی را می‌گیریم تا گفتار از دست نرود.
            if (!gotResult) {
                val finalText = VoskOfflineEngine.finalResult()
                if (finalText != null) {
                    CoroutineScope(Dispatchers.Main).launch { onResult(finalText) }
                }
            }

            stopVoskRecording()
        }
    }

    private fun stopVoskRecording() {
        isRecordingVosk = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        VoskOfflineEngine.release()
    }
}
