package com.techquantum.promptvault

import android.content.Context

object AppSettings {
    private const val PREFS = "promptvault_settings"
    private const val THEME = "theme_mode"
    private const val LANGUAGE = "language"
    private const val IMPORTED_CATALOG_IDS = "imported_catalog_ids"
    private const val DELETED_LIBRARY_KEYS = "deleted_library_keys"

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
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(THEME, "system") ?: "system"

    fun setTheme(context: Context, value: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(THEME, value).apply()
    }

    fun getLanguage(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(LANGUAGE, "fa") ?: "fa"

    fun setLanguage(context: Context, value: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(LANGUAGE, value).apply()
    }
}
