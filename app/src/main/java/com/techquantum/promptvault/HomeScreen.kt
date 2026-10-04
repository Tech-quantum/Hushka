package com.techquantum.promptvault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat

@Composable
fun PromptVaultApp(viewModel: PromptViewModel, onVoiceSearch: () -> Unit) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var editingPromptId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var categoryDirectoryOpen by rememberSaveable { mutableStateOf(false) }
    var selectedCategoryPage by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var themeMode by rememberSaveable { mutableStateOf(AppSettings.getTheme(context)) }
    var language by rememberSaveable { mutableStateOf(AppSettings.getLanguage(context)) }
    val uiState by viewModel.uiState.collectAsState()
    val prompts = uiState.prompts
    // Look the prompt up fresh so saving an edit never overwrites favorite/pinned/lastUsedAt
    // changes made while the dialog was open.
    val editingPrompt = editingPromptId?.let { id -> prompts.firstOrNull { it.id == id } }
    val filteredPrompts = uiState.filteredPrompts
    val providers = remember(prompts) { promptProviders(prompts) }
    val domains = remember(prompts) { promptDomains(prompts) }
    val collections = remember(prompts) { prompts.map { it.collection }.filter { it.isNotBlank() }.distinct().sorted() }
    val categories = remember(prompts, uiState.selectedProvider, uiState.selectedDomain) {
        promptCategories(prompts.filter {
            (uiState.selectedProvider == ALL_PROVIDER || it.provider == uiState.selectedProvider) &&
                (uiState.selectedDomain == ALL_DOMAIN || promptDomain(it.category) == uiState.selectedDomain)
        })
    }

    BackHandler(enabled = selectedCategoryPage != null || categoryDirectoryOpen) {
        if (selectedCategoryPage != null) {
            selectedCategoryPage = null
            categoryDirectoryOpen = true
        } else {
            categoryDirectoryOpen = false
        }
    }

    PromptVaultTheme(themeMode = themeMode) {
        when {
            selectedCategoryPage != null -> {
                CategoryPromptsScreen(
                    category = selectedCategoryPage!!,
                    prompts = prompts,
                    onBack = { selectedCategoryPage = null; categoryDirectoryOpen = true },
                    onFavorite = viewModel::toggleFavorite,
                    onPin = viewModel::togglePinned,
                    onCopy = { prompt -> viewModel.markUsed(prompt); copyPrompt(context, prompt.text) },
                    onEdit = { editingPromptId = it.id },
                    onDelete = viewModel::deletePrompt
                )
            }
            categoryDirectoryOpen -> {
                CategoryDirectoryScreen(
                    prompts = prompts,
                    onBack = { categoryDirectoryOpen = false },
                    onSelectCategory = {
                        selectedCategoryPage = it
                        categoryDirectoryOpen = false
                    }
                )
            }
            else -> {
                HomeScaffold(
                    viewModel = viewModel,
                    onVoiceSearch = onVoiceSearch,
                    onOpenSettings = { showSettings = true },
                    onOpenAddPrompt = { showAddDialog = true },
                    onEditPrompt = { editingPromptId = it.id },
                    onCopyPrompt = { prompt -> copyPrompt(context, prompt.text) },
                    onOpenCategories = { categoryDirectoryOpen = true },
                    categories = categories,
                    selectedProvider = uiState.selectedProvider,
                    selectedDomain = uiState.selectedDomain,
                    prompts = prompts,
                    filteredPrompts = filteredPrompts,
                    query = uiState.query,
                    selectedCategory = uiState.selectedCategory,
                    selectedCategories = uiState.selectedCategories,
                    selectedCollection = uiState.selectedCollection,
                    collections = collections,
                    pinnedOnly = uiState.pinnedOnly,
                    recentOnly = uiState.recentOnly,
                    favoritesOnly = uiState.favoritesOnly,
                    sortMode = uiState.sortMode,
                    isUpdating = uiState.isUpdating,
                    onUpdateLibrary = viewModel::updateLibrary
                )
            }
        }
    }

    HomeDialogs(
        viewModel = viewModel, showSettings = showSettings, themeMode = themeMode, language = language,
        categories = categories, showAddDialog = showAddDialog, editingPrompt = editingPrompt,
        onThemeChange = { themeMode = it; AppSettings.setTheme(context, it) },
        onLanguageChange = { language = it; AppSettings.setLanguage(context, it); AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(it)) },
        onDismissSettings = { showSettings = false }, onDismissAdd = { showAddDialog = false },
        onDismissEdit = { editingPromptId = null }
    )
}

private fun copyPrompt(context: Context, text: String) {
    if (text.isBlank()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("PromptVault prompt", text))
    Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show()
}
