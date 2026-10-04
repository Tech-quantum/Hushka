package com.techquantum.promptvault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
                Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}
