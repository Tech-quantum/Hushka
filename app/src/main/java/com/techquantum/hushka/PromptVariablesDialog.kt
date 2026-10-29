package com.techquantum.hushka
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PromptVariablesDialog(
    prompt: PromptEntity,
    isRtl: Boolean,
    onConfirm: (String, String) -> Unit, // (finalText, action)
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showModelMenu by remember { mutableStateOf(false) }
    val text = prompt.text
    val allVars = PromptVariables.extract(text)

    val values = remember { mutableStateMapOf<String, String>() }

    fun buildFinalText(): String {
        var result = text
        values.forEach { (key, value) ->
            result = result.replace("[$key]", value).replace("{$key}", value)
        }
        return result
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isRtl) "تکمیل متغیرهای پرامپت" else "Fill Prompt Variables",
                    fontWeight = FontWeight.Bold
                )
                Box {
                    IconButton(onClick = { showModelMenu = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Select Model")
                    }
                    DropdownMenu(
                        expanded = showModelMenu,
                        onDismissRequest = { showModelMenu = false }
                    ) {
                        val providers = listOf(
                            "gemini" to "Gemini 💎",
                            "openai" to "OpenAI 🟢",
                            "deepseek" to "DeepSeek 🟣",
                            "groq" to "Groq ⚡",
                            "anthropic" to "Anthropic 🟠",
                            "mistral" to "Mistral 🌪",
                            "xai" to "XAI 🚀",
                            "local" to "بومی 🏠"
                        )
                        providers.forEach { (id, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    AppSettings.setAiProvider(context, id)
                                    showModelMenu = false
                                    Toast.makeText(context, "مدل: $label", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isRtl) "این پرامپت دارای متغیر است. برای بهترین نتیجه، آن‌ها را تکمیل کنید." else "This prompt has variables. Fill them in for best results.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                allVars.forEach { v ->
                    OutlinedTextField(
                        value = values[v] ?: "",
                        onValueChange = { values[v] = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("[${getLocalizedVarName(v)}]", fontSize = 12.sp) },
                        placeholder = { Text(if (isRtl) "مقدار را وارد کنید..." else "Enter value...", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onConfirm(buildFinalText(), "copy") },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isRtl) "کپی 📋" else "Copy 📋", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onConfirm(buildFinalText(), "chat") },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isRtl) "چت 💬" else "Chat 💬", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isRtl) "لغو" else "Cancel")
            }
        }
    )
}
