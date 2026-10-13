package com.techquantum.hushka

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
    isPinned: Boolean = false,
    isFavorite: Boolean = false,
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
                    title.ifBlank { stringResource(R.string.untitled) },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
                Spacer(Modifier.width(8.dp))
                Row {
                    // 👁 پیش‌نمایش — همیشه خاکستری (action)
                    EyeCircleButton(onClick = onPreviewClick)
                    Spacer(Modifier.width(6.dp))
                    // 📌 پین — عمودی خاکستری / کج بنفش
                    PinToggleButton(isPinned = isPinned, onClick = onPinToggle)
                    Spacer(Modifier.width(6.dp))
                    // ⭐ علاقه‌مندی — outline خاکستری / پر طلایی
                    StarToggleButton(isFavorite = isFavorite, onClick = onFavoriteToggle)
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
                            "${roleTemplate.emoji} ${roleTemplate.displayName()}",
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
                ActionButton(emoji = "📋", label = stringResource(R.string.copy), onClick = onCopyClick, color = purplePrimary)
                ActionButton(emoji = "📤", label = stringResource(R.string.card_share), onClick = onShareClick, color = Color(0xFF4CAF50))
                ActionButton(emoji = "✨", label = stringResource(R.string.card_analyze), onClick = onAnalyzeClick, color = Color(0xFFFFB74D))
                ActionButton(emoji = "▶", label = stringResource(R.string.card_test), onClick = onLiveTestClick, color = Color(0xFF64B5F6))
                ActionButton(emoji = "✏️", label = stringResource(R.string.edit), onClick = onEditClick, color = textGrayColor)
                ActionButton(emoji = "🗑", label = stringResource(R.string.delete), onClick = onDeleteClick, color = Color(0xFFE57373))
            }
        }
    }
}

// ============================================================
// 👁 دکمه پیش‌نمایش — همیشه خاکستری (action، نه toggle)
// ============================================================
@Composable
private fun EyeCircleButton(onClick: () -> Unit) {
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    val bgColor = MaterialTheme.colorScheme.surfaceVariant

    Surface(
        shape = CircleShape,
        color = bgColor,
        modifier = Modifier.size(36.dp),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================================
// 📌 دکمه پین — عمودی خاکستری / کج -45° بنفش
// ============================================================
@Composable
private fun PinToggleButton(isPinned: Boolean, onClick: () -> Unit) {
    val activeIconColor = Color(0xFFA78BFA)
    val activeBgColor = Color(0xFF7C3AED).copy(alpha = 0.22f)
    val activeBorderColor = Color(0xFFA78BFA).copy(alpha = 0.55f)

    val inactiveIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    val inactiveBgColor = MaterialTheme.colorScheme.surfaceVariant

    // 🎬 چرخش -45 درجه با انیمیشن spring
    val rotation by animateFloatAsState(
        targetValue = if (isPinned) -45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pinRotation"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isPinned) activeIconColor else inactiveIconColor,
        animationSpec = tween(300),
        label = "pinIconColor"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isPinned) activeBgColor else inactiveBgColor,
        animationSpec = tween(300),
        label = "pinBgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isPinned) activeBorderColor else Color.Transparent,
        animationSpec = tween(300),
        label = "pinBorderColor"
    )

    Surface(
        shape = CircleShape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.size(36.dp),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotation)
            )
        }
    }
}

// ============================================================
// ⭐ دکمه علاقه‌مندی — outline خاکستری / پر طلایی
// ============================================================
@Composable
private fun StarToggleButton(isFavorite: Boolean, onClick: () -> Unit) {
    val activeIconColor = Color(0xFFFFC107)
    val activeBgColor = Color(0xFFFFB300).copy(alpha = 0.22f)
    val activeBorderColor = Color(0xFFFFC107).copy(alpha = 0.55f)

    val inactiveIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    val inactiveBgColor = MaterialTheme.colorScheme.surfaceVariant

    val iconColor by animateColorAsState(
        targetValue = if (isFavorite) activeIconColor else inactiveIconColor,
        animationSpec = tween(300),
        label = "starIconColor"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFavorite) activeBgColor else inactiveBgColor,
        animationSpec = tween(300),
        label = "starBgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFavorite) activeBorderColor else Color.Transparent,
        animationSpec = tween(300),
        label = "starBorderColor"
    )

    Surface(
        shape = CircleShape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.size(36.dp),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
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
