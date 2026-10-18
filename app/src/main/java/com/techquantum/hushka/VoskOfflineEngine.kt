package com.techquantum.hushka

import android.content.Context
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.util.zip.ZipInputStream

object VoskOfflineEngine {
    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var currentLang: String? = null

    fun init(context: Context, language: String) {
        if (currentLang == language && recognizer != null) return
        release()
        try {
            val zipName = when (language) {
                "fa" -> "vosk-model-small-fa-0.4.zip"
                "en" -> "vosk-model-small-en-us-0.15.zip"
                "ar" -> "vosk-model-small-ar-0.3.zip"
                "tr" -> "vosk-model-small-tr-0.3.zip"
                else -> "vosk-model-small-en-us-0.15.zip"
            }
            val folderName = zipName.replace(".zip", "")
            val destDir = File(context.filesDir, folderName)
            
            if (!destDir.exists() || destDir.listFiles()?.isEmpty() == true) {
                unzipFromAssets(context, zipName, context.filesDir)
            }
            
            model = Model(destDir.absolutePath)
            recognizer = Recognizer(model, 16000f)
            currentLang = language
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun processAudio(buffer: ByteArray): String? {
        val rec = recognizer ?: return null
        return if (rec.acceptWaveForm(buffer, buffer.size)) {
            JSONObject(rec.result).optString("text", "").takeIf { it.isNotBlank() }
        } else null
    }

    fun release() {
        recognizer?.close(); model?.close()
        recognizer = null; model = null; currentLang = null
    }

    private fun unzipFromAssets(context: Context, zipFile: String, targetDir: File) {
        val zipInputStream = ZipInputStream(context.assets.open(zipFile))
        var entry = zipInputStream.nextEntry
        val buffer = ByteArray(4096)
        val canonicalTargetDir = targetDir.canonicalFile

        while (entry != null) {
            val file = File(targetDir, entry.name)
            if (!file.canonicalPath.startsWith(canonicalTargetDir.path + File.separator)) {
                throw SecurityException("Zip entry outside target dir: " + entry.name)
            }
            if (entry.isDirectory) {
                file.mkdirs()
            } else {
                file.parentFile?.mkdirs()
                val fileOutputStream = file.outputStream()
                var read: Int
                while (zipInputStream.read(buffer).also { read = it } != -1) {
                    fileOutputStream.write(buffer, 0, read)
                }
                fileOutputStream.close()
            }
            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
    }
}
