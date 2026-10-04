package com.techquantum.promptvault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
}
