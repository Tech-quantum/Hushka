package com.techquantum.hushka

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

class PromptViewModel(private val repository: PromptRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private var filterJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) { repository.initializeDefaults() }
        viewModelScope.launch {
            repository.prompts.collectLatest { prompts ->
                val allTags = withContext(Dispatchers.Default) {
                    prompts.flatMap { it.tags.split(",").map { t -> t.trim() } }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .sorted()
                }
                updateState { it.copy(prompts = prompts, allTags = allTags) }
            }
        }
    }

    fun onQueryChanged(value: String) = updateState { it.copy(query = value) }
    fun onProviderSelected(value: String) = updateState { it.copy(selectedProvider = value, selectedDomain = ALL_DOMAIN, selectedCategory = ALL_CATEGORY, selectedCategories = emptySet()) }
    fun onDomainSelected(value: String) = updateState { it.copy(selectedDomain = value, selectedCategory = ALL_CATEGORY, selectedCategories = emptySet()) }
    fun onCategorySelected(value: String) = updateState { it.copy(selectedCategory = value, selectedCategories = emptySet()) }
    fun onCategoriesSelected(values: Set<String>) = updateState { it.copy(selectedCategories = values, selectedCategory = ALL_CATEGORY) }
    fun onCollectionSelected(value: String) = updateState { it.copy(selectedCollection = value) }
    fun onPinnedToggled() = updateState { it.copy(pinnedOnly = !it.pinnedOnly) }
    fun onRecentToggled() = updateState { it.copy(recentOnly = !it.recentOnly) }
    fun togglePinned(prompt: PromptEntity) { viewModelScope.launch(Dispatchers.IO) { repository.togglePinned(prompt) } }
    fun markUsed(prompt: PromptEntity) { viewModelScope.launch(Dispatchers.IO) { repository.markUsed(prompt) } }
    fun onFavoritesToggled() = updateState { it.copy(favoritesOnly = !it.favoritesOnly) }
    fun onSortSelected(value: String) = updateState { it.copy(sortMode = value) }
    fun onLanguageSelected(value: String) = updateState { it.copy(selectedLanguage = value) }
    fun onTagFilterChanged(tags: Set<String>, mode: String) = updateState { it.copy(selectedTags = tags, tagFilterMode = mode) }
    fun clearNewPromptId() = updateState { it.copy(newPromptId = null) }

    fun updateLibrary() {
        if (_uiState.value.isUpdating) return
        updateState { it.copy(isUpdating = true, updateMessageKey = "updating", updateAddedCount = 0, updateError = "") }
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { repository.updateLibrary() }
                updateState { it.copy(isUpdating = false, newPromptId = result.firstNewPromptId, updateAddedCount = result.addedCount, updateMessageKey = if (result.addedCount == 0) "up_to_date" else "added") }
            } catch (e: Exception) {
                updateState { it.copy(isUpdating = false, updateMessageKey = "error", updateError = e.message ?: "") }
            }
        }
    }

    fun addPrompt(title: String, category: String, tags: String, text: String, collection: String = "") {
        viewModelScope.launch(Dispatchers.IO) { repository.addPrompt(PromptEntity(title = title, category = category, tags = tags, text = text, collection = collection)) }
    }

    fun addPromptWithRole(title: String, category: String, tags: String, text: String, collection: String, role: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPrompt(
                PromptEntity(
                    title = title,
                    category = category,
                    tags = tags,
                    text = text,
                    collection = collection,
                    role = role
                )
            )
        }
    }

    fun updatePrompt(prompt: PromptEntity) { viewModelScope.launch(Dispatchers.IO) { repository.updatePrompt(prompt) } }
    fun deletePrompt(prompt: PromptEntity) { viewModelScope.launch(Dispatchers.IO) { repository.deletePrompt(prompt) } }
    fun toggleFavorite(prompt: PromptEntity) { viewModelScope.launch(Dispatchers.IO) { repository.toggleFavorite(prompt) } }

    // ==================== State & Filtering ====================

    private fun updateState(transform: (HomeUiState) -> HomeUiState) {
        _uiState.update(transform)
        scheduleFiltering()
    }

    /**
     * فیلتر و مرتب‌سازی روی ترد پس‌زمینه انجام می‌شود تا رابط کاربری قفل نکند.
     * اگر تغییر جدیدی بیاید، کار قبلی لغو می‌شود و فقط آخرین نتیجه اعمال می‌شود.
     */
    private fun scheduleFiltering() {
        filterJob?.cancel()
        val snapshot = _uiState.value
        filterJob = viewModelScope.launch {
            try {
                val startedAt = System.currentTimeMillis()
                val filtered = withContext(Dispatchers.Default) { filterWith(snapshot) }
                val elapsed = System.currentTimeMillis() - startedAt
                if (elapsed > 150) {
                    Log.w(TAG, "Slow filtering: ${elapsed}ms, sort=${snapshot.sortMode}, items=${snapshot.prompts.size}")
                }
                _uiState.update { it.copy(filteredPrompts = filtered) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Filtering failed (sort=${snapshot.sortMode})", e)
            }
        }
    }

    private fun filterWith(state: HomeUiState): List<PromptEntity> = PromptFilter.apply(
        prompts = state.prompts,
        query = state.query,
        selectedProvider = state.selectedProvider,
        selectedDomain = state.selectedDomain,
        selectedCategory = state.selectedCategory,
        selectedCategories = state.selectedCategories,
        selectedCollection = state.selectedCollection,
        favoritesOnly = state.favoritesOnly,
        sortMode = state.sortMode,
        pinnedOnly = state.pinnedOnly,
        recentOnly = state.recentOnly,
        selectedLanguage = state.selectedLanguage,
        selectedTags = state.selectedTags,
        tagFilterMode = state.tagFilterMode
    )

    // ==================== Export / Import ====================

    fun exportToJson(context: Context, uri: Uri, prompts: List<PromptEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonArray = JSONArray()
                prompts.forEach { p ->
                    val obj = JSONObject().apply {
                        put("id", p.id)
                        put("title", p.title)
                        put("category", p.category)
                        put("tags", p.tags)
                        put("text", p.text)
                        put("collection", p.collection)
                        put("role", p.role)
                    }
                    jsonArray.put(obj)
                }
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(jsonArray.toString(2).toByteArray())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Export JSON failed", e)
            }
        }
    }

    fun exportToMarkdown(context: Context, uri: Uri, prompts: List<PromptEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sb = StringBuilder()
                sb.append("# Hushka Prompts Backup\n\n")
                sb.append("Total: ${prompts.size} prompts\n\n---\n\n")
                prompts.forEach { p ->
                    sb.append("## ${p.title}\n\n")
                    if (p.category.isNotBlank()) sb.append("**Category:** ${p.category}\n\n")
                    if (p.role.isNotBlank()) sb.append("**Role:** ${p.role}\n\n")
                    if (p.tags.isNotBlank()) sb.append("**Tags:** ${p.tags}\n\n")
                    if (p.collection.isNotBlank()) sb.append("**Collection:** ${p.collection}\n\n")
                    sb.append("```\n${p.text}\n```\n\n---\n\n")
                }
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(sb.toString().toByteArray())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Export Markdown failed", e)
            }
        }
    }

    fun importFromJson(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: return@launch
                val jsonArray = JSONArray(content)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val title = obj.optString("title", "")
                    if (title.isBlank()) continue
                    repository.addPrompt(
                        PromptEntity(
                            title = title,
                            category = obj.optString("category", ""),
                            tags = obj.optString("tags", ""),
                            text = obj.optString("text", ""),
                            collection = obj.optString("collection", ""),
                            role = obj.optString("role", "")
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Import JSON failed", e)
            }
        }
    }

    // ==================== Web Prompts Fetch ====================

    private val _selectedWebSource = MutableStateFlow(WebSource.GITHUB)
    val selectedWebSource: StateFlow<WebSource> = _selectedWebSource.asStateFlow()

    fun onWebSourceSelected(source: WebSource) {
        _selectedWebSource.value = source
    }

    private val _webPrompts = MutableStateFlow<List<PromptEntity>>(emptyList())
    val webPrompts: StateFlow<List<PromptEntity>> = _webPrompts.asStateFlow()

    private val _isFetchingWeb = MutableStateFlow(false)
    val isFetchingWeb: StateFlow<Boolean> = _isFetchingWeb.asStateFlow()

    private val _webError = MutableStateFlow<String?>(null)
    val webError: StateFlow<String?> = _webError.asStateFlow()

    // 🆕 پیام پویا برای نمایش به کاربر هنگام لودینگ
    private val _loadingMessage = MutableStateFlow("loading_connecting")
    val loadingMessage: StateFlow<String> = _loadingMessage.asStateFlow()

    fun fetchWebPrompts(forceRefresh: Boolean = false) {
        if (_isFetchingWeb.value) return
        val source = _selectedWebSource.value
        val tStart = System.currentTimeMillis()

        viewModelScope.launch {
            // ⚡ اول کش را چک کن (بدون هیچ state change)
            if (!forceRefresh) {
                val cached: List<PromptEntity>? = withContext(Dispatchers.IO) {
                    WebCacheManager.get(
                        HushkaApplicationHolder.context.applicationContext,
                        source.id
                    )
                }
                if (!cached.isNullOrEmpty()) {
                    // ⚡ instant — بدون loading state
                    val t = System.currentTimeMillis() - tStart
                    _webError.value = null
                    _webPrompts.value = cached
                    _loadingMessage.value = "loading_from_cache"
                    android.widget.Toast.makeText(
                        HushkaApplicationHolder.context.applicationContext,
                        "⚡ CACHE HIT [$source]\n${cached.size} prompts in ${t}ms",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
            }

            // 🌐 فقط اگر کش نبود، وارد حالت loading شو
            android.widget.Toast.makeText(
                HushkaApplicationHolder.context.applicationContext,
                "🌐 CACHE MISS [$source]\nFetching from web...",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            _isFetchingWeb.value = true
            _webError.value = null
            _webPrompts.value = emptyList()
            _loadingMessage.value = "loading_connecting"

            val messageJob = launch {
                delay(3_000)
                _loadingMessage.value = "loading_downloading"
                delay(4_000)
                _loadingMessage.value = "loading_almost_ready"
                delay(5_000)
                _loadingMessage.value = "loading_slow_internet"
            }

            try {
                val result = withContext(Dispatchers.IO) {
                    when (source) {
                        WebSource.ALL -> fetchAllSources()
                        WebSource.GITHUB -> fetchFromGitHub()
                        WebSource.HF_PROMPTS_CHAT -> fetchFromHuggingFace(
                            dataset = "fka/prompts.chat",
                            promptField = "prompt",
                            titleField = "act",
                            limit = 500
                        )
                        WebSource.HF_STABLE_DIFFUSION -> fetchFromHuggingFace(
                            dataset = "Gustavosta/Stable-Diffusion-Prompts",
                            promptField = "Prompt",
                            titleField = null,
                            limit = 500
                        )
                        WebSource.HF_JAILBREAK -> fetchFromHuggingFace(
                            dataset = "rubend18/ChatGPT-Jailbreak-Prompts",
                            promptField = "Prompt",
                            titleField = "name",
                            limit = 100
                        )
                        WebSource.HF_PERSIAN -> fetchFromHuggingFace(
                            dataset = "kasranaqhdpur/persiandatasets2",
                            promptField = "instruction",
                            titleField = null,
                            limit = 300,
                            extraField = "input"
                        )
                        WebSource.HF_DOLLY -> fetchFromHuggingFace(
                            dataset = "databricks/databricks-dolly-15k",
                            promptField = "instruction",
                            titleField = null,
                            limit = 300,
                            extraField = "context"
                        )
                        WebSource.HF_WRITING -> fetchFromHuggingFace(
                            dataset = "euclaise/writingprompts",
                            promptField = "prompt",
                            titleField = null,
                            limit = 300
                        )
                        WebSource.HF_CODE -> fetchFromHuggingFace(
                            dataset = "sahil2801/CodeAlpaca-20k",
                            promptField = "instruction",
                            titleField = null,
                            limit = 300,
                            extraField = "input"
                        )
                        WebSource.HF_MEDICAL -> fetchFromHuggingFace(
                            dataset = "medalpaca/medical_meadow_medical_flashcards",
                            promptField = "instruction",
                            titleField = null,
                            limit = 200,
                            extraField = "input"
                        )
                    }
                }

                if (result.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        WebCacheManager.put(
                            HushkaApplicationHolder.context.applicationContext,
                            source.id,
                            result
                        )
                    }
                }

                val tTotal = System.currentTimeMillis() - tStart
                android.widget.Toast.makeText(
                    HushkaApplicationHolder.context.applicationContext,
                    "🌐 WEB FETCH [$source]\n${result.size} prompts in ${tTotal}ms",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                _webPrompts.value = result
                if (result.isEmpty()) {
                    _webError.value = "no_prompts_found"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fetching web prompts failed", e)
                val staleCache: List<PromptEntity>? = withContext(Dispatchers.IO) {
                    try {
                        val f = java.io.File(
                            HushkaApplicationHolder.context.applicationContext.filesDir,
                            "web_cache_${source.id}.json"
                        )
                        if (f.exists()) {
                            val text = f.readText(Charsets.UTF_8)
                            val arr = org.json.JSONArray(text)
                            val list = ArrayList<PromptEntity>()
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
                            list
                        } else null
                    } catch (e2: Throwable) {
                        Log.e(TAG, "stale cache read failed", e2)
                        null
                    }
                }
                if (!staleCache.isNullOrEmpty()) {
                    _webPrompts.value = staleCache
                    _loadingMessage.value = "loading_from_cache_offline"
                } else {
                    _webError.value = e.message ?: "unknown_error"
                    _webPrompts.value = emptyList()
                }
            } finally {
                messageJob.cancel()
                _loadingMessage.value = "loading_connecting"
                _isFetchingWeb.value = false
            }
        }
    }

    fun refreshWebPrompts() {
        fetchWebPrompts(forceRefresh = true)
    }

    fun clearWebCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                WebCacheManager.clearAll(
                    HushkaApplicationHolder.context.applicationContext
                )
            }
        }
    }

    /**
     * واکشی از همه منابع — با تحمل خطا
     */
    private suspend fun fetchAllSources(): List<PromptEntity> {
        val result = mutableListOf<PromptEntity>()

        try { result += fetchFromGitHub() } catch (e: Exception) { Log.e(TAG, "ALL: GitHub failed", e) }

        val hfSources = listOf(
            Triple("fka/prompts.chat", "prompt" to "act", null as String?),
            Triple("Gustavosta/Stable-Diffusion-Prompts", "Prompt" to null, null),
            Triple("rubend18/ChatGPT-Jailbreak-Prompts", "Prompt" to "name", null),
            Triple("kasranaqhdpur/persiandatasets2", "instruction" to null, "input"),
            Triple("databricks/databricks-dolly-15k", "instruction" to null, "context"),
            Triple("euclaise/writingprompts", "prompt" to null, null),
            Triple("sahil2801/CodeAlpaca-20k", "instruction" to null, "input"),
            Triple("medalpaca/medical_meadow_medical_flashcards", "instruction" to null, "input")
        )

        for ((dataset, fields, extra) in hfSources) {
            val (promptField, titleField) = fields
            try {
                result += fetchFromHuggingFace(
                    dataset = dataset,
                    promptField = promptField,
                    titleField = titleField,
                    limit = 200,
                    extraField = extra
                )
            } catch (e: Exception) {
                Log.e(TAG, "ALL: $dataset failed", e)
            }
        }

        return result
    }

    private suspend fun fetchFromGitHub(): List<PromptEntity> {
        val url = URL("https://raw.githubusercontent.com/f/awesome-chatgpt-prompts/main/prompts.csv")
        val csv = url.readText()
        return parseCsv(csv)
    }

    private suspend fun fetchFromHuggingFace(
        dataset: String,
        promptField: String,
        titleField: String?,
        limit: Int,
        extraField: String? = null
    ): List<PromptEntity> {
        val allPrompts = mutableListOf<PromptEntity>()
        val encodedDataset = URLEncoder.encode(dataset, "UTF-8")
        val batchSize = 100
        var offset = 0

        while (offset < limit) {
            val length = minOf(batchSize, limit - offset)
            val url = "https://datasets-server.huggingface.co/rows?" +
                    "dataset=$encodedDataset&config=default&split=train" +
                    "&offset=$offset&length=$length"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val rows = json.optJSONArray("rows") ?: break
            if (rows.length() == 0) break

            for (i in 0 until rows.length()) {
                val rowObj = rows.getJSONObject(i).getJSONObject("row")
                val mainText = rowObj.optString(promptField, "").trim()
                val extraText = if (extraField != null) rowObj.optString(extraField, "").trim() else ""
                val promptText = if (extraText.isNotBlank() && !mainText.contains(extraText)) {
                    "$mainText\n\n$extraText"
                } else {
                    mainText
                }
                if (promptText.isBlank()) continue

                val title = if (titleField != null) {
                    rowObj.optString(titleField, "").trim().take(100).ifBlank {
                        promptText.take(60)
                    }
                } else {
                    promptText.take(60)
                }

                allPrompts.add(
                    PromptEntity(
                        title = title,
                        category = "web",
                        tags = "huggingface,$dataset",
                        text = promptText,
                        collection = "huggingface"
                    )
                )
            }

            offset += rows.length()
            if (rows.length() < length) break
            delay(200)
        }

        return allPrompts
    }

    private fun parseCsv(csv: String): List<PromptEntity> {
        val prompts = mutableListOf<PromptEntity>()
        val lines = csv.split("\n")
        var i = 1

        while (i < lines.size) {
            val line = lines[i]
            if (line.isBlank()) {
                i++
                continue
            }
            val parsed = parseCsvLine(lines, i)
            if (parsed != null && parsed.values.size >= 2) {
                val act = parsed.values[0].trim()
                val prompt = parsed.values[1].trim()
                if (act.isNotBlank() && prompt.isNotBlank()) {
                    prompts.add(
                        PromptEntity(
                            title = act.take(100),
                            category = "web",
                            tags = "awesome-chatgpt-prompts",
                            text = prompt,
                            collection = "github"
                        )
                    )
                }
                i = parsed.lastLineIndex + 1
            } else {
                i++
            }
        }
        return prompts
    }

    private data class CsvParseResult(val values: List<String>, val lastLineIndex: Int)

    private fun parseCsvLine(allLines: List<String>, startIndex: Int): CsvParseResult? {
        val values = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var currentIndex = startIndex
        var charIndex = 0
        var text = allLines[currentIndex]

        while (charIndex < text.length || inQuotes) {
            if (charIndex >= text.length) {
                currentIndex++
                if (currentIndex >= allLines.size) break
                text += "\n" + allLines[currentIndex]
                continue
            }
            val ch = text[charIndex]
            when {
                ch == '"' -> {
                    if (inQuotes && charIndex + 1 < text.length && text[charIndex + 1] == '"') {
                        sb.append('"')
                        charIndex++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ch == ',' && !inQuotes -> {
                    values.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(ch)
            }
            charIndex++
        }
        values.add(sb.toString())
        return CsvParseResult(values, currentIndex)
    }

    fun importWebPrompts(selected: List<PromptEntity>) {
        if (selected.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            selected.forEach { p ->
                repository.addPrompt(p)
            }
            _webPrompts.value = emptyList()
        }
    }

    fun clearWebPrompts() {
        _webPrompts.value = emptyList()
        _webError.value = null
        _loadingMessage.value = "loading_connecting"
    }

    // ==================== Translation ====================

    fun translateWebPrompts(prompts: List<PromptEntity>) {
        if (_isTranslating.value) return
        _isTranslating.value = true
        viewModelScope.launch {
            try {
                val translated = withContext(Dispatchers.IO) {
                    prompts.map { prompt ->
                        val translatedText = TranslationService.translateText(prompt.text)
                        if (translatedText != null) {
                            prompt.copy(text = translatedText)
                        } else {
                            prompt
                        }
                    }
                }
                _webPrompts.value = translated
            } catch (e: Exception) {
                Log.e(TAG, "Translation failed", e)
            } finally {
                _isTranslating.value = false
            }
        }
    }

    // ==================== In-app Prompt Translation ====================

    private val _translateTargetPrompt = MutableStateFlow<PromptEntity?>(null)
    val translateTargetPrompt: StateFlow<PromptEntity?> = _translateTargetPrompt.asStateFlow()

    private val _translateLoading = MutableStateFlow(false)
    val translateLoading: StateFlow<Boolean> = _translateLoading.asStateFlow()

    private val _translatedResult = MutableStateFlow<String?>(null)
    val translatedResult: StateFlow<String?> = _translatedResult.asStateFlow()

    private val _translateError = MutableStateFlow<String?>(null)
    val translateError: StateFlow<String?> = _translateError.asStateFlow()

    fun startPromptTranslation(prompt: PromptEntity, targetLang: String) {
        if (_translateLoading.value) return
        _translateTargetPrompt.value = prompt
        _translateLoading.value = true
        _translatedResult.value = null
        _translateError.value = null

        viewModelScope.launch {
            try {
                val sourceLang = LanguageDetector.detect(prompt.text)
                if (sourceLang == targetLang) {
                    _translateError.value = "same_language"
                } else {
                    val result = TranslationService.translateText(
                        text = prompt.text,
                        sourceLang = sourceLang,
                        targetLang = targetLang
                    )
                    if (result.isNullOrBlank() || result.equals(prompt.text, ignoreCase = true)) {
                        _translateError.value = "empty_result"
                    } else {
                        _translatedResult.value = result
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Prompt translation failed", e)
                _translateError.value = e.message ?: "unknown"
            } finally {
                _translateLoading.value = false
            }
        }
    }

    fun retryPromptTranslation(targetLang: String) {
        val prompt = _translateTargetPrompt.value ?: return
        startPromptTranslation(prompt, targetLang)
    }

    fun dismissPromptTranslation() {
        _translateTargetPrompt.value = null
        _translateLoading.value = false
        _translatedResult.value = null
        _translateError.value = null
    }

    private companion object {
        const val TAG = "HushkaViewModel"
    }
}
