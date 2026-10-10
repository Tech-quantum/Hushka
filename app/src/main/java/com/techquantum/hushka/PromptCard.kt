package com.techquantum.hushka

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PromptCard(
    title: String,
    description: String,
    mainTag: String,
    tags: List<String>,
    provider: String,
    roleId: String = "",
    onFavoriteToggle: () -> Unit,
    onPinToggle: () -> Unit,
    onCardClick: () -> Unit,
    onPreviewClick: () -> Unit,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onLiveTestClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    val roleTemplate = if (roleId.isNotBlank()) RoleTemplates.findById(roleId) else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onCardClick
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // ===== عنوان + دکمه‌های بالا =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    title.ifBlank { "بدون عنوان" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
                Spacer(Modifier.width(8.dp))
                Row {
                    // چشم (پیش‌نمایش)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = cardLightColor,
                        modifier = Modifier.size(32.dp),
                        onClick = onPreviewClick
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👁", fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    // پین
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = cardLightColor,
                        modifier = Modifier.size(32.dp),
                        onClick = onPinToggle
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📌", fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    // ستاره
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = cardLightColor,
                        modifier = Modifier.size(32.dp),
                        onClick = onFavoriteToggle
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⭐", fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ===== برچسب دسته‌بندی + نقش =====
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mainTag.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = purplePrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            getLocalizedCategory(mainTag),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            color = purplePrimary
                        )
                    }
                }
                if (roleTemplate != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFB74D).copy(alpha = 0.15f)
                    ) {
                        Text(
                            "${roleTemplate.emoji} ${roleTemplate.nameFa}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            color = Color(0xFFFFB74D)
                        )
                    }
                }
            }

            if (mainTag.isNotBlank() || roleTemplate != null) {
                Spacer(Modifier.height(10.dp))
            }

            // ===== تگ‌ها =====
            if (tags.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(tags) { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = cardLightColor
                        ) {
                            Text(
                                tag,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 11.sp,
                                color = textColor
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            // ===== توضیحات =====
            Text(
                description,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = textGrayColor
            )

            Spacer(Modifier.height(14.dp))

            // ===== ارائه‌دهنده =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = purplePrimary.copy(alpha = 0.3f)
                ) {
                    Text(
                        provider,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            HorizontalDivider(color = cardLightColor.copy(alpha = 0.5f))

            Spacer(Modifier.height(10.dp))

            // ===== ردیف دکمه‌های عملیات =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(emoji = "📋", label = "کپی", onClick = onCopyClick, color = purplePrimary)
                ActionButton(emoji = "📤", label = "اشتراک", onClick = onShareClick, color = Color(0xFF4CAF50))
                ActionButton(emoji = "✨", label = "آنالیز", onClick = onAnalyzeClick, color = Color(0xFFFFB74D))
                ActionButton(emoji = "▶", label = "تست", onClick = onLiveTestClick, color = Color(0xFF64B5F6))
                ActionButton(emoji = "✏️", label = "ویرایش", onClick = onEditClick, color = textGrayColor)
                ActionButton(emoji = "🗑", label = "حذف", onClick = onDeleteClick, color = Color(0xFFE57373))
            }
        }
    }
}

@Composable
fun ActionButton(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(38.dp)
        ) {
            Text(emoji, fontSize = 16.sp)
        }
        Text(
            label,
            fontSize = 9.sp,
            color = color,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
