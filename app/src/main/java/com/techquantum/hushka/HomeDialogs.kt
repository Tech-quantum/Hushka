package com.techquantum.hushka

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun HomeDialogs(
    viewModel: PromptViewModel,
    showSettings: Boolean,
    themeMode: String,
    colorTheme: String,
    language: String,
    huggingFaceToken: String,
    categories: List<String>,
    showAddDialog: Boolean,
    editingPrompt: PromptEntity?,
    onThemeChange: (String) -> Unit,
    onColorThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onHuggingFaceTokenChange: (String) -> Unit,
    onDismissSettings: () -> Unit,
    onDismissAdd: () -> Unit,
    onDismissEdit: () -> Unit
) {
    if (showSettings) {
        SettingsDialog(
            themeMode = themeMode,
            colorTheme = colorTheme,
            language = language,
            huggingFaceToken = huggingFaceToken,
            onThemeChange = onThemeChange,
            onColorThemeChange = onColorThemeChange,
            onLanguageChange = onLanguageChange,
            onHuggingFaceTokenChange = onHuggingFaceTokenChange,
            onExportJson = { },
            onExportMarkdown = { },
            onImportJson = { },
            onBrowseWeb = { },
            onManageColors = { },
            onDismiss = onDismissSettings
        )
    }

    if (showAddDialog) {
        PromptFormDialog(
            title = stringResource(R.string.add_prompt_title),
            initialPrompt = null,
            categories = categories,
            onDismiss = onDismissAdd,
            onSave = { title, category, tags, text, collection, role ->
                viewModel.addPromptWithRole(title, category, tags, text, collection, role)
                onDismissAdd()
            }
        )
    }

    editingPrompt?.let { prompt ->
        PromptFormDialog(
            title = stringResource(R.string.edit_prompt_title),
            initialPrompt = prompt,
            categories = categories,
            onDismiss = onDismissEdit,
            onSave = { title, category, tags, text, collection, role ->
                viewModel.updatePrompt(
                    prompt.copy(
                        title = title,
                        category = category,
                        tags = tags,
                        text = text,
                        collection = collection,
                        role = role
                    )
                )
                onDismissEdit()
            }
        )
    }
}
