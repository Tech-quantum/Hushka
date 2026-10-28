package com.techquantum.hushka
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.DisposableEffect

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * دیالوگ نمایش نتیجه ترجمه خودکار پرامپت
 */
@Composable
fun TranslatedPromptDialog(
    originalTitle: String,
    sourceLang: String,
    targetLang: String,
    isLoading: Boolean,
    translatedText: String?,
    error: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {

    val ttsHelper = remember { TtsHelper(context) }
    DisposableEffect(Unit) {
        onDispose { ttsHelper.shutdown() }
    }
    val context = LocalContext.current
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    "🌐  " + stringResource(R.string.translate_prompt),
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${localizedLangName(sourceLang)} → ${localizedLangName(targetLang)}",
                    color = textGrayColor,
                    fontSize = 11.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // عنوان پرامپت
                Text(
                    originalTitle,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )

                Spacer(Modifier.height(12.dp))

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = purplePrimary,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    stringResource(R.string.translating),
                                    color = textGrayColor,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚠️", fontSize = 40.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.translation_error),
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    localizedError(error),
                                    color = textGrayColor,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    translatedText != null -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = cardLightColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                translatedText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(14.dp),
                                color = textColor,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (translatedText != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { ttsHelper.speak(translatedText, AppSettings.getLanguage(context)) }) {
                        Text("🔊", fontSize = 18.sp)
                    }
                    TextButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Hushka Prompt", translatedText))
                        Toast.makeText(context, context.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
                    }) {
                        Text("📋 " + stringResource(R.string.copy), color = purplePrimary, fontSize = 12.sp)
                    }
                    TextButton(onClick = {
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, translatedText)
                                putExtra(Intent.EXTRA_SUBJECT, originalTitle)
                            }
                            context.startActivity(
                                Intent.createChooser(shareIntent, context.getString(R.string.share_chooser))
                            )
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.no_app_found), Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("📤 " + stringResource(R.string.card_share), color = Color(0xFF4CAF50), fontSize = 12.sp)
                    }
                }
            } else if (error != null) {
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.translation_retry), color = purplePrimary, fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = textGrayColor)
            }
        }
    )
}

// ============================================================
// Helpers
// ============================================================

private fun localizedLangName(code: String): String = when (code) {
    "fa" -> "فارسی"
    "en" -> "English"
    "ar" -> "العربية"
    "tr" -> "Türkçe"
    else -> code
}

@Composable
private fun localizedError(error: String): String = when (error) {
    "same_language" -> stringResource(R.string.translation_same_language)
    "empty_result" -> stringResource(R.string.translation_empty_result)
    else -> error
}
