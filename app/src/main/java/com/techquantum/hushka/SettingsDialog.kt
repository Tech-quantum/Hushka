package com.techquantum.hushka

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsDialog(
    themeMode: String,
    colorTheme: String,
    language: String,
    onThemeChange: (String) -> Unit,
    onColorThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onExportJson: () -> Unit,
    onExportMarkdown: () -> Unit,
    onImportJson: () -> Unit,
    onBrowseWeb: () -> Unit,
    onManageColors: () -> Unit,
    onDismiss: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold, color = textColor) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ===== Appearance =====
                Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                listOf(
                    Triple("light", "☀", stringResource(R.string.light)),
                    Triple("dark", "☾", stringResource(R.string.dark)),
                    Triple("system", "⚙", stringResource(R.string.system))
                ).forEach { (value, icon, label) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (themeMode == value) purplePrimary.copy(alpha = 0.35f) else cardColor,
                        onClick = { onThemeChange(value) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(icon, fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, color = textColor)
                            RadioButton(selected = themeMode == value, onClick = { onThemeChange(value) })
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ===== Color Theme =====
                Text(stringResource(R.string.color_theme), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    val colorOptions = listOf(
                        "purple" to Color(0xFF7C7CFF),
                        "red" to Color(0xFFFF5252),
                        "green" to Color(0xFF4CAF50),
                        "blue" to Color(0xFF42A5F5),
                        "orange" to Color(0xFFFF9800)
                    )
                    colorOptions.forEach { (key, color) ->
                        val isSelected = colorTheme == key
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = color,
                                modifier = Modifier
                                    .size(44.dp)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) textColor else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                onClick = { onColorThemeChange(key) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isSelected) {
                                        Text("✓", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = when (key) {
                                    "purple" -> stringResource(R.string.color_purple)
                                    "red" -> stringResource(R.string.color_red)
                                    "green" -> stringResource(R.string.color_green)
                                    "blue" -> stringResource(R.string.color_blue)
                                    else -> stringResource(R.string.color_orange)
                                },
                                fontSize = 10.sp,
                                color = if (isSelected) purplePrimary else textGrayColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ===== Language =====
                Text(stringResource(R.string.language), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                listOf(
                    Triple("fa", "🇮🇷", stringResource(R.string.persian)),
                    Triple("en", "🇬🇧", stringResource(R.string.english)),
                    Triple("ar", "🇸🇦", stringResource(R.string.arabic)),
                    Triple("tr", "🇹🇷", stringResource(R.string.turkish))
                ).forEach { (value, flag, label) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (language == value) purplePrimary.copy(alpha = 0.35f) else cardColor,
                        onClick = { onLanguageChange(value) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(flag, fontSize = 16.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, color = textColor)
                            RadioButton(selected = language == value, onClick = { onLanguageChange(value) })
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ===== Backup Card =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📦  " + stringResource(R.string.backup_title), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                        Text(
                            stringResource(R.string.backup_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = textGrayColor,
                            fontSize = 11.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onExportJson,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = purplePrimary)
                            ) {
                                Text(stringResource(R.string.export_json), fontSize = 11.sp, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = onExportMarkdown,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(stringResource(R.string.export_markdown), fontSize = 11.sp)
                            }
                        }
                        OutlinedButton(
                            onClick = onImportJson,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(stringResource(R.string.import_json), fontSize = 11.sp)
                        }
                    }
                }

                // ===== Cloud Card =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("☁  " + stringResource(R.string.cloud_title), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                        Text(
                            stringResource(R.string.cloud_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = textGrayColor,
                            fontSize = 11.sp
                        )
                        Button(
                            onClick = onBrowseWeb,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = purplePrimary)
                        ) {
                            Text(stringResource(R.string.browse_web), fontSize = 12.sp, color = Color.White)
                        }
                    }
                }

                // ===== Manage Colors Card =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🎨  " + stringResource(R.string.colors_title), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                        Text(
                            stringResource(R.string.colors_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = textGrayColor,
                            fontSize = 11.sp
                        )
                        Button(
                            onClick = onManageColors,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = purplePrimary)
                        ) {
                            Text(stringResource(R.string.manage_colors), fontSize = 12.sp, color = Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.app_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = textGrayColor
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = purplePrimary)
            }
        }
    )
}
