@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.techquantum.hushka

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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

// ==================== Category Directory ====================

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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("🗂", style = MaterialTheme.typography.titleLarge)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getLocalizedCategory(category),
                    maxLines = 2,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.category_prompt_count, count),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==================== Category Prompts Screen ====================

@Composable
fun CategoryPromptsScreen(
    category: String,
    prompts: List<PromptEntity>,
    onBack: () -> Unit,
    onFavoriteToggle: (PromptEntity) -> Unit,
    onPinToggle: (PromptEntity) -> Unit,
    onCopyClick: (PromptEntity) -> Unit,
    onShareClick: (PromptEntity) -> Unit,
    onAnalyzeClick: (PromptEntity) -> Unit,
    onLiveTestClick: (PromptEntity) -> Unit,
    onEditClick: (PromptEntity) -> Unit,
    onDeleteClick: (PromptEntity) -> Unit,
    onPreviewClick: (PromptEntity) -> Unit,
    onCardClick: (PromptEntity) -> Unit
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
                        Text(getLocalizedCategory(category))
                        Text(
                            stringResource(R.string.category_prompt_count, categoryPrompts.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.search_in_category)) },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.height(12.dp))
            PromptListSection(
                prompts = visiblePrompts,
                query = query,
                listState = listState,
                onFavoriteToggle = onFavoriteToggle,
                onPinToggle = onPinToggle,
                onCopyClick = onCopyClick,
                onShareClick = onShareClick,
                onAnalyzeClick = onAnalyzeClick,
                onLiveTestClick = onLiveTestClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onPreviewClick = onPreviewClick,
                onCardClick = onCardClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
