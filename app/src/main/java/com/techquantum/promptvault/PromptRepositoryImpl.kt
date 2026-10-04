package com.techquantum.promptvault

import kotlinx.coroutines.flow.Flow

class PromptRepositoryImpl(private val dao: PromptDao) : PromptRepository {
    override val prompts: Flow<List<PromptEntity>> = dao.observeAll()

    override suspend fun initializeDefaults() {
        PromptCatalogImporter.importIfNeeded(
            PromptVaultApplicationHolder.context,
            dao
        )
    }

    override suspend fun addPrompt(prompt: PromptEntity): Long = dao.insert(prompt)
    override suspend fun updatePrompt(prompt: PromptEntity) = dao.update(prompt)
    override suspend fun deletePrompt(prompt: PromptEntity) {
        if (prompt.source.startsWith("${PromptLibraryRepository.sourceName}:")) {
            AppSettings.addDeletedLibraryKey(PromptVaultApplicationHolder.context, prompt.source)
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
        val deletedKeys = AppSettings.getDeletedLibraryKeys(PromptVaultApplicationHolder.context)

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
    }}
