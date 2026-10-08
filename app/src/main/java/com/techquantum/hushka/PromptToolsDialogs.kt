package com.techquantum.hushka

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==================== ANALYZE DIALOG ====================

@Composable
fun AnalyzeDialog(
    prompt: PromptEntity,
    onDismiss: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    val text = prompt.text
    val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
    val chars = text.length
    val lines = text.split("\n").size

    val bracketVars = Regex("\\[([A-Z_]+)\\]").findAll(text).map { it.groupValues[1] }.toList().distinct()
    val curlyVars = Regex("\\{([^}]+)\\}").findAll(text).map { it.groupValues[1] }.toList().distinct()
    val allVars = (bracketVars + curlyVars).distinct()

    val score = calculateScore(text, words.size, allVars.size)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Text("✨ آنالیز پرامپت", color = textColor, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = purplePrimary.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "$score",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = purplePrimary
                        )
                        Text(
                            "امتیاز کیفیت از ۱۰۰",
                            fontSize = 11.sp,
                            color = textGrayColor
                        )
                    }
                }

                StatRow("📝 تعداد کلمات", "${words.size}", textColor, textGrayColor)
                StatRow("🔤 تعداد کاراکتر", "$chars", textColor, textGrayColor)
                StatRow("📄 تعداد خطوط", "$lines", textColor, textGrayColor)
                if (prompt.category.isNotBlank())
                    StatRow("📁 دسته‌بندی", getLocalizedCategory(prompt.category), textColor, textGrayColor)
                if (prompt.collection.isNotBlank())
                    StatRow("🏷 ارائه‌دهنده", prompt.collection, textColor, textGrayColor)

                if (allVars.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "🔧 متغیرهای شناسایی شده (${allVars.size})",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    allVars.forEach { v ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = cardLightColor,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                "[$v]",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                color = purplePrimary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "💡 توصیه‌ها",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                val tips = buildList {
                    if (words.size < 20) add("• پرامپت کوتاهه، جزئیات بیشتری اضافه کن")
                    if (words.size > 500) add("• پرامپت خیلی طولانیه، سعی کن خلاصه‌تر کنی")
                    if (allVars.isEmpty() && !text.contains("You are", true)) add("• از متغیرهایی مثل [SUBJECT] استفاده کن")
                    if (prompt.category.isBlank()) add("• دسته‌بندی رو تنظیم کن")
                    if (prompt.tags.isBlank()) add("• برچسب‌های مرتبط اضافه کن")
                    if (text.contains("You are", true)) add("✅ نقش (Role) تعریف شده")
                    if (text.contains("step", true) || text.contains("format", true)) add("✅ ساختار خروجی مشخصه")
                    if (isEmpty()) add("✅ پرامپت ساختار خوبی داره!")
                }

                tips.forEach { tip ->
                    Text(tip, color = textGrayColor, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن", color = purplePrimary)
            }
        }
    )
}

private fun calculateScore(text: String, wordCount: Int, varCount: Int): Int {
    var score = 30
    if (wordCount > 20) score += 15
    if (wordCount > 50) score += 10
    if (wordCount > 100) score += 5
    if (wordCount > 500) score -= 10
    if (text.contains("You are", true)) score += 10
    if (text.contains("role", true)) score += 5
    if (text.contains("format", true) || text.contains("structure", true)) score += 10
    if (text.contains("example", true)) score += 5
    if (varCount > 0) score += 10
    if (varCount > 3) score += 5
    if (text.contains("step", true)) score += 5
    return score.coerceIn(0, 100)
}

@Composable
private fun StatRow(label: String, value: String, textColor: Color, textGrayColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = textGrayColor, fontSize = 12.sp)
        Text(value, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

// ==================== LIVE TEST DIALOG ====================

@Composable
fun LiveTestDialog(
    prompt: PromptEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    val bracketVars = Regex("\\[([A-Z_]+)\\]").findAll(prompt.text).map { it.groupValues[1] }.toList().distinct()
    val curlyVars = Regex("\\{([^}]+)\\}").findAll(prompt.text).map { it.groupValues[1] }.toList().distinct()
    val allVars = (bracketVars + curlyVars).distinct()

    val values = remember { mutableStateMapOf<String, String>() }

    fun buildFinalText(): String {
        var result = prompt.text
        values.forEach { (key, value) ->
            result = result.replace("[$key]", value).replace("{$key}", value)
        }
        return result
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Column {
                Text("▶ تست زنده", color = textColor, fontWeight = FontWeight.Bold)
                Text(
                    "متغیرها را پر کن و اجرا بگیر",
                    color = textGrayColor,
                    fontSize = 11.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (allVars.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = cardLightColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "این پرامپت متغیری نداره. متن آماده اجراست.",
                            modifier = Modifier.padding(12.dp),
                            color = textGrayColor,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text(
                        "🔧 ${allVars.size} متغیر پیدا شد. مقادیر رو پر کن:",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    allVars.forEach { v ->
                        OutlinedTextField(
                            value = values[v] ?: "",
                            onValueChange = { values[v] = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("[$v]", fontSize = 11.sp) },
                            placeholder = { Text("مقدار $v", fontSize = 11.sp, color = textGrayColor) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = purplePrimary,
                                unfocusedBorderColor = textGrayColor.copy(alpha = 0.4f),
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedLabelColor = purplePrimary,
                                unfocusedLabelColor = textGrayColor
                            )
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "📄 پیش‌نمایش نهایی:",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = cardLightColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        buildFinalText(),
                        modifier = Modifier.padding(12.dp),
                        color = textColor,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalText = buildFinalText()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Hushka Prompt", finalText))
                    Toast.makeText(context, "کپی شد! آماده اجرا", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = purplePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("📋 کپی متن نهایی", fontSize = 12.sp)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    val finalText = buildFinalText()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Hushka Prompt", finalText))
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.openai.com"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "مرورگر پیدا نشد", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("🌐 ChatGPT", color = textGrayColor, fontSize = 11.sp)
                }
                TextButton(onClick = onDismiss) {
                    Text("بستن", color = textGrayColor, fontSize = 12.sp)
                }
            }
        }
    )
}
