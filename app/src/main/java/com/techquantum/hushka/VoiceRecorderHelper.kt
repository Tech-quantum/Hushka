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
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.ArrayList
import kotlin.math.sqrt

// --- متغیرهای مورد نیاز برای ضبط آنلاین ---
private val ONLINE_SAMPLE_RATE = 16000
private val ONLINE_CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO
private val ONLINE_AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT

private val ONLINE_MAX_RECORD_MS = 5000L
private val ONLINE_RMS_THRESHOLD = 800.0
private val ONLINE_MIN_SPEECH_MS = 1000L
private val ONLINE_SILENCE_STOP_MS = 1200L

@Volatile
private var isRecordingOnline = false
// -------------------------------------------package com.techquantum.hushka


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

    // ─── کمک‌کننده‌های ساخت فایل WAV ───

    private fun writeWavFile(file: File, pcmData: ByteArray) {
        val sampleRate = ONLINE_SAMPLE_RATE
        val channels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = ((channels * bitsPerSample) / 8).toShort()
        val dataSize = pcmData.size
        val totalSize = 36 + dataSize

        FileOutputStream(file).use { out ->
            out.write("RIFF".toByteArray())
            out.write(intToLE(totalSize))
            out.write("WAVE".toByteArray())
            out.write("fmt ".toByteArray())
            out.write(intToLE(16))
            out.write(shortToLE(1))
            out.write(shortToLE(channels.toShort()))
            out.write(intToLE(sampleRate))
            out.write(intToLE(byteRate))
            out.write(shortToLE(blockAlign))
            out.write(shortToLE(bitsPerSample.toShort()))
            out.write("data".toByteArray())
            out.write(intToLE(dataSize))
            out.write(pcmData)
        }
    }

    private fun intToLE(value: Int): ByteArray {
        val bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
        bb.putInt(value)
        return bb.array()
    }

    private fun shortToLE(value: Short): ByteArray {
        val bb = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
        bb.putShort(value)
        return bb.array()
    }

    private fun calculateRms(data: ByteArray): Double {
        if (data.isEmpty()) return 0.0
        var sum = 0.0
        var i = 0
        val size = data.size
        while (i + 1 < size) {
            val low = data[i].toInt() and 0xFF
            val high = data[i + 1].toInt() shl 8
            val sample = (high or low).toShort()
            val s = sample.toDouble()
            sum += s * s
            i += 2
        }
        val count = size / 2
        return if (count == 0) 0.0 else Math.sqrt(sum / count)
    }

    // ─── ۱. ضبط برای آنلاین (Groq / Gemini) با تشخیص سکوت ───
    //
    // از AudioRecord + RMS استفاده می‌کنیم (مشابه مسیر آفلاین).
    // فایل خروجی: voice_input.wav (PCM 16-bit mono 16kHz) که هم Groq و هم Gemini پشتیبانی می‌کنند.
    // در صورت شکست Groq و در دسترس بودن کلید Gemini، بدون ضبط مجدد از همان فایل WAV فالبک انجام می‌شود.

    fun startGroqRecording(
        context: Context,
        apiKey: String,
        language: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        engine: OnlineSpeechEngine = OnlineSpeechEngine.GROQ,
        fallbackEngine: OnlineSpeechEngine? = null,
        fallbackApiKey: String? = null
    ) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            onError("دسترسی به میکروفون داده نشده است")
            return
        }

        try {
            audioRecord?.release()
        } catch (_: Exception) {
        }
        audioRecord = null
        isRecordingOnline = false

        val audioFile = File(context.cacheDir, "voice_input.wav")
        if (audioFile.exists()) {
            audioFile.delete()
        }

        val bufferSize = AudioRecord.getMinBufferSize(
            ONLINE_SAMPLE_RATE,
            ONLINE_CHANNEL_CONFIG,
            ONLINE_AUDIO_FORMAT
        )
        if (bufferSize <= 0) {
            onError("خطا در آماده‌سازی ضبط آنلاین")
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                ONLINE_SAMPLE_RATE,
                ONLINE_CHANNEL_CONFIG,
                ONLINE_AUDIO_FORMAT,
                bufferSize * 2
            )

            val record = audioRecord ?: run {
                onError("خطا در ایجاد نمونه ضبط آنلاین")
                return
            }

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                try {
                    record.release()
                } catch (_: Exception) {
                }
                audioRecord = null
                onError("خطا در مقداردهی اولیه ضبط آنلاین")
                return
            }

            val pcmBuffer = ByteArray(bufferSize)
            val pcmList = ArrayList<Byte>(bufferSize * 32)
            val startTime = System.currentTimeMillis()
            var speechStartTime: Long = -1L
            var lastSpeechTime: Long = -1L

            record.startRecording()
            isRecordingOnline = true

            Thread {
                try {
                    while (isRecordingOnline) {
                        val now = System.currentTimeMillis()
                        val elapsed = now - startTime
                        if (elapsed >= ONLINE_MAX_RECORD_MS) {
                            break
                        }

                        val readCount = record.read(pcmBuffer, 0, pcmBuffer.size)
                        if (readCount > 0) {
                            var i = 0
                            while (i < readCount) {
                                pcmList.add(pcmBuffer[i])
                                i++
                            }

                            val chunk = pcmBuffer.copyOf(readCount)
                            val rms = calculateRms(chunk)
                            if (rms >= ONLINE_RMS_THRESHOLD) {
                                if (speechStartTime == -1L) {
                                    speechStartTime = now
                                }
                                lastSpeechTime = now
                            }

                            if (speechStartTime != -1L && lastSpeechTime != -1L) {
                                val hasEnoughSpeech = elapsed >= ONLINE_MIN_SPEECH_MS
                                val silenceDuration = now - lastSpeechTime
                                if (hasEnoughSpeech && silenceDuration >= ONLINE_SILENCE_STOP_MS) {
                                    break
                                }
                            }
                        }
                        Thread.sleep(10)
                    }

                    isRecordingOnline = false
                    try {
                        if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                            record.stop()
                        }
                    } catch (_: Exception) {
                    }
                    try {
                        record.release()
                    } catch (_: Exception) {
                    }
                    audioRecord = null

                    val pcmBytes = ByteArray(pcmList.size)
                    for (j in pcmList.indices) {
                        pcmBytes[j] = pcmList[j]
                    }
                    writeWavFile(audioFile, pcmBytes)

                    Handler(Looper.getMainLooper()).post {
                        CoroutineScope(Dispatchers.Main).launch {
                            val primaryText = when (engine) {
                                OnlineSpeechEngine.GROQ -> GroqWhisperEngine.transcribe(apiKey, audioFile, language)
                                OnlineSpeechEngine.GEMINI -> GeminiSpeechEngine.transcribe(apiKey, audioFile, language)
                                else -> null
                            }

                            if (!primaryText.isNullOrBlank()) {
                                onResult(primaryText)
                                return@launch
                            }

                            if (engine == OnlineSpeechEngine.GROQ && fallbackEngine == OnlineSpeechEngine.GEMINI && !fallbackApiKey.isNullOrBlank()) {
                                val fallbackText = GeminiSpeechEngine.transcribe(fallbackApiKey, audioFile, language)
                                if (!fallbackText.isNullOrBlank()) {
                                    onResult(fallbackText)
                                    return@launch
                                }
                            }

                            onError("خطا در تبدیل صوت به متن (${engine.displayName})")
                        }
                    }
                } catch (e: Exception) {
                    isRecordingOnline = false
                    try {
                        val r = audioRecord
                        if (r?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                            r.stop()
                        }
                        r?.release()
                    } catch (_: Exception) {
                    }
                    audioRecord = null
                    Handler(Looper.getMainLooper()).post {
                        onError("خطا در ضبط آنلاین: ${e.message}")
                    }
                }
            }.start()
        } catch (e: Exception) {
            try {
                audioRecord?.release()
            } catch (_: Exception) {
            }
            audioRecord = null
            isRecordingOnline = false
            onError("خطا در شروع ضبط آنلاین: ${e.message}")
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
