package com.techquantum.promptvault

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun HomeScaffold(
    viewModel: PromptViewModel,
    onVoiceSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAddPrompt: () -> Unit,
    onEditPrompt: (PromptEntity) -> Unit,
    onCopyPrompt: (PromptEntity) -> Unit,
    onOpenCategories: () -> Unit,
    categories: List<String>,
    selectedProvider: String,
    selectedDomain: String,
    prompts: List<PromptEntity>,
    filteredPrompts: List<PromptEntity>,
    query: String,
    selectedCategory: String,
    selectedCategories: Set<String>,
    selectedCollection: String,
    collections: List<String>,
    pinnedOnly: Boolean,
    recentOnly: Boolean,
    favoritesOnly: Boolean,
    sortMode: String,
    isUpdating: Boolean,
    onUpdateLibrary: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HomeTopBar(
                promptCount = prompts.size,
                isUpdating = isUpdating,
                onOpenSettings = onOpenSettings,
                onUpdateLibrary = onUpdateLibrary
            )
        },
        floatingActionButton = {
            HomeFloatingActionButton(onClick = onOpenAddPrompt)
        }
    ) { paddingValues ->
        HomeContent(
            viewModel = viewModel,
            onVoiceSearch = onVoiceSearch,
            onEditPrompt = onEditPrompt,
            onCopyPrompt = onCopyPrompt,
            onOpenCategories = onOpenCategories,
            providers = promptProviders(prompts),
            domains = promptDomains(prompts),
            categories = categories,
            prompts = prompts,
            filteredPrompts = filteredPrompts,
            query = query,
            selectedProvider = selectedProvider,
            selectedDomain = selectedDomain,
            selectedCategory = selectedCategory,
            selectedCategories = selectedCategories,
            selectedCollection = selectedCollection,
            collections = collections,
            pinnedOnly = pinnedOnly,
            recentOnly = recentOnly,
            favoritesOnly = favoritesOnly,
            sortMode = sortMode,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        )
    }
}
