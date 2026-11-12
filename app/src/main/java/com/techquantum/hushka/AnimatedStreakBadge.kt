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
fun AnimatedStreakBadge(streak: Int) {
    if (streak <= 0) return

    androidx.compose.material3.Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = androidx.compose.ui.Modifier.padding(4.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = androidx.compose.ui.Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            androidx.compose.material3.Text(
                text = "🔥",
                fontSize = 16.sp
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = androidx.compose.ui.Modifier.width(4.dp)
            )
            androidx.compose.material3.Text(
                text = streak.toString(),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = 14.sp,
                color = androidx.compose.ui.graphics.Color(0xFFFF9800)
            )
        }
    }
}
