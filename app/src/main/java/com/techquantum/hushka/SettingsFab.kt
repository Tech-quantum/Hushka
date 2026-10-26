package com.techquantum.hushka

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** اندازه‌ی خود دکمه (استاندارد Material). */
private val FabButtonSize = 56.dp

/**
 * جای‌گیری کل دکمه روی صفحه = دکمه + فاصله‌ی حلقه‌های تزئینی.
 * حلقه‌ها بیرون از دکمه رسم می‌شوند تا روی خود دکمه نیفتند.
 */
private val FabFootprint = 68.dp

/**
 * FAB تنظیمات، قابل جابه‌جایی در فضای محتوا؛ نه روی هدر و نه روی فوتر.
 *
 * سه لایه‌ی بصری دارد:
 * ۱. دو حلقه‌ی ثابت دور دکمه (کمک به دیده‌شدن روی پس‌زمینه‌ی شلوغ)
 * ۲. یک sweep چرخشی کوتاه فقط در اولین اجرا (هینت کشف‌پذیری، ۱.۵ ثانیه / دو دور)
 * ۳. سایه‌ی رنگی و بزرگ‌شدن ملایم دکمه فقط در حین درگ
 */
@Composable
fun DraggableSettingsFab(
    onClick: () -> Unit,
    containerWidth: Float,
    containerHeight: Float,
    topBoundary: Float,
    bottomInset: Float,
    initialFraction: Pair<Float, Float>?,
    showFirstRunHint: Boolean,
    onHintShown: () -> Unit,
    onCommit: (Float, Float) -> Unit,
    resetKey: Int = 0,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val footprintPx = with(LocalDensity.current) { FabFootprint.toPx() }
    // حاشیه‌ی ۱۲dp از لبه‌های چپ/راست: گوشه‌ی صفحه در ناوبری ژستی اندروید
    // ناحیه‌ی «برگشت» است و دکمه‌ی چسبیده به لبه، درگ را با swipe سیستم
    // قاطی می‌کند. این حاشیه هم از آن جلوگیری می‌کند و هم از چسبیدن
    // حلقه‌ی تزئینی به لبه‌ی نمایشگر جلوگیری می‌کند.
    val edgeMarginPx = with(LocalDensity.current) { 12.dp.toPx() }
    val maxX = (containerWidth - footprintPx - edgeMarginPx).coerceAtLeast(0f)
    val minY = (topBoundary + edgeMarginPx).coerceAtLeast(0f)
    val maxY = (containerHeight - bottomInset - footprintPx - edgeMarginPx).coerceAtLeast(minY)

    var position by remember(containerWidth, containerHeight, topBoundary, bottomInset, resetKey) {
        mutableStateOf(initialFabPosition(initialFraction, maxX, minY, maxY))
    }
    var dragging by remember { mutableStateOf(false) }

    // پالس سایه فقط هنگام درگ
    val glowElevation by animateDpAsState(
        targetValue = if (dragging) 14.dp else 4.dp,
        label = "settingsFabGlow"
    )
    val dragScale by animateFloatAsState(
        targetValue = if (dragging) 1.06f else 1f,
        label = "settingsFabScale"
    )

    // هینت چرخشی یک‌باره؛ اگر کاربر انیمیشن‌ها را در سیستم خاموش کرده باشد، فقط ثبت می‌شود
    val sweep = remember { Animatable(0f) }
    val motionAllowed = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) > 0f
    }
    LaunchedEffect(showFirstRunHint) {
        if (!showFirstRunHint) return@LaunchedEffect
        if (motionAllowed) {
            sweep.animateTo(
                targetValue = 720f,
                animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
            )
        }
        onHintShown()
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(position.first.roundToInt(), position.second.roundToInt()) }
            .size(FabFootprint)
            .drawBehind {
                val outerWidth = 1.2.dp.toPx()
                val outerRadius = size.minDimension / 2f - outerWidth
                val innerRadius = outerRadius - 3.dp.toPx()

                drawCircle(
                    color = primary.copy(alpha = 0.18f),
                    radius = outerRadius,
                    style = Stroke(outerWidth)
                )
                drawCircle(
                    color = primary.copy(alpha = 0.42f),
                    radius = innerRadius,
                    style = Stroke(1.6.dp.toPx())
                )

                if (showFirstRunHint) {
                    val stroke = 2.6.dp.toPx()
                    val arcSize = outerRadius * 2f - stroke
                    val topLeft = Offset(
                        x = (size.width - arcSize) / 2f,
                        y = (size.height - arcSize) / 2f
                    )
                    // دنباله‌ی کم‌رنگ + سر پررنگ = حس نور در حال چرخش
                    rotate(sweep.value, center) {
                        drawArc(
                            color = primary.copy(alpha = 0.35f),
                            startAngle = -96f,
                            sweepAngle = 46f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = primary,
                            startAngle = -50f,
                            sweepAngle = 46f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }
            }
            .pointerInput(containerWidth, containerHeight, topBoundary, bottomInset) {
                detectDragGestures(
                    onDragStart = {
                        dragging = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDragCancel = {
                        // حرکت نیمه‌کاره هم باید ثبت شود، وگرنه موقعیتِ ذخیره‌شده
                        // با موقعیتِ واقعی دکمه ناهماهنگ می‌ماند.
                        dragging = false
                        onCommit(
                            fabFraction(position.first, maxX),
                            fabFraction(position.second - minY, maxY - minY)
                        )
                    },
                    onDragEnd = {
                        dragging = false
                        onCommit(
                            fabFraction(position.first, maxX),
                            fabFraction(position.second - minY, maxY - minY)
                        )
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val clamped = clampFabPosition(
                            x = position.first + dragAmount.x,
                            y = position.second + dragAmount.y,
                            maxX = maxX,
                            minY = minY,
                            maxY = maxY
                        )
                        position = clamped
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier
                .size(FabButtonSize)
                .graphicsLayer {
                    scaleX = dragScale
                    scaleY = dragScale
                }
                .shadow(
                    elevation = glowElevation,
                    shape = CircleShape,
                    ambientColor = primary,
                    spotColor = primary
                ),
            shape = CircleShape,
            containerColor = primary,
            contentColor = onPrimary,
            // سایه از Modifier.shadow می‌آید تا رنگش از تم گرفته شود
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.settings)
            )
        }
    }
}

// منطق خالص موقعیت‌دهی در `FabGeometry.kt` است تا وابسته به Compose نباشد.
