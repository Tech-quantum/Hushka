package com.techquantum.hushka

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * رنگ اختصاصی هر Provider
 */
private fun providerColor(name: String, fallback: Color): Color {
    return when (name.lowercase().trim()) {
        "openai", "gpt", "chatgpt" -> Color(0xFF10A37F)
        "gemini", "google" -> Color(0xFF4285F4)
        "claude", "anthropic" -> Color(0xFFD97757)
        "deepseek" -> Color(0xFF4D6BFE)
        "meta", "llama" -> Color(0xFF0866FF)
        "mistral" -> Color(0xFFFF7000)
        else -> fallback
    }
}

/**
 * آیکون اختصاصی هر Provider
 */
private fun providerIcon(name: String): String? {
    return when (name.lowercase().trim()) {
        "openai", "gpt", "chatgpt" -> "🟢"
        "gemini", "google" -> "🔷"
        "claude", "anthropic" -> "🟠"
        "deepseek" -> "🟣"
        "meta", "llama" -> "🔵"
        "mistral" -> "🟧"
        else -> null
    }
}

@Composable
fun ModernChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingEmoji: String? = null,
    trailingEmoji: String? = null,
    accentColor: Color? = null
) {
    val basePrimary = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val chipAccent = accentColor ?: providerColor(label, basePrimary)
    val emoji = leadingEmoji ?: providerIcon(label)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "chipScale"
    )

    val containerColor by animateColorAsState(
        targetValue = if (selected) chipAccent else surfaceVariant.copy(alpha = 0.6f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipContent"
    )

    val shape = RoundedCornerShape(50)

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(
                brush = if (selected) {
                    Brush.linearGradient(
                        colors = listOf(
                            chipAccent,
                            chipAccent.copy(alpha = 0.82f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(containerColor, containerColor)
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selected) {
                Text(
                    text = "✓",
                    fontSize = 12.sp,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            } else if (emoji != null) {
                Text(text = emoji, fontSize = 11.sp)
            }
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor,
                maxLines = 1
            )
            if (trailingEmoji != null) {
                Text(
                    text = trailingEmoji,
                    fontSize = 11.sp,
                    color = contentColor
                )
            }
        }
    }
}
