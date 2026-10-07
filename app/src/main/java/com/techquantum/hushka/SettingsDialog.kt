package com.techquantum.hushka

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SettingsDialog(
    themeMode: String,
    language: String,
    onThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Appearance Section
                Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                listOf(
                    Triple("light", "☀", stringResource(R.string.light)),
                    Triple("dark", "☾", stringResource(R.string.dark)),
                    Triple("system", "⚙", stringResource(R.string.system))
                ).forEach { (value, icon, label) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (themeMode == value) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        onClick = { onThemeChange(value) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(icon, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.width(12.dp))
                            Text(label, modifier = Modifier.weight(1f))
                            RadioButton(selected = themeMode == value, onClick = { onThemeChange(value) })
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Language Section
                Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                listOf(
                    Triple("fa", "🇮🇷", stringResource(R.string.persian)),
                    Triple("en", "🇬🇧", stringResource(R.string.english)),
                    Triple("ar", "🇸🇦", stringResource(R.string.arabic)),
                    Triple("tr", "🇹🇷", stringResource(R.string.turkish))
                ).forEach { (value, flag, label) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (language == value) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        onClick = { onLanguageChange(value) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(flag, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.width(12.dp))
                            Text(label, modifier = Modifier.weight(1f))
                            RadioButton(selected = language == value, onClick = { onLanguageChange(value) })
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Local Backup & Data Independence (Based on Image 5)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("پشتیبان‌گیری و استقلال داده‌های محلی", fontWeight = FontWeight.Bold)
                        }
                        Text("استخراج کل آرشیو پرامپت‌ها در قالب یک فایل استاندارد JSON و امکان بازیابی آن در هر زمان، بدون نیاز به سرور ابری.", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { /* TODO */ }, modifier = Modifier.weight(1f)) {
                                Text("خروجی Markdown")
                            }
                            Button(onClick = { /* TODO */ }, modifier = Modifier.weight(1f)) {
                                Text("خروجی کامل JSON")
                            }
                        }
                        OutlinedButton(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
                            Text("درون‌ریزی فایل پشتیبان (JSON)")
                        }
                    }
                }

                // Cloud Update (Based on Image 5)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("بروزرسانی ابری از اینترنت", fontWeight = FontWeight.Bold)
                            Text("کاوش آنلاین مخازن هوش مصنوعی، پرامپت‌ها و گیت‌هاب", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = { /* TODO */ }) {
                            Text("کاوش در وب")
                        }
                    }
                }

                // Tag Coloring (Based on Image 5)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("رنگ‌بندی تگ‌ها", fontWeight = FontWeight.Bold)
                            Text("مدیریت و شخصی‌سازی رنگ موضوعات و برچسب‌ها", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = { /* TODO */ }) {
                            Text("مدیریت رنگ‌ها")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.app_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}
