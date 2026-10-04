@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.techquantum.promptvault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CategoryDirectoryScreen(
    prompts: List<PromptEntity>,
    onBack: () -> Unit,
    onSelectCategory: (String) -> Unit
) {
    val gridState = rememberLazyGridState()
    val counts = remember(prompts) { prompts.groupingBy { it.category }.eachCount() }
    val categories = remember(prompts) { prompts.map { it.category }.distinct().sorted() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.categories_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            state = gridState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories, key = { it }) { category ->
                CategoryButton(
                    category = category,
                    count = counts[category] ?: 0,
                    onClick = { onSelectCategory(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryButton(
    category: String,
    count: Int,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(82.dp),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("▦", style = MaterialTheme.typography.titleLarge)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.substringAfter('.', category),
                    maxLines = 2,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.category_prompt_count, count),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun CategoryPromptsScreen(
    category: String,
    prompts: List<PromptEntity>,
    onBack: () -> Unit,
    onFavorite: (PromptEntity) -> Unit,
    onPin: (PromptEntity) -> Unit,
    onCopy: (PromptEntity) -> Unit,
    onEdit: (PromptEntity) -> Unit,
    onDelete: (PromptEntity) -> Unit
) {
    var query by remember(category) { mutableStateOf("") }
    val categoryPrompts = remember(category, prompts) {
        prompts.filter { it.category == category }
    }
    val visiblePrompts = remember(categoryPrompts, query) {
        val q = query.trim()
        if (q.isBlank()) categoryPrompts else SmartSearchEngine.rank(categoryPrompts, q)
    }
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(category.substringAfter('.', category))
                        Text(
                            stringResource(R.string.category_prompt_count, categoryPrompts.size),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.search_in_category)) },
                shape = RoundedCornerShape(18.dp)
            )
            Spacer(Modifier.height(12.dp))
            PromptListSection(
                prompts = visiblePrompts,
                query = query,
                listState = listState,
                onFavorite = onFavorite,
                onPin = onPin,
                onCopy = onCopy,
                onEdit = onEdit,
                onDelete = onDelete,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
