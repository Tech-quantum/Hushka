package com.techquantum.hushka

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * پالت رنگ‌های قابل انتخاب برای تگ‌ها
 */
private val TAG_PALETTE = listOf(
    "#7C7CFF", // بنفش
    "#FF5252", // قرمز
    "#4CAF50", // سبز
    "#42A5F5", // آبی
    "#FF9800", // نارنجی
    "#FFC107", // زرد
    "#26C6DA", // فیروزه‌ای
    "#EC407A", // صورتی
    "#9E9E9E", // خاکستری
    "#8B5CF6", // بنفش تیره
    "#10B981", // زمرد
    "#F97316"  // نارنجی تیره
)

@Composable
fun TagColorManagerDialog(
    allTags: List<String>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    // بارگذاری رنگ‌های ذخیره‌شده
    var tagColors by remember { mutableStateOf(AppSettings.getAllTagColors(context)) }

    // تگی که کاربر می‌خواد رنگش رو انتخاب کنه
    var selectedTagForPick by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            color = cardColor
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ===== Top Bar =====
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            stringResource(R.string.close),
                            fontSize = 13.sp,
                            color = purplePrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "🎨 " + stringResource(R.string.tag_colors_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            stringResource(R.string.tag_colors_hint),
                            fontSize = 10.sp,
                            color = textGrayColor
                        )
                    }
                }

                HorizontalDivider(color = cardLightColor.copy(alpha = 0.5f))

                // ===== Content =====
                if (allTags.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏷", fontSize = 40.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.tag_colors_empty),
                                color = textGrayColor,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allTags.sorted(), key = { it }) { tag ->
                            val colorHex = tagColors[tag]
                            val tagColor = if (colorHex != null) {
                                parseColor(colorHex) ?: purplePrimary
                            } else {
                                purplePrimary
                            }
                            val hasCustomColor = colorHex != null

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = cardLightColor,
                                onClick = { selectedTagForPick = tag }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // دایره رنگ
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(tagColor)
                                            .border(
                                                width = if (hasCustomColor) 2.dp else 1.dp,
                                                color = if (hasCustomColor) textColor else textGrayColor.copy(alpha = 0.3f),
                                                shape = CircleShape
                                            )
                                    )

                                    Spacer(Modifier.width(12.dp))

                                    Text(
                                        "#$tag",
                                        modifier = Modifier.weight(1f),
                                        fontSize = 13.sp,
                                        color = textColor,
                                        fontWeight = if (hasCustomColor) FontWeight.Medium else FontWeight.Normal
                                    )

                                    if (!hasCustomColor) {
                                        Text(
                                            stringResource(R.string.tag_color_default),
                                            fontSize = 10.sp,
                                            color = textGrayColor
                                        )
                                    }
                                }
                            }
                        }

                        // ===== Reset All Button =====
                        item {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    AppSettings.resetAllTagColors(context)
                                    tagColors = emptyMap()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "🔄  " + stringResource(R.string.tag_color_reset_all),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ===== Color Picker Dialog =====
    selectedTagForPick?.let { tag ->
        ColorPickerDialog(
            tagName = tag,
            currentColorHex = tagColors[tag],
            onColorSelected = { colorHex ->
                if (colorHex == null) {
                    // حذف رنگ (بازگشت به پیش‌فرض)
                    val updated = tagColors.toMutableMap()
                    updated.remove(tag)
                    AppSettings.resetAllTagColors(context)
                    updated.forEach { (k, v) -> AppSettings.setTagColor(context, k, v) }
                    tagColors = updated
                } else {
                    AppSettings.setTagColor(context, tag, colorHex)
                    tagColors = tagColors.toMutableMap().apply { put(tag, colorHex) }
                }
                selectedTagForPick = null
            },
            onDismiss = { selectedTagForPick = null }
        )
    }
}

// ============================================================
// 🎨 Color Picker Dialog
// ============================================================
@Composable
private fun ColorPickerDialog(
    tagName: String,
    currentColorHex: String?,
    onColorSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Text(
                "${stringResource(R.string.tag_color_pick_for)} #$tagName",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // پالت رنگ‌ها به صورت Grid
                val columns = 4
                TAG_PALETTE.chunked(columns).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { hex ->
                            val color = parseColor(hex) ?: purplePrimary
                            val isSelected = currentColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .padding(6.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) textColor else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { onColorSelected(hex) }
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✓", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        // پر کردن فضای خالی اگه ردیف ناقصه
                        repeat(columns - row.size) {
                            Box(modifier = Modifier.size(52.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Spacer(Modifier.height(8.dp))

                // دکمه حذف رنگ (پیش‌فرض)
                OutlinedButton(
                    onClick = { onColorSelected(null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "🗑  " + stringResource(R.string.tag_color_default),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = textGrayColor)
            }
        }
    )
}

// ============================================================
// 🔧 Helper: parse hex to Color
// ============================================================
private fun parseColor(hex: String): Color? {
    return try {
        val cleaned = hex.removePrefix("#")
        val colorInt = cleaned.toLong(16)
        Color(0xFF000000 or colorInt)
    } catch (e: Exception) {
        null
    }
}
