package com.techquantum.hushka

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun PromptSearchFilterSection(
    query: String,
    selectedProvider: String,
    selectedDomain: String,
    selectedCategory: String,
    favoritesOnly: Boolean,
    sortMode: String,
    providers: List<String>,
    domains: List<String>,
    categories: List<String>,
    onQueryChanged: (String) -> Unit,
    onVoiceSearch: () -> Unit,
    onProviderSelected: (String) -> Unit,
    onDomainSelected: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onOpenCategories: () -> Unit,
    selectedCategories: Set<String>,
    onCategoriesSelected: (Set<String>) -> Unit,
    selectedCollection: String,
    collections: List<String>,
    pinnedOnly: Boolean,
    recentOnly: Boolean,
    onCollectionSelected: (String) -> Unit,
    onPinnedToggled: () -> Unit,
    onRecentToggled: () -> Unit,
    onFavoritesToggled: () -> Unit,
    onSortSelected: (String) -> Unit
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var draftCategories by remember(selectedCategories) { mutableStateOf(selectedCategories) }
    var collectionMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Text("⌕", style = MaterialTheme.typography.headlineSmall) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.isNotEmpty()) {
                        TextButton(onClick = { onQueryChanged("") }) {
                            Text(stringResource(R.string.clear))
                        }
                    }
                    IconButton(onClick = onVoiceSearch) {
                        Text("🎙", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }

    Spacer(Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onOpenCategories,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.open_categories))
        }

        Box(modifier = Modifier.weight(1f)) {
            Button(
                onClick = {
                    draftCategories = selectedCategories
                    filterMenuExpanded = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.filter))
            }

            DropdownMenu(
                expanded = filterMenuExpanded,
                onDismissRequest = { filterMenuExpanded = false }
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(min = 260.dp, max = 340.dp)
                        .heightIn(max = 520.dp)
                        .padding(8.dp)
                ) {
                    Text(
                        stringResource(R.string.filter_category_type),
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = { draftCategories = emptySet() }) {
                        Text(stringResource(R.string.clear_filter))
                    }
                    Column(
                        modifier = Modifier
                            .heightIn(max = 380.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        categories.forEach { category ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = category in draftCategories,
                                    onCheckedChange = { checked ->
                                        draftCategories =
                                            if (checked) draftCategories + category
                                            else draftCategories - category
                                    }
                                )
                                Text(category.replaceFirstChar { it.uppercase() })
                            }
                        }
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onCategoriesSelected(draftCategories)
                            filterMenuExpanded = false
                        }
                    ) {
                        Text(stringResource(R.string.apply_filter))
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    // ==================== Providers ====================
    if (providers.isNotEmpty()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            item {
                ModernChip(
                    label = stringResource(R.string.all_providers),
                    selected = selectedProvider == ALL_PROVIDER,
                    onClick = { onProviderSelected(ALL_PROVIDER) },
                    leadingEmoji = "✨"
                )
            }
            items(providers) { provider ->
                ModernChip(
                    label = provider,
                    selected = selectedProvider == provider,
                    onClick = { onProviderSelected(provider) }
                )
            }
        }
        Spacer(Modifier.height(6.dp))
    }

    // ==================== Domains ====================
    if (domains.isNotEmpty()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            item {
                ModernChip(
                    label = stringResource(R.string.all_domains),
                    selected = selectedDomain == ALL_DOMAIN,
                    onClick = { onDomainSelected(ALL_DOMAIN) },
                    leadingEmoji = "🌐"
                )
            }
            items(domains) { domain ->
                ModernChip(
                    label = domain,
                    selected = selectedDomain == domain,
                    onClick = { onDomainSelected(domain) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    // ==================== Secondary filters ====================
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        item {
            ModernChip(
                label = stringResource(R.string.pinned),
                selected = pinnedOnly,
                onClick = onPinnedToggled,
                leadingEmoji = "📌",
                accentColor = Color(0xFFD97757)
            )
        }
        item {
            ModernChip(
                label = stringResource(R.string.recent),
                selected = recentOnly,
                onClick = onRecentToggled,
                leadingEmoji = "⏱",
                accentColor = Color(0xFF4285F4)
            )
        }
        item {
            Box {
                ModernChip(
                    label = if (selectedCollection == ALL_COLLECTION)
                        stringResource(R.string.collections)
                    else
                        selectedCollection,
                    selected = selectedCollection != ALL_COLLECTION,
                    onClick = { collectionMenuExpanded = true },
                    leadingEmoji = "📁",
                    trailingEmoji = "⌄",
                    accentColor = Color(0xFF10A37F)
                )
                DropdownMenu(
                    expanded = collectionMenuExpanded,
                    onDismissRequest = { collectionMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.all)) },
                        onClick = {
                            onCollectionSelected(ALL_COLLECTION)
                            collectionMenuExpanded = false
                        }
                    )
                    collections.forEach { collection ->
                        DropdownMenuItem(
                            text = { Text(collection) },
                            onClick = {
                                onCollectionSelected(collection)
                                collectionMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
        item {
            ModernChip(
                label = stringResource(R.string.favorites),
                selected = favoritesOnly,
                onClick = onFavoritesToggled,
                leadingEmoji = "⭐",
                accentColor = Color(0xFFFFB300)
            )
        }
        if (selectedCategories.isNotEmpty()) {
            item {
                OutlinedButton(
                    onClick = {
                        draftCategories = emptySet()
                        onCategoriesSelected(emptySet())
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text(stringResource(R.string.clear_filter))
                }
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    // ==================== Sort ====================
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box {
            ModernChip(
                label = stringResource(
                    R.string.sort_label,
                    localizedSort(sortMode)
                ),
                selected = sortMode != SORT_LATEST,
                onClick = { sortMenuExpanded = true },
                leadingEmoji = "⇅",
                trailingEmoji = "⌄"
            )
            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false }
            ) {
                listOf(SORT_LATEST, SORT_TITLE, SORT_CATEGORY, SORT_FAVORITES)
                    .forEach { option ->
                        DropdownMenuItem(
                            text = { Text(localizedSort(option)) },
                            onClick = {
                                onSortSelected(option)
                                sortMenuExpanded = false
                            }
                        )
                    }
            }
        }
    }
}

@Composable
private fun localizedSort(value: String): String = when (value) {
    SORT_LATEST -> stringResource(R.string.sort_latest)
    SORT_TITLE -> stringResource(R.string.sort_title)
    SORT_CATEGORY -> stringResource(R.string.sort_category)
    SORT_FAVORITES -> stringResource(R.string.sort_favorites)
    else -> value
}
