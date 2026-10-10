package com.techquantum.hushka

import android.content.Context

object ThemePreferences {
    private const val PREFS_NAME = "hushka_theme_prefs"
    private const val KEY_THEME = "theme_mode"

    fun getTheme(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, "system") ?: "system"
    }

    fun setTheme(context: Context, mode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, mode)
            .apply()
    }
}
