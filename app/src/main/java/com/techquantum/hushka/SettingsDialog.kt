package com.techquantum.hushka

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val textColor = MaterialTheme.colorScheme.onSurface
    val purplePrimary = MaterialTheme.colorScheme.primary

    var selectedTab by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold, color = textColor)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.close), color = purplePrimary, fontSize = 12.sp)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {

                // === Tabs ===
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = purplePrimary,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎨", fontSize = 13.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.settings_tab_appearance),
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📦", fontSize = 13.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.settings_tab_data),
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("ℹ️", fontSize = 13.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.settings_tab_about),
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))

                // === Tab Content ===
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedTab) {
                        0 -> AppearanceTab(
                            themeMode, colorTheme, language,
                            onThemeChange, onColorThemeChange, onLanguageChange
                        )
                        1 -> DataTab(
                            onExportJson, onExportMarkdown, onImportJson, onBrowseWeb
                        )
                        else -> AboutTab(onManageColors)
                    }
                }
            }
        },
        confirmButton = {}
    )
}

// ============================================================
// 🎨 Tab 1: Appearance
// ============================================================
@Composable
private fun AppearanceTab(
    themeMode: String,
    colorTheme: String,
    language: String,
    onThemeChange: (String) -> Unit,
    onColorThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    Text(
        stringResource(R.string.appearance),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = textColor
    )
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
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 16.sp)
                Spacer(Modifier.width(10.dp))
                Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp, color = textColor)
                RadioButton(selected = themeMode == value, onClick = { onThemeChange(value) })
            }
        }
    }

    Spacer(Modifier.height(4.dp))

    Text(
        stringResource(R.string.color_theme),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = textColor
    )
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
                        .size(40.dp)
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) textColor else Color.Transparent,
                            shape = CircleShape
                        ),
                    onClick = { onColorThemeChange(key) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            Text("✓", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = when (key) {
                        "purple" -> stringResource(R.string.color_purple)
                        "red" -> stringResource(R.string.color_red)
                        "green" -> stringResource(R.string.color_green)
                        "blue" -> stringResource(R.string.color_blue)
                        else -> stringResource(R.string.color_orange)
                    },
                    fontSize = 9.sp,
                    color = if (isSelected) purplePrimary else textGrayColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }

    Spacer(Modifier.height(4.dp))

    Text(
        stringResource(R.string.language),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = textColor
    )
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
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(flag, fontSize = 16.sp)
                Spacer(Modifier.width(10.dp))
                Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp, color = textColor)
                RadioButton(selected = language == value, onClick = { onLanguageChange(value) })
            }
        }
    }
}

// ============================================================
// 📦 Tab 2: Data
// ============================================================
@Composable
private fun DataTab(
    onExportJson: () -> Unit,
    onExportMarkdown: () -> Unit,
    onImportJson: () -> Unit,
    onBrowseWeb: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📦  " + stringResource(R.string.backup_title), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("☁  " + stringResource(R.string.cloud_title), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
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
}

// ============================================================
// ℹ️ Tab 3: About
// ============================================================
@Composable
private fun AboutTab(onManageColors: () -> Unit) {
    val cardColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🎨  " + stringResource(R.string.colors_title), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("💎 Hushka", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = purplePrimary)
            Text(
                stringResource(R.string.app_subtitle),
                fontSize = 11.sp,
                color = textGrayColor,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = purplePrimary.copy(alpha = 0.15f)
            ) {
                Text(
                    stringResource(R.string.about_version),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    fontSize = 10.sp,
                    color = purplePrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
