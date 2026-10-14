package com.techquantum.hushka

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * پالت LED — ۸ رنگ (آخری = اولی برای چرخه نرم)
 */
private val LED_COLORS = listOf(
    Color(0xFF9C27B0), // بنفش
    Color(0xFF3F51B5), // نیلی
    Color(0xFF2196F3), // آبی
    Color(0xFF00BCD4), // سایان
    Color(0xFF4CAF50), // سبز
    Color(0xFFFFC107), // کهربایی
    Color(0xFFFF5722), // نارنجی
    Color(0xFFE91E63), // صورتی
    Color(0xFF9C27B0), // ← بنفش (برای حلقه)
)

@Composable
fun HushkaLogo(
    size: Dp = 44.dp
) {
    val transition = rememberInfiniteTransition(label = "hushkaLogo")

    // چرخه رنگ در ۶ ثانیه
    val colorIndex by transition.animateFloat(
        initialValue = 0f,
        targetValue = (LED_COLORS.size - 1).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "colorIndex"
    )

    // درخشش پویا (pulse)
    val glowAlpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // رنگ نرم بین دو رنگ مجاور
    val lower = colorIndex.toInt().coerceIn(0, LED_COLORS.size - 1)
    val upper = (lower + 1).coerceIn(0, LED_COLORS.size - 1)
    val fraction = colorIndex - lower
    val blendedColor = lerp(LED_COLORS[lower], LED_COLORS[upper], fraction)

    Box(
        modifier = Modifier.size(size * 1.6f),
        contentAlignment = Alignment.Center
    ) {
        // 🔆 هاله بیرونی (Glow)
        Box(
            modifier = Modifier
                .size(size * 1.6f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            blendedColor.copy(alpha = 0.35f * glowAlpha),
                            blendedColor.copy(alpha = 0.12f * glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        // 💎 هسته لوگو
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            blendedColor.copy(alpha = glowAlpha * 0.55f),
                            blendedColor.copy(alpha = 0.15f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "💎",
                fontSize = (size.value * 0.58f).sp
            )
        }
    }
}
