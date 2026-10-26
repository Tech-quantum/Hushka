package com.techquantum.hushka

import android.content.Context
import androidx.datastore.preferences.core.edit

import androidx.datastore.preferences.core.edit

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

import org.json.JSONObject


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hushka_settings")

object AppSettings {
    private const val PREFS = "promptvault_settings"
    private const val THEME = "theme_mode"
    private const val LANGUAGE = "language"
    private const val COLOR_THEME = "color_theme"
    private const val IMPORTED_CATALOG_IDS = "imported_catalog_ids"
    private const val DELETED_LIBRARY_KEYS = "deleted_library_keys"
    private const val TAG_COLORS = "tag_colors"

    // AI Chat settings
    private const val AI_PROVIDER = "ai_provider"
    private const val AI_API_KEY = "ai_api_key"
    private const val AI_MODEL = "ai_model"

    // Online speech-to-text settings (independent from the chat provider key)
    private const val SPEECH_API_KEY = "speech_api_key"

    // Floating settings button (position is stored as 0..1 fractions, not pixels)
    private const val FAB_X = "settings_fab_x"
    private const val FAB_Y = "settings_fab_y"
    private const val FAB_HINT_SHOWN = "settings_fab_hint_shown"

    // Biometric app lock
    private const val BIOMETRIC_LOCK = "biometric_lock_enabled"
    private val STREAK_KEY = androidx.datastore.preferences.core.longPreferencesKey("daily_streak")
    private val LAST_LOGIN_KEY = androidx.datastore.preferences.core.longPreferencesKey("last_login_date")


    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // Catalog ids that were already imported once. Lets a user delete a bundled prompt
    // without it coming back on the next launch.
    fun getImportedCatalogIds(context: Context): Set<String> =
        prefs(context).getStringSet(IMPORTED_CATALOG_IDS, emptySet())?.toSet() ?: emptySet()

    fun setImportedCatalogIds(context: Context, ids: Set<String>) {
        prefs(context).edit().putStringSet(IMPORTED_CATALOG_IDS, HashSet(ids)).apply()
    }

    // Source keys of prompts.chat prompts the user deleted; skipped by library updates.
    fun getDeletedLibraryKeys(context: Context): Set<String> =
        prefs(context).getStringSet(DELETED_LIBRARY_KEYS, emptySet())?.toSet() ?: emptySet()

    fun addDeletedLibraryKey(context: Context, key: String) {
        prefs(context).edit()
            .putStringSet(DELETED_LIBRARY_KEYS, HashSet(getDeletedLibraryKeys(context) + key))
            .apply()
    }

    fun getTheme(context: Context): String =
        prefs(context).getString(THEME, "system") ?: "system"

    fun setTheme(context: Context, value: String) {
        prefs(context).edit().putString(THEME, value).apply()
    }

    fun getLanguage(context: Context): String =
        prefs(context).getString(LANGUAGE, "fa") ?: "fa"

    fun setLanguage(context: Context, value: String) {
        prefs(context).edit().putString(LANGUAGE, value).apply()
    }

    fun getColorTheme(context: Context): String =
        prefs(context).getString(COLOR_THEME, "purple") ?: "purple"

    fun setColorTheme(context: Context, value: String) {
        prefs(context).edit().putString(COLOR_THEME, value).apply()
    }



    // ==================== Tag Colors ====================
    fun getAllTagColors(context: Context): Map<String, String> {
        val json = prefs(context).getString(TAG_COLORS, "") ?: ""
        if (json.isBlank()) return emptyMap()
        return try {
            val obj = JSONObject(json)
            val map = mutableMapOf<String, String>()
            obj.keys().forEach { key -> map[key] = obj.getString(key) }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun setTagColor(context: Context, tag: String, colorHex: String) {
        val current = getAllTagColors(context).toMutableMap()
        current[tag] = colorHex
        saveTagColors(context, current)
    }

    fun resetAllTagColors(context: Context) {
        prefs(context).edit().remove(TAG_COLORS).apply()
    }

    // ==================== AI Chat Settings ====================

    fun getAiProvider(context: Context): String =
        prefs(context).getString(AI_PROVIDER, "gemini") ?: "gemini"

    fun setAiProvider(context: Context, value: String) {
        prefs(context).edit().putString(AI_PROVIDER, value).apply()
    }

    fun getAiApiKey(context: Context): String =
        prefs(context).getString(AI_API_KEY, "") ?: ""

    fun setAiApiKey(context: Context, value: String) {
        prefs(context).edit().putString(AI_API_KEY, value).apply()
    }

    fun getAiModel(context: Context): String =
        prefs(context).getString(AI_MODEL, "") ?: ""

    fun setAiModel(context: Context, value: String) {
        prefs(context).edit().putString(AI_MODEL, value).apply()
    }

    /**
     * کلید اختصاصی تشخیص گفتار آنلاین.
     *
     * عمداً از `AI_API_KEY` جدا است، چون آن کلید وابسته به provider انتخابی
     * چت است و ممکن است برای سرویس گفتار کار نکند.
     */
    fun getSpeechApiKey(context: Context): String =
        prefs(context).getString(SPEECH_API_KEY, "") ?: ""

    fun setSpeechApiKey(context: Context, value: String) {
        prefs(context).edit().putString(SPEECH_API_KEY, value).apply()
    }

    // ==================== Floating Settings Button ====================

    /**
     * موقعیت ذخیره‌شده‌ی FAB تنظیمات به‌صورت کسر ۰..۱ از محدوده‌ی حرکت.
     *
     * کسری ذخیره می‌شود (نه پیکسل) تا بعد از چرخش صفحه یا تغییر رزولوشن،
     * دکمه بیرون از صفحه نیفتد. `null` یعنی کاربر هنوز آن را جابه‌جا نکرده است.
     */
    fun getFabPosition(context: Context): Pair<Float, Float>? {
        val p = prefs(context)
        if (!p.contains(FAB_X) || !p.contains(FAB_Y)) return null
        return p.getFloat(FAB_X, 1f) to p.getFloat(FAB_Y, 1f)
    }

    fun setFabPosition(context: Context, fractionX: Float, fractionY: Float) {
        prefs(context).edit()
            .putFloat(FAB_X, fractionX.coerceIn(0f, 1f))
            .putFloat(FAB_Y, fractionY.coerceIn(0f, 1f))
            .apply()
    }

    fun clearFabPosition(context: Context) {
        prefs(context).edit().remove(FAB_X).remove(FAB_Y).apply()
    }

    /** هینت چرخشی فقط یک‌بار در عمر نصب اجرا می‌شود. */
    fun isFabHintShown(context: Context): Boolean =
        prefs(context).getBoolean(FAB_HINT_SHOWN, false)

    fun setFabHintShown(context: Context) {
        prefs(context).edit().putBoolean(FAB_HINT_SHOWN, true).apply()
    }

    /** قفل بیومتریک؛ پیش‌فرض خاموش است تا کاربر خودش روشنش کند. */
    fun isBiometricLockEnabled(context: Context): Boolean =
        prefs(context).getBoolean(BIOMETRIC_LOCK, false)

    fun setBiometricLockEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(BIOMETRIC_LOCK, enabled).apply()
    }

    private fun saveTagColors(context: Context, map: Map<String, String>) {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        prefs(context).edit().putString(TAG_COLORS, obj.toString()).apply()
    }


    suspend fun updateAndGetStreak(context: Context): Int {
        var streak = 1
        val today = java.time.LocalDate.now().toEpochDay()
        
        context.dataStore.edit { prefs ->
            val lastLogin = prefs[LAST_LOGIN_KEY] ?: 0L
            val currentStreak = prefs[STREAK_KEY] ?: 0L
            
            if (lastLogin == today) {
                // امروز قبلاً وارد شده، استریک تغییر نمی‌کند
                streak = currentStreak.toInt().coerceAtLeast(1)
            } else if (lastLogin == today - 1) {
                // دیروز وارد شده، استریک +1 می‌شود
                streak = (currentStreak + 1).toInt()
                prefs[STREAK_KEY] = streak.toLong()
            } else {
                // چند روز وارد نشده، استریک ریست می‌شود
                streak = 1
                prefs[STREAK_KEY] = 1L
            }
            prefs[LAST_LOGIN_KEY] = today
        }
        return streak
    }
}
