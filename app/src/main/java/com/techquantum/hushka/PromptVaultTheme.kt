package com.techquantum.promptvault

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun PromptVaultTheme(
    themeMode: String,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }

    MaterialTheme(
        colorScheme = if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFBFC1FF),
                onPrimary = Color(0xFF292A67),
                primaryContainer = Color(0xFF41437F),
                onPrimaryContainer = Color(0xFFE6E5FF),
                secondary = Color(0xFFC5C3DC),
                background = Color(0xFF121318),
                surface = Color(0xFF1A1B20),
                surfaceVariant = Color(0xFF45464F),
                outline = Color(0xFF90919A)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF5B5FEF),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFE8E8FF),
                onPrimaryContainer = Color(0xFF15164A),
                secondary = Color(0xFF5F5D72),
                background = Color(0xFFF7F7FB),
                surface = Color.White,
                surfaceVariant = Color(0xFFEFEFF5),
                outline = Color(0xFFD8D8E2)
            )
        },
        content = content
    )
}
