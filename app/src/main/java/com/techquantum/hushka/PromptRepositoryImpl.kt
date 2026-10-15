package com.techquantum.hushka

import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class PromptRepositoryImpl(private val dao: PromptDao) : PromptRepository {
    override val prompts: Flow<List<PromptEntity>> = dao.observeAll()

    private val webCacheDao: WebCacheDao?
        get() = try {
            if (HushkaApplicationHolder.isInitialized()) {
                PromptDatabase.getInstance(HushkaApplicationHolder.context).webCacheDao()
            } else null
        } catch (e: Throwable) {
            null
        }

    private val cacheValidityMs = 24L * 60L * 60L * 1000L

    override suspend fun initializeDefaults() {
        PromptCatalogImporter.importIfNeeded(
            HushkaApplicationHolder.context,
            dao
        )
    }

    override suspend fun addPrompt(prompt: PromptEntity): Long = dao.insert(prompt)
    override suspend fun updatePrompt(prompt: PromptEntity) = dao.update(prompt)
    override suspend fun deletePrompt(prompt: PromptEntity) {
        if (prompt.source.startsWith("${PromptLibraryRepository.sourceName}:")) {
            AppSettings.addDeletedLibraryKey(HushkaApplicationHolder.context, prompt.source)
        }
        dao.delete(prompt)
    }
    override suspend fun toggleFavorite(prompt: PromptEntity) =
        dao.update(prompt.copy(favorite = !prompt.favorite))

    override suspend fun togglePinned(prompt: PromptEntity) =
        dao.update(prompt.copy(pinned = !prompt.pinned))

    override suspend fun markUsed(prompt: PromptEntity) =
        dao.update(prompt.copy(lastUsedAt = System.currentTimeMillis()))

    override suspend fun updateLibrary(): LibraryUpdateResult {
        // Every update reconciles the remote library so all existing prompts
        // are reclassified immediately and new prompts enter their category.
        val imported = PromptLibraryRepository.fetch()
        val existing = dao.getLibraryPrompts().associateBy { it.source }
        val deletedKeys = AppSettings.getDeletedLibraryKeys(HushkaApplicationHolder.context)

        var added = 0
        var firstNewPromptId: Long? = null

        imported.forEach { remote ->
            val sourceKey = "${PromptLibraryRepository.sourceName}:${remote.remoteKey}"
            val current = existing[sourceKey]

            if (current == null) {
                if (sourceKey in deletedKeys) return@forEach
                val insertedId = dao.insert(
                    PromptEntity(
                        title = remote.title,
                        category = remote.category,
                        text = remote.text,
                        tags = remote.tags,
                        source = sourceKey,
                        license = PromptLibraryRepository.licenseName
                    )
                )
                if (firstNewPromptId == null) firstNewPromptId = insertedId
                added++
            } else {
                // Keep local identity/favorites, but refresh remote content,
                // category and tags on every update.
                val refreshed = current.copy(
                    title = remote.title,
                    category = remote.category,
                    text = remote.text,
                    tags = remote.tags,
                    source = sourceKey,
                    license = PromptLibraryRepository.licenseName
                )
                if (refreshed != current) dao.update(refreshed)
            }
        }

        return LibraryUpdateResult(
            addedCount = added,
            firstNewPromptId = firstNewPromptId
        )
    }

    override suspend fun getCachedWebPrompts(source: String): List<PromptEntity>? {
        val dao = webCacheDao ?: return null
        return runCatching {
            val row = dao.get(source) ?: return@runCatching null
            val age = System.currentTimeMillis() - row.cachedAt
            if (age > cacheValidityMs) return@runCatching null
            jsonToPrompts(row.payload)
        }.getOrNull()
    }

    override suspend fun cacheWebPrompts(source: String, prompts: List<PromptEntity>) {
        val dao = webCacheDao ?: return
        runCatching {
            dao.put(
                WebCacheEntity(
                    source = source,
                    payload = promptsToJson(prompts),
                    cachedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun clearWebCache() {
        runCatching { webCacheDao?.clearAll() }
    }

    private fun promptsToJson(prompts: List<PromptEntity>): String {
        val arr = JSONArray()
        prompts.forEach { p ->
            arr.put(JSONObject().apply {
                put("title", p.title)
                put("category", p.category)
                put("text", p.text)
                put("tags", p.tags)
            })
        }
        return arr.toString()
    }

    private fun jsonToPrompts(json: String): List<PromptEntity> {
        val arr = JSONArray(json)
        val list = mutableListOf<PromptEntity>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                PromptEntity(
                    id = -(i + 1).toLong(),
                    title = o.optString("title"),
                    category = o.optString("category"),
                    text = o.optString("text"),
                    tags = o.optString("tags"),
                    source = "web_cache"
                )
            )
        }
        return list
    }
}
