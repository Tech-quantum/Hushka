package com.techquantum.hushka

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PromptListSection(
    prompts: List<PromptEntity>,
    query: String,
    listState: LazyListState,
    onFavoriteToggle: (PromptEntity) -> Unit,
    onPinToggle: (PromptEntity) -> Unit,
    onCopyClick: (PromptEntity) -> Unit,
    onShareClick: (PromptEntity) -> Unit,
    onAnalyzeClick: (PromptEntity) -> Unit,
    onLiveTestClick: (PromptEntity) -> Unit,
    onEditClick: (PromptEntity) -> Unit,
    onDeleteClick: (PromptEntity) -> Unit,
    onPreviewClick: (PromptEntity) -> Unit,
    onCardClick: (PromptEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (prompts.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth()
        ) {
            EmptyPromptState(query)
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 2.dp, bottom = 96.dp)
        ) {
            items(prompts, key = { it.id }) { prompt ->
                PromptCard(
                    title = prompt.title,
                    description = prompt.text,
                    mainTag = prompt.category,
                    tags = prompt.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    provider = prompt.collection.ifBlank { "custom" },
                    roleId = prompt.role,
                    onFavoriteToggle = { onFavoriteToggle(prompt) },
                    onPinToggle = { onPinToggle(prompt) },
                    onCardClick = { onCardClick(prompt) },
                    onPreviewClick = { onPreviewClick(prompt) },
                    onCopyClick = { onCopyClick(prompt) },
                    onShareClick = { onShareClick(prompt) },
                    onAnalyzeClick = { onAnalyzeClick(prompt) },
                    onLiveTestClick = { onLiveTestClick(prompt) },
                    onEditClick = { onEditClick(prompt) },
                    onDeleteClick = { onDeleteClick(prompt) }
                )
            }
        }
    }
}

@Composable
fun EmptyPromptState(query: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "✨",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (query.isBlank()) stringResource(R.string.no_prompts) else stringResource(R.string.no_results),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (query.isBlank()) stringResource(R.string.empty_hint) else stringResource(R.string.empty_search_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
