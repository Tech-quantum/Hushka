package com.techquantum.hushka

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

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
    lastUsedAt: Long = 0L,
    onFavoriteToggle: () -> Unit,
    onPinToggle: () -> Unit,
    onCardClick: () -> Unit,
    onPreviewClick: () -> Unit,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onTranslateClick: () -> Unit = {},
    onAnalyzeClick: () -> Unit,
    onLiveTestClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSwipeToDelete: () -> Unit = {},
    itemIndex: Int = 0
) {
    var displayTitle by remember(title) { mutableStateOf(title) }
    
    
    }

    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    val roleTemplate = if (roleId.isNotBlank()) RoleTemplates.findById(roleId) else null

    // 🎨 رنگ اختصاصی دسته
    val accentColor = remember(mainTag, purplePrimary) {
        getCategoryColor(mainTag, purplePrimary)
    }

    // 🏷️ رنگ‌های سفارشی تگ‌ها (از AppSettings)
    val appContext = LocalContext.current
    val tagColors = remember(tags) {
        AppSettings.getAllTagColors(appContext)
    }

    // 🌐 ترجمه خودکار عنوان بر اساس زبان اپ
    val appLang = remember(appContext) { AppSettings.getLanguage(appContext) }
    LaunchedEffect(title, appLang) {
        if (title.isNotBlank()) {
            displayTitle = TitleTranslationCache.translate(title, appLang)
        }
    }

    // 📊 زمان آخرین استفاده
    val metadataText = remember(lastUsedAt) { formatLastUsed(lastUsedAt) }

    // 🎬 انیمیشن press
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "cardScale"
    )

    // ✨ انیمیشن ورود (فقط بار اول)
    var hasAnimated by rememberSaveable { mutableStateOf(false) }
    val entrance = remember { Animatable(if (hasAnimated) 1f else 0f) }

    LaunchedEffect(Unit) {
        if (!hasAnimated) {
            delay(itemIndex * 40L)
            entrance.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 380,
                    easing = FastOutSlowInEasing
                )
            )
            hasAnimated = true
        }
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipeToDelete()
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isSwiping = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val bgColor by animateColorAsState(
                targetValue = if (isSwiping) Color(0xFFE53935) else Color.Transparent,
                animationSpec = tween(200),
                label = "swipeBg"
            )
            val iconTint by animateColorAsState(
                targetValue = if (isSwiping) Color.White else Color.Transparent,
                animationSpec = tween(200),
                label = "swipeIcon"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .background(bgColor, RoundedCornerShape(22.dp))
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "حذف",
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val baseScale = scale
                val entranceScale = 0.85f + (0.15f * entrance.value)
                scaleX = baseScale * entranceScale
                scaleY = baseScale * entranceScale
                alpha = entrance.value
                translationY = (1f - entrance.value) * 40f
            }
    ) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onCardClick,
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.07f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp)
        ) {
            // ===== عنوان + دکمه‌های بالا =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    displayTitle.ifBlank { stringResource(R.string.untitled) },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
                Spacer(Modifier.width(8.dp))
                Row {
                    EyeCircleButton(onClick = onPreviewClick)
                    Spacer(Modifier.width(6.dp))
                    PinToggleButton(isPinned = isPinned, onClick = onPinToggle)
                    Spacer(Modifier.width(6.dp))
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
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            getLocalizedCategory(mainTag),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = accentColor
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

            // ===== تگ‌ها (رنگی با اولویت سفارشی) =====
            if (tags.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(tags) { tag ->
                        // اگه رنگ سفارشی داره، از اون استفاده کن؛ وگرنه accentColor
                        val tagColor = tagColors[tag]?.let { parseHexColor(it) } ?: accentColor
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = tagColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                tag,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 11.sp,
                                color = tagColor,
                                fontWeight = FontWeight.Medium
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

            Spacer(Modifier.height(12.dp))

            // ===== Metadata: زمان + provider =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (metadataText != null) {
                    Text(
                        "⏱  $metadataText",
                        fontSize = 10.sp,
                        color = textGrayColor
                    )
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = purplePrimary.copy(alpha = 0.30f)
                ) {
                    Text(
                        provider,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            HorizontalDivider(color = cardLightColor.copy(alpha = 0.5f))

            Spacer(Modifier.height(8.dp))

            // ===== ردیف دکمه‌های عملیات =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(emoji = "📋", label = stringResource(R.string.copy), onClick = onCopyClick, color = purplePrimary)
                ActionButton(emoji = "📤", label = stringResource(R.string.card_share), onClick = onShareClick, color = Color(0xFF4CAF50))
                ActionButton(emoji = "🌐", label = stringResource(R.string.translate_prompt), onClick = onTranslateClick, color = Color(0xFF9C27B0))
                ActionButton(emoji = "✨", label = stringResource(R.string.card_analyze), onClick = onAnalyzeClick, color = Color(0xFFFFB74D))
                ActionButton(emoji = "▶", label = stringResource(R.string.card_test), onClick = onLiveTestClick, color = Color(0xFF64B5F6))
                ActionButton(emoji = "✏️", label = stringResource(R.string.edit), onClick = onEditClick, color = textGrayColor)
                ActionButton(emoji = "🗑", label = stringResource(R.string.delete), onClick = onDeleteClick, color = Color(0xFFE57373))
            }
        }
    }
    }
}

// ============================================================
// 🎨 رنگ اختصاصی هر دسته
// ============================================================
private fun getCategoryColor(category: String, fallback: Color): Color {
    val domain = category.substringBefore('.').lowercase()
    return when (domain) {
        "ai" -> Color(0xFF8B5CF6)          // بنفش
        "analysis" -> Color(0xFF6366F1)    // نیلی
        "business" -> Color(0xFFF59E0B)    // کهربایی
        "career" -> Color(0xFF14B8A6)      // فیروزه‌ای
        "code", "coding" -> Color(0xFF3B82F6) // آبی
        "data" -> Color(0xFF06B6D4)        // سایان
        "fun" -> Color(0xFFEC4899)         // صورتی
        "image" -> Color(0xFFEF4444)       // قرمز
        "life" -> Color(0xFF10B981)        // زمرد
        "logo" -> Color(0xFFA855F7)        // بنفش روشن
        "marketing" -> Color(0xFFF97316)   // نارنجی
        "prompting" -> Color(0xFF6366F1)   // نیلی
        "seo" -> Color(0xFFFBBF24)         // زرد
        "social" -> Color(0xFF0EA5E9)      // آبی آسمانی
        "study" -> Color(0xFF8B5CF6)       // بنفش
        "text", "writing" -> Color(0xFF10B981) // سبز
        "video" -> Color(0xFFDC2626)       // قرمز تیره
        "other" -> Color(0xFF6B7280)       // خاکستری
        else -> fallback
    }
}

// ============================================================
// 📊 فرمت زمان آخرین استفاده
// ============================================================
private fun parseHexColor(hex: String): Color? {
    return try {
        val cleaned = hex.removePrefix("#")
        val colorInt = cleaned.toLong(16)
        Color(0xFF000000 or colorInt)
    } catch (e: Exception) {
        null
    }
}

private fun formatLastUsed(lastUsedAt: Long): String? {
    if (lastUsedAt <= 0L) return null
    val diff = System.currentTimeMillis() - lastUsedAt
    val minutes = diff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "همین حالا"
        minutes < 60 -> "$minutes دقیقه پیش"
        hours < 24 -> "$hours ساعت پیش"
        days == 1L -> "دیروز"
        days < 7 -> "$days روز پیش"
        days < 30 -> "${days / 7} هفته پیش"
        days < 365 -> "${days / 30} ماه پیش"
        else -> "${days / 365} سال پیش"
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
