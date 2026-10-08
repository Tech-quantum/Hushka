package com.techquantum.hushka

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class PromptViewModel(private val repository: PromptRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) { repository.initializeDefaults() }
        viewModelScope.launch {
            repository.prompts.collectLatest { prompts ->
                updateState { it.copy(prompts = prompts) }
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
    fun updatePrompt(prompt: PromptEntity) { viewModelScope.launch(Dispatchers.IO) { repository.updatePrompt(prompt) } }
    fun deletePrompt(prompt: PromptEntity) { viewModelScope.launch(Dispatchers.IO) { repository.deletePrompt(prompt) } }
    fun toggleFavorite(prompt: PromptEntity) { viewModelScope.launch(Dispatchers.IO) { repository.toggleFavorite(prompt) } }

    private fun updateState(transform: (HomeUiState) -> HomeUiState) {
        _uiState.value = applyFilters(transform(_uiState.value))
    }

    private fun applyFilters(state: HomeUiState): HomeUiState = state.copy(
        filteredPrompts = PromptFilter.apply(
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
            recentOnly = state.recentOnly
        )
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
                    }
                    jsonArray.put(obj)
                }
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(jsonArray.toString(2).toByteArray())
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                    if (p.tags.isNotBlank()) sb.append("**Tags:** ${p.tags}\n\n")
                    if (p.collection.isNotBlank()) sb.append("**Collection:** ${p.collection}\n\n")
                    sb.append("```\n${p.text}\n```\n\n---\n\n")
                }
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(sb.toString().toByteArray())
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                            collection = obj.optString("collection", "")
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==================== Web Prompts Fetch ====================

    private val _webPrompts = MutableStateFlow<List<PromptEntity>>(emptyList())
    val webPrompts: StateFlow<List<PromptEntity>> = _webPrompts.asStateFlow()

    private val _isFetchingWeb = MutableStateFlow(false)
    val isFetchingWeb: StateFlow<Boolean> = _isFetchingWeb.asStateFlow()

    private val _webError = MutableStateFlow<String?>(null)
    val webError: StateFlow<String?> = _webError.asStateFlow()

    fun fetchWebPrompts() {
        if (_isFetchingWeb.value) return
        _isFetchingWeb.value = true
        _webError.value = null
        _webPrompts.value = emptyList()
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val url = URL("https://raw.githubusercontent.com/f/awesome-chatgpt-prompts/main/prompts.csv")
                    val csv = url.readText()
                    parseCsv(csv)
                }
                _webPrompts.value = result
                if (result.isEmpty()) {
                    _webError.value = "no_prompts_found"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _webError.value = e.message ?: "unknown_error"
                _webPrompts.value = emptyList()
            } finally {
                _isFetchingWeb.value = false
            }
        }
    }

    private fun parseCsv(csv: String): List<PromptEntity> {
        val prompts = mutableListOf<PromptEntity>()
        val lines = csv.split("\n")
        var i = 1 // skip header

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
    }
}
