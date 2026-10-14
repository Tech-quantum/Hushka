package com.techquantum.hushka

import android.content.Context
import org.json.JSONObject

object AppSettings {
    private const val PREFS = "promptvault_settings"
    private const val THEME = "theme_mode"
    private const val LANGUAGE = "language"
    private const val COLOR_THEME = "color_theme"
    private const val IMPORTED_CATALOG_IDS = "imported_catalog_ids"
    private const val DELETED_LIBRARY_KEYS = "deleted_library_keys"
    private const val TAG_COLORS = "tag_colors"

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

    private fun saveTagColors(context: Context, map: Map<String, String>) {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        prefs(context).edit().putString(TAG_COLORS, obj.toString()).apply()
    }

}
