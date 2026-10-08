package com.techquantum.hushka

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WebPromptsDialog(
    prompts: List<PromptEntity>,
    isLoading: Boolean,
    error: String?,
    onImport: (List<PromptEntity>) -> Unit,
    onDismiss: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    val selected = remember { mutableStateListOf<PromptEntity>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Column {
                Text("واکشی پرامپت از وب", color = textColor, fontWeight = FontWeight.Bold)
                Text(
                    "مخزن awesome-chatgpt-prompts",
                    color = textGrayColor,
                    fontSize = 11.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = purplePrimary)
                                Spacer(Modifier.height(16.dp))
                                Text("در حال دانلود از GitHub...", color = textGrayColor, fontSize = 13.sp)
                            }
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚠️", fontSize = 40.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("خطا در دانلود پرامپت‌ها", color = textColor, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(error, color = textGrayColor, fontSize = 11.sp)
                            }
                        }
                    }
                    prompts.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("پرامپتی پیدا نشد", color = textGrayColor)
                        }
                    }
                    else -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${prompts.size} پرامپت • ${selected.size} انتخاب شده",
                                color = textGrayColor,
                                fontSize = 12.sp
                            )
                            TextButton(
                                onClick = {
                                    if (selected.size == prompts.size) {
                                        selected.clear()
                                    } else {
                                        selected.clear()
                                        selected.addAll(prompts)
                                    }
                                }
                            ) {
                                Text(
                                    if (selected.size == prompts.size) "لغو همه" else "انتخاب همه",
                                    color = purplePrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(prompts) { prompt ->
                                val isSelected = selected.any { it.title == prompt.title && it.text == prompt.text }
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) purplePrimary.copy(alpha = 0.25f) else cardLightColor,
                                    onClick = {
                                        if (isSelected) {
                                            selected.removeAll { it.title == prompt.title && it.text == prompt.text }
                                        } else {
                                            selected.add(prompt)
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                if (checked == true) {
                                                    if (!selected.any { it.title == prompt.title && it.text == prompt.text }) {
                                                        selected.add(prompt)
                                                    }
                                                } else {
                                                    selected.removeAll { it.title == prompt.title && it.text == prompt.text }
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = purplePrimary)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                prompt.title,
                                                color = textColor,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                prompt.text,
                                                color = textGrayColor,
                                                fontSize = 11.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(selected.toList()) },
                enabled = !isLoading && prompts.isNotEmpty() && error == null && selected.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = purplePrimary,
                    contentColor = Color.White,
                    disabledContainerColor = cardLightColor,
                    disabledContentColor = textGrayColor
                )
            ) {
                Text("افزودن ${selected.size} پرامپت")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن", color = textGrayColor)
            }
        }
    )
}
