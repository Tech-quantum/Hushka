package com.techquantum.hushka

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ClipboardHelper {
    private val scope = CoroutineScope(Dispatchers.Main)

    fun copyWithAutoClear(context: Context, text: String, timeoutMs: Long = 30_000L) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Hushka", text))
            scope.launch {
                delay(timeoutMs)
                try {
                    val current = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                    if (current == text) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            clipboard.clearPrimaryClip()
                        } else {
                            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                        }
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
