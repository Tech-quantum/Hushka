package com.techquantum.hushka

import android.content.Context
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File

object VoskOfflineEngine {
    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var currentLang: String? = null

    fun init(context: Context, language: String) {
        if (currentLang == language && recognizer != null) return
        release()
        try {
            val modelPath = "vosk-model-small-$language"
            val destDir = File(context.filesDir, modelPath)
            if (!destDir.exists()) {
                copyAssetFolder(context, modelPath, destDir.absolutePath)
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

    private fun copyAssetFolder(context: Context, src: String, dest: String) {
        val assets = context.assets
        val children = assets.list(src) ?: return
        File(dest).mkdirs()
        if (children.isEmpty()) {
            assets.open(src).use { input -> File(dest).outputStream().use { output -> input.copyTo(output) } }
        } else {
            for (child in children) copyAssetFolder(context, "$src/$child", "$dest/$child")
        }
    }
}
