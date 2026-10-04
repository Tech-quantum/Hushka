package com.techquantum.promptvault

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptFormDialog(
    title: String,
    initialPrompt: PromptEntity?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var promptTitle by rememberSaveable(initialPrompt?.id) { mutableStateOf(initialPrompt?.title.orEmpty()) }
    var category by rememberSaveable(initialPrompt?.id) { mutableStateOf(initialPrompt?.category.orEmpty()) }
    var tags by rememberSaveable(initialPrompt?.id) { mutableStateOf(initialPrompt?.tags.orEmpty()) }
    var text by rememberSaveable(initialPrompt?.id) { mutableStateOf(initialPrompt?.text.orEmpty()) }
    var collection by rememberSaveable(initialPrompt?.id) { mutableStateOf(initialPrompt?.collection.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = promptTitle,
                    onValueChange = { promptTitle = it },
                    label = { Text(stringResource(R.string.title)) },
                    placeholder = { Text(stringResource(R.string.title_placeholder)) },
                    singleLine = true
                )
                var categoryMenuExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(stringResource(R.string.category)) },
                        placeholder = { Text(stringResource(R.string.category_placeholder)) },
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        categories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    category = option
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text(stringResource(R.string.tags)) },
                    placeholder = { Text(stringResource(R.string.tags_placeholder)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = collection,
                    onValueChange = { collection = it },
                    label = { Text(stringResource(R.string.collection)) },
                    placeholder = { Text(stringResource(R.string.collection_placeholder)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.prompt_text)) },
                    placeholder = { Text(stringResource(R.string.prompt_text_placeholder)) },
                    minLines = 5,
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = promptTitle.isNotBlank() && text.isNotBlank(),
                onClick = {
                    onSave(
                        promptTitle.trim(),
                        category.trim().ifBlank { "Custom" },
                        tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.distinct().joinToString(", "),
                        text.trim(),
                        collection.trim()
                    )
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
