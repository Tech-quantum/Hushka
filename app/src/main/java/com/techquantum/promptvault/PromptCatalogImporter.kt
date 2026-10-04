package com.techquantum.promptvault

import android.content.Context
import org.json.JSONObject

object PromptCatalogImporter {
    private const val ASSET_NAME = "prompts_catalog_v1.2.0.json"

    suspend fun importIfNeeded(context: Context, dao: PromptDao) {
        val seenIds = AppSettings.getImportedCatalogIds(context).toHashSet()
        seenIds += dao.getCatalogIds()
        val root = JSONObject(context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() })
        val prompts = root.getJSONArray("prompts")

        try {
        for (i in 0 until prompts.length()) {
            val item = prompts.getJSONObject(i)
            val catalogId = item.getString("id")
            if (catalogId in seenIds) continue

            val tags = item.optJSONArray("tags")?.let { array ->
                (0 until array.length()).joinToString(",") { index -> array.getString(index) }
            } ?: ""

            val provider = item.optString("provider", "")
            val sourceIds = item.optJSONArray("source_ids")?.let { array ->
                (0 until array.length()).joinToString(",") { index -> array.getString(index) }
            } ?: ""

            val source = buildString {
                append("catalog:")
                append(provider.ifBlank { "unknown" })
                append(":")
                append(catalogId)
                if (sourceIds.isNotBlank()) {
                    append(":")
                    append(sourceIds)
                }
            }

            dao.insert(
                PromptEntity(
                    catalogId = catalogId,
                    provider = provider,
                    modality = item.optString("modality", ""),
                    title = item.getString("title"),
                    category = item.getString("category"),
                    text = item.getString("prompt"),
                    tags = tags,
                    source = source,
                    license = ""
                )
            )
            seenIds += catalogId
        }
        } finally {
            AppSettings.setImportedCatalogIds(context, seenIds)
        }
    }
}
