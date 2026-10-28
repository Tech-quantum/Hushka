package com.techquantum.hushka

import androidx.compose.ui.platform.LocalContext

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
            Text(
                text = if (isRtl) "تکمیل متغیرهای پرامپت" else "Fill Prompt Variables",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { ttsHelper.speak(buildFinalText(), AppSettings.getLanguage(context)) }) {
                    Text("🔊", fontSize = 20.sp)
                }
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
