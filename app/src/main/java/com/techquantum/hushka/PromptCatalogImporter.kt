package com.techquantum.hushka

import android.content.Context
import org.json.JSONObject

object PromptCatalogImporter {
    private val ASSET_NAMES = listOf(
        "prompts_catalog_v1.2.0.json",
        "prompts_persian_v0.1.0.json",
        "prompts_psychology_v0.2.0.json",
        "prompts_psychology_v0.3.0.json",
        "prompts_psychology_v0.4.0.json",
        "prompts_psychology_v0.5.0.json",
        "prompts_psychology_v0.6.0.json",
        "prompts_life_v0.1.0.json"
    )

    suspend fun importIfNeeded(context: Context, dao: PromptDao) {
        val seenIds = AppSettings.getImportedCatalogIds(context).toHashSet()
        seenIds += dao.getCatalogIds()
        try {
            for (assetName in ASSET_NAMES) {
                val root = try {
                    JSONObject(context.assets.open(assetName).bufferedReader().use { it.readText() })
                } catch (e: Exception) {
                    continue
                }
                val prompts = root.optJSONArray("prompts") ?: continue

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
            }
        } finally {
            AppSettings.setImportedCatalogIds(context, seenIds)
        }
    }
}
