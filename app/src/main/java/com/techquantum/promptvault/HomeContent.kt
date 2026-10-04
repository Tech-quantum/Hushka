package com.techquantum.promptvault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeContent(
    viewModel: PromptViewModel,
    onVoiceSearch: () -> Unit,
    onEditPrompt: (PromptEntity) -> Unit,
    onCopyPrompt: (PromptEntity) -> Unit,
    onOpenCategories: () -> Unit,
    providers: List<String>,
    domains: List<String>,
    categories: List<String>,
    prompts: List<PromptEntity>,
    filteredPrompts: List<PromptEntity>,
    query: String,
    selectedProvider: String,
    selectedDomain: String,
    selectedCategory: String,
    selectedCategories: Set<String>,
    selectedCollection: String,
    collections: List<String>,
    pinnedOnly: Boolean,
    recentOnly: Boolean,
    favoritesOnly: Boolean,
    sortMode: String,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val updateState by viewModel.uiState.collectAsState()
    Column(modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(8.dp))
        PromptSearchFilterSection(
            query, selectedProvider, selectedDomain, selectedCategory, favoritesOnly, sortMode,
            providers, domains, categories,
            viewModel::onQueryChanged, onVoiceSearch, viewModel::onProviderSelected,
            viewModel::onDomainSelected, viewModel::onCategorySelected, onOpenCategories,
            selectedCategories, viewModel::onCategoriesSelected, selectedCollection, collections, pinnedOnly, recentOnly, viewModel::onCollectionSelected, viewModel::onPinnedToggled, viewModel::onRecentToggled, viewModel::onFavoritesToggled, viewModel::onSortSelected
        )
        Spacer(Modifier.height(12.dp))
        PromptListHeaderSection(
            query = query, favoritesOnly = favoritesOnly, promptCount = prompts.size,
            filteredCount = filteredPrompts.size, favoriteCount = prompts.count { it.favorite }
        )
        LibraryUpdateStatus(
            messageKey = updateState.updateMessageKey,
            addedCount = updateState.updateAddedCount,
            error = updateState.updateError
        )
        Spacer(Modifier.height(12.dp))
        PromptListSection(
            prompts = filteredPrompts, query = query, listState = listState,
            onFavorite = viewModel::toggleFavorite, onPin = viewModel::togglePinned, onCopy = { prompt -> viewModel.markUsed(prompt); onCopyPrompt(prompt) },
            onEdit = onEditPrompt, onDelete = viewModel::deletePrompt,
            modifier = Modifier.weight(1f)
        )
    }
}

fun promptProviders(prompts: List<PromptEntity>): List<String> =
    prompts.map { it.provider }.filter { it.isNotBlank() }.distinct().sorted()

fun promptDomains(prompts: List<PromptEntity>): List<String> =
    prompts.map { promptDomain(it.category) }.distinct().sorted()

fun promptCategories(prompts: List<PromptEntity>): List<String> =
    prompts.map { it.category }.distinct().sorted()
