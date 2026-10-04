package com.techquantum.promptvault

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun HomeDialogs(
    viewModel: PromptViewModel,
    showSettings: Boolean,
    themeMode: String,
    language: String,
    categories: List<String>,
    showAddDialog: Boolean,
    editingPrompt: PromptEntity?,
    onThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onDismissSettings: () -> Unit,
    onDismissAdd: () -> Unit,
    onDismissEdit: () -> Unit
) {
    if (showSettings) {
        SettingsDialog(
            themeMode = themeMode,
            language = language,
            onThemeChange = onThemeChange,
            onLanguageChange = onLanguageChange,
            onDismiss = onDismissSettings
        )
    }

    if (showAddDialog) {
        PromptFormDialog(
            title = stringResource(R.string.add_prompt),
            initialPrompt = null,
            categories = categories,
            onDismiss = onDismissAdd,
            onSave = { title, category, tags, text, collection ->
                viewModel.addPrompt(title, category, tags, text, collection)
                onDismissAdd()
            }
        )
    }

    editingPrompt?.let { prompt ->
        PromptFormDialog(
            title = stringResource(R.string.edit_prompt),
            initialPrompt = prompt,
            categories = categories,
            onDismiss = onDismissEdit,
            onSave = { title, category, tags, text, collection ->
                viewModel.updatePrompt(
                    prompt.copy(
                        title = title,
                        category = category,
                        tags = tags,
                        text = text,
                        collection = collection
                    )
                )
                onDismissEdit()
            }
        )
    }
}
