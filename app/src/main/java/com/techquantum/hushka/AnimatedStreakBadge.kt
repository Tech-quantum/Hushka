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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val STREAK_ORANGE = Color(0xFFFF9800)
private val STREAK_FLAME_HOT = Color(0xFFFFC107)

@Composable
fun AnimatedStreakBadge(
    streak: Int,
    modifier: Modifier = Modifier
) {
    if (streak <= 0) return

    val transition = rememberInfiniteTransition(label = "streakFlame")

    val flameScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )

    val flameLift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameLift"
    )

    val glowAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streakGlow"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = STREAK_ORANGE.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    STREAK_ORANGE.copy(alpha = glowAlpha),
                                    STREAK_FLAME_HOT.copy(alpha = glowAlpha * 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Text(
                    text = "\uD83D\uDD25",
                    fontSize = 11.sp,
                    modifier = Modifier
                        .scale(flameScale)
                        .padding(bottom = flameLift.dp)
                )
            }

            Spacer(Modifier.width(2.dp))

            Text(
                text = "$streak",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = STREAK_ORANGE
            )
        }
    }
}
