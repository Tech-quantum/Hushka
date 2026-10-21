package com.techquantum.hushka

import android.content.Context
import android.util.Log
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.util.zip.ZipInputStream

/**
 * موتور تشخیص گفتار آفلاین با Vosk.
 *
 * نتیجه‌ی [init] با [isReady] قابل بررسی است؛ اگر مدل آماده نشده باشد
 * (مثلاً ZIP مدل در assets ناقص/LFS-pointer باشد) هنگام شروع ضبط به کاربر
 * خطای واضح نشان داده می‌شود، نه اینکه میکروفون بی‌صدا بماند.
 */
object VoskOfflineEngine {
    private const val TAG = "VoskOfflineEngine"

    // مدل سنگین است و لودش ۱-۳ ثانیه طول می‌کشد؛ بین ضبط‌ها نگه داشته می‌شود.
    private var model: Model? = null
    private var modelLang: String? = null

    // recognizer سبک است ولی state دارد؛ هر ضبط یک نسخه‌ی تازه می‌سازد.
    private var recognizer: Recognizer? = null

    @Volatile
    var lastError: String? = null
        private set

    val isReady: Boolean
        get() = recognizer != null

    /** آخرین پیام خطای آماده‌سازی مدل، یا null اگر مدل درست بارگذاری شده باشد. */
    fun init(context: Context, language: String): Boolean {
        lastError = null
        return try {
            ensureModel(context, language)
            // هر شروع ضبط یک recognizer تازه می‌خواهد (state قبلی مصرف شده است).
            runCatching { recognizer?.close() }
            recognizer = Recognizer(model, 16000f)
            true
        } catch (e: Exception) {
            lastError = e.message ?: e.javaClass.simpleName
            Log.e(TAG, "Failed to load Vosk model for '$language'", e)
            runCatching { recognizer?.close() }
            recognizer = null
            false
        }
    }

    /** مدل را فقط وقتی لود می‌کند که هنوز لود نشده یا زبان عوض شده باشد. */
    private fun ensureModel(context: Context, language: String) {
        if (model != null && modelLang == language) return

        // زبان عوض شده؛ مدل قبلی را آزاد کن.
        runCatching { model?.close() }
        model = null
        modelLang = null

        val zipName = zipAssetFor(language)
        val folderName = zipName.removeSuffix(".zip")
        val destDir = File(context.filesDir, folderName)

        if (!isExtracted(destDir)) {
            unzipFromAssets(context, zipName, context.filesDir)
        }

        model = Model(destDir.absolutePath)
        modelLang = language
    }

    fun processAudio(buffer: ByteArray): String? {
        val rec = recognizer ?: return null
        return if (rec.acceptWaveForm(buffer, buffer.size)) {
            JSONObject(rec.result).optString("text", "").takeIf { it.isNotBlank() }
        } else null
    }

    /**
     * پایان ضبط: نتیجه‌ی نهایی (final result) را استخراج می‌کند.
     *
     * اگر کاربر پیوسته (بدون مکث) حرف بزند، ممکن است [processAudio] هیچ‌وقت
     * نتیجه ندهد؛ در آن صورت بدون این متد تمام گفتار از دست می‌رفت.
     */
    fun finalResult(): String? {
        val rec = recognizer ?: return null
        return runCatching {
            JSONObject(rec.finalResult).optString("text", "").takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    /**
     * پایان جلسه‌ی ضبط: فقط recognizer آزاد می‌شود و **مدل در حافظه می‌ماند**
     * تا ضبط بعدی سریع شروع شود (لود مدل ۱-۳ ثانیه طول می‌کشد).
     */
    fun release() {
        runCatching { recognizer?.close() }
        recognizer = null
    }

    /** پاک‌سازی کامل (مدل + recognizer) — مثلاً هنگام تغییر زبان یا خروج از اپ. */
    fun releaseAll() {
        runCatching { recognizer?.close() }
        runCatching { model?.close() }
        recognizer = null; model = null; modelLang = null
    }

    private fun zipAssetFor(language: String): String = when (language) {
        "fa" -> "vosk-model-small-fa-0.4.zip"
        "en" -> "vosk-model-small-en-us-0.15.zip"
        "ar" -> "vosk-model-small-ar-0.3.zip"
        "tr" -> "vosk-model-small-tr-0.3.zip"
        else -> "vosk-model-small-en-us-0.15.zip"
    }

    /**
     * مدل استخراج‌شده وقتی سالم است که فایل‌های کلیدی مدل داخلش پیدا شوند.
     *
     * فقط چک کردن پوشه‌ی `am` کافی نیست: اگر استخراج وسط کار قطع شود، پوشه
     * ساخته می‌شود ولی فایل‌ها ناقص می‌مانند و بعداً مدل خراب لود می‌شود.
     */
    private fun isExtracted(destDir: File): Boolean =
        destDir.isDirectory &&
            File(destDir, "am/final.mdl").exists() &&
            File(destDir, "conf/mfcc.conf").exists() &&
            File(destDir, "graph/HCLG.fst").exists()

    private fun unzipFromAssets(context: Context, zipFile: String, targetDir: File) {
        val zipInputStream = ZipInputStream(context.assets.open(zipFile))
        var entry = zipInputStream.nextEntry
        val buffer = ByteArray(4096)
        val canonicalTargetDir = targetDir.canonicalFile
        var extractedAnything = false

        while (entry != null) {
            val file = File(targetDir, entry.name)
            if (!file.canonicalPath.startsWith(canonicalTargetDir.path + File.separator)) {
                throw SecurityException("Zip entry outside target dir: " + entry.name)
            }
            if (entry.isDirectory) {
                file.mkdirs()
            } else {
                file.parentFile?.mkdirs()
                file.outputStream().use { output ->
                    var read: Int
                    while (zipInputStream.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                }
                extractedAnything = true
            }
            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()

        // ZIP-pointer های Git-LFS یک فایل متنی‌اند و هیچ ورودی واقعی ندارند.
        if (!extractedAnything) {
            throw IllegalStateException(
                "Voice model asset '$zipFile' is not a real archive (empty or Git-LFS pointer)"
            )
        }
    }
}
