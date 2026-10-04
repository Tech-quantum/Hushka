package com.techquantum.promptvault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    listState: androidx.compose.foundation.lazy.LazyListState,
    onFavorite: (PromptEntity) -> Unit,
    onPin: (PromptEntity) -> Unit,
    onCopy: (PromptEntity) -> Unit,
    onEdit: (PromptEntity) -> Unit,
    onDelete: (PromptEntity) -> Unit,
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
                    prompt = prompt,
                    onFavorite = { onFavorite(prompt) },
                    onPin = { onPin(prompt) },
                    onCopy = { onCopy(prompt) },
                    onEdit = { onEdit(prompt) },
                    onDelete = { onDelete(prompt) }
                )
            }
        }
    }
}

@Composable
private fun EmptyPromptState(query: String) {
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
            androidx.compose.material3.Text(
                "✦",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.Text(
                if (query.isBlank()) stringResource(R.string.no_prompts) else stringResource(R.string.no_results),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            androidx.compose.material3.Text(
                if (query.isBlank()) stringResource(R.string.empty_hint) else stringResource(R.string.empty_search_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
