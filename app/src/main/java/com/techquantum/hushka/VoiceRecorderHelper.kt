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

        // اجرای ضبط در پس‌زمینه (به مدت ۵ ثانیه)
        CoroutineScope(Dispatchers.IO).launch {
            val startTime = System.currentTimeMillis()
            var gotResult = false
            while (isRecordingVosk && System.currentTimeMillis() - startTime < 5000) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    val result = VoskOfflineEngine.processAudio(buffer)
                    if (result != null) {
                        gotResult = true
                        CoroutineScope(Dispatchers.Main).launch { onResult(result) }
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
