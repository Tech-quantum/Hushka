package com.techquantum.hushka
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private data class Accent(
    val darkPrimary: Color,
    val darkOnPrimary: Color,
    val darkContainer: Color,
    val darkOnContainer: Color,
    val lightPrimary: Color,
    val lightContainer: Color,
    val lightOnContainer: Color
)

private fun accentFor(colorTheme: String): Accent = when (colorTheme) {
    "red" -> Accent(
        Color(0xFFFFB3AD), Color(0xFF68000C), Color(0xFF93000F), Color(0xFFFFDAD6),
        Color(0xFFD32F2F), Color(0xFFFFDAD6), Color(0xFF410002)
    )
    "green" -> Accent(
        Color(0xFF8BD98F), Color(0xFF00390F), Color(0xFF1B5E20), Color(0xFFA7F5A9),
        Color(0xFF2E7D32), Color(0xFFC8F2C9), Color(0xFF002106)
    )
    "blue" -> Accent(
        Color(0xFF9ACBFF), Color(0xFF003258), Color(0xFF00497D), Color(0xFFD1E4FF),
        Color(0xFF1976D2), Color(0xFFD1E4FF), Color(0xFF001D36)
    )
    "orange" -> Accent(
        Color(0xFFFFB86B), Color(0xFF4A2800), Color(0xFF6A3C00), Color(0xFFFFDCBE),
        Color(0xFFEF6C00), Color(0xFFFFDCBE), Color(0xFF2C1600)
    )
    else -> Accent( // purple (default)
        Color(0xFFBFC1FF), Color(0xFF292A67), Color(0xFF41437F), Color(0xFFE6E5FF),
        Color(0xFF5B5FEF), Color(0xFFE8E8FF), Color(0xFF15164A)
    )
}

@Composable
fun HushkaTheme(
    themeMode: String,
    colorTheme: String = "purple",
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        "amoled" -> true
        else -> systemDark
    }
    val accent = accentFor(colorTheme)

    MaterialTheme(
        colorScheme = if (darkTheme) {
            if (themeMode == "amoled") {
                darkColorScheme(
                    primary = accent.darkPrimary,
                    onPrimary = accent.darkOnPrimary,
                    primaryContainer = accent.darkContainer,
                    onPrimaryContainer = accent.darkOnContainer,
                    secondary = Color(0xFFC5C3DC),
                    background = Color(0xFF000000),      // مشکی مطلق
                    surface = Color(0xFF000000),         // مشکی مطلق
                    surfaceVariant = Color(0xFF1C1C1C),  // خاکستری تیره برای کارت‌ها
                    outline = Color(0xFF90919A)
                )
            } else {
                darkColorScheme(
                    primary = accent.darkPrimary,
                    onPrimary = accent.darkOnPrimary,
                    primaryContainer = accent.darkContainer,
                    onPrimaryContainer = accent.darkOnContainer,
                    secondary = Color(0xFFC5C3DC),
                    background = Color(0xFF121318),
                    surface = Color(0xFF1A1B20),
                    surfaceVariant = Color(0xFF45464F),
                    outline = Color(0xFF90919A)
                )
            }
        } else {
            lightColorScheme(
                primary = accent.lightPrimary,
                onPrimary = Color.White,
                primaryContainer = accent.lightContainer,
                onPrimaryContainer = accent.lightOnContainer,
                secondary = Color(0xFF5F5D72),
                background = Color(0xFFF7F7FB),
                surface = Color.White,
                surfaceVariant = Color(0xFFEFEFF5),
                outline = Color(0xFFD8D8E2)
            )
        },
        content = content
    )
    } // close CompositionLocalProvider
}
