package com.techquantum.hushka

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun PromptVaultTheme(
    themeMode: String,
    colorTheme: String = "purple",
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }

    // رنگ اصلی و container بر اساس تم رنگی
    val (primaryColor, primaryContainerColor) = when (colorTheme) {
        "red" -> if (darkTheme) Color(0xFFFF6B6B) to Color(0xFF5A2A2A)
                 else Color(0xFFE53935) to Color(0xFFFFE0E0)
        "green" -> if (darkTheme) Color(0xFF4CAF50) to Color(0xFF2A4A2A)
                   else Color(0xFF43A047) to Color(0xFFE0F5E0)
        "blue" -> if (darkTheme) Color(0xFF5AA9FF) to Color(0xFF2A3A5A)
                  else Color(0xFF1E88E5) to Color(0xFFE0EFFF)
        "orange" -> if (darkTheme) Color(0xFFFFB74D) to Color(0xFF5A3A2A)
                    else Color(0xFFFB8C00) to Color(0xFFFFEEDD)
        else -> if (darkTheme) Color(0xFF7C7CFF) to Color(0xFF3A3A55)
                else Color(0xFF6C63FF) to Color(0xFFE0DEFF)
    }

    MaterialTheme(
        colorScheme = if (darkTheme) {
            darkColorScheme(
                primary = primaryColor,
                onPrimary = Color.White,
                primaryContainer = primaryContainerColor,
                onPrimaryContainer = Color(0xFFEAEAEA),
                secondary = primaryColor.copy(alpha = 0.7f),
                background = Color(0xFF121318),
                onBackground = Color(0xFFEAEAEA),
                surface = Color(0xFF1A1B20),
                onSurface = Color(0xFFEAEAEA),
                surfaceVariant = Color(0xFF45464F),
                onSurfaceVariant = Color(0xFFB0B0B8),
                outline = Color(0xFF90919A)
            )
        } else {
            lightColorScheme(
                primary = primaryColor,
                onPrimary = Color.White,
                primaryContainer = primaryContainerColor,
                onPrimaryContainer = Color(0xFF15164A),
                secondary = primaryColor.copy(alpha = 0.8f),
                background = Color(0xFFF7F7FB),
                onBackground = Color(0xFF1A1A2E),
                surface = Color.White,
                onSurface = Color(0xFF1A1A2E),
                surfaceVariant = Color(0xFFEFEFF5),
                onSurfaceVariant = Color(0xFF5A5A6E),
                outline = Color(0xFFD8D8E2)
            )
        },
        content = content
    )
}
