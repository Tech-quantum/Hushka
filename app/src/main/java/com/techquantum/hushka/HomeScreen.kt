package com.techquantum.hushka

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PromptViewModel,
    onSettingsClick: () -> Unit = {},
    onStartVoiceSearch: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPrompt by remember { mutableStateOf<PromptEntity?>(null) }
    var previewPrompt by remember { mutableStateOf<PromptEntity?>(null) }
    var deletePrompt by remember { mutableStateOf<PromptEntity?>(null) }
    var analyzePrompt by remember { mutableStateOf<PromptEntity?>(null) }
    var liveTestPrompt by remember { mutableStateOf<PromptEntity?>(null) }
    var sortExpanded by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // === Web Prompts States ===
    val webPrompts by viewModel.webPrompts.collectAsStateWithLifecycle()
    val isFetchingWeb by viewModel.isFetchingWeb.collectAsStateWithLifecycle()
    val webError by viewModel.webError.collectAsStateWithLifecycle()
    var showWebDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isFetchingWeb) {
        if (!isFetchingWeb && (webPrompts.isNotEmpty() || webError != null)) {
            showWebDialog = true
        }
    }

    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val purplePrimary = MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hushka", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.app_subtitle),
                            fontSize = 11.sp,
                            color = textGrayColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = textColor, modifier = Modifier.size(26.dp))
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = purplePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.new_prompt_btn), fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.onQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_hint), fontSize = 12.sp, color = textGrayColor) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textGrayColor) },
                trailingIcon = {
                    IconButton(onClick = onStartVoiceSearch) { Text("🎤", fontSize = 18.sp) }
                },
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = purplePrimary,
                    unfocusedContainerColor = cardColor,
                    focusedContainerColor = cardColor,
                    unfocusedTextColor = textColor,
                    focusedTextColor = textColor,
                    cursorColor = purplePrimary
                )
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { showCategoryDialog = true },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.selectedCategory != ALL_CATEGORY) purplePrimary else cardLightColor,
                        contentColor = textColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("📁", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.categories_btn), fontSize = 14.sp)
                }
                Button(
                    onClick = { viewModel.onFavoritesToggled() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.favoritesOnly) purplePrimary else cardLightColor,
                        contentColor = textColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("⭐", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.favorites_btn), fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            val providerKeys = listOf("all", "anthropic", "deepseek", "gemini", "openai")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(providerKeys) { key ->
                    val value = if (key == "all") ALL_PROVIDER else key
                    val label = if (key == "all") stringResource(R.string.all_providers) else key
                    val isSelected = uiState.selectedProvider == value
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) purplePrimary.copy(alpha = 0.25f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (isSelected) purplePrimary else borderColor),
                        onClick = { viewModel.onProviderSelected(value) }
                    ) {
                        Text(
                            label,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            color = if (isSelected) purplePrimary else textGrayColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            val tabKeys = listOf("recent", "pinned", "favorites")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tabKeys) { key ->
                    val label = when (key) {
                        "recent" -> stringResource(R.string.recent_tab)
                        "pinned" -> stringResource(R.string.pinned_tab)
                        "favorites" -> stringResource(R.string.favorites_tab)
                        else -> key
                    }
                    val isSelected = when (key) {
                        "recent" -> uiState.recentOnly
                        "pinned" -> uiState.pinnedOnly
                        "favorites" -> uiState.favoritesOnly
                        else -> false
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) purplePrimary.copy(alpha = 0.25f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (isSelected) purplePrimary else borderColor),
                        onClick = {
                            when (key) {
                                "recent" -> viewModel.onRecentToggled()
                                "pinned" -> viewModel.onPinnedToggled()
                                "favorites" -> viewModel.onFavoritesToggled()
                            }
                        }
                    ) {
                        Text(
                            label,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            color = if (isSelected) purplePrimary else textGrayColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box {
                    val sortLabel = when (uiState.sortMode) {
                        SORT_TITLE -> stringResource(R.string.sort_title)
                        SORT_CATEGORY -> stringResource(R.string.sort_category)
                        SORT_FAVORITES -> stringResource(R.string.sort_favorites)
                        else -> stringResource(R.string.sort_latest)
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, borderColor),
                        onClick = { sortExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.sort_label, sortLabel), fontSize = 12.sp, color = textGrayColor)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textGrayColor, modifier = Modifier.size(18.dp))
                        }
                    }
                    DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                        listOf(
                            SORT_LATEST to stringResource(R.string.sort_latest),
                            SORT_TITLE to stringResource(R.string.sort_title),
                            SORT_CATEGORY to stringResource(R.string.sort_category),
                            SORT_FAVORITES to stringResource(R.string.sort_favorites)
                        ).forEach { pair ->
                            DropdownMenuItem(
                                text = { Text(pair.second) },
                                onClick = {
                                    viewModel.onSortSelected(pair.first)
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.my_prompts), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(Modifier.height(2.dp))
                    Text(stringResource(R.string.saved_count, uiState.filteredPrompts.size), fontSize = 11.sp, color = textGrayColor)
                }
                IconButton(onClick = { viewModel.updateLibrary() }) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (uiState.isUpdating) purplePrimary else textGrayColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (uiState.filteredPrompts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (uiState.query.isNotEmpty()) stringResource(R.string.no_results) else stringResource(R.string.no_prompts),
                        color = textGrayColor,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(uiState.filteredPrompts, key = { it.id }) { prompt ->
                        PromptCard(
                            title = prompt.title,
                            description = prompt.text,
                            mainTag = prompt.category,
                            tags = prompt.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            provider = prompt.collection.ifBlank { "custom" },
                            onFavoriteToggle = { viewModel.toggleFavorite(prompt) },
                            onPinToggle = { viewModel.togglePinned(prompt) },
                            onCardClick = { editingPrompt = prompt },
                            onPreviewClick = { previewPrompt = prompt },
                            onCopyClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Hushka Prompt", prompt.text))
                                Toast.makeText(context, "کپی شد!", Toast.LENGTH_SHORT).show()
                                viewModel.markUsed(prompt)
                            },
                            onAnalyzeClick = { analyzePrompt = prompt },
                            onLiveTestClick = { liveTestPrompt = prompt },
                            onEditClick = { editingPrompt = prompt },
                            onDeleteClick = { deletePrompt = prompt }
                        )
                    }
                }
            }
        }
    }

    // Add Prompt Dialog
    if (showAddDialog) {
        PromptFormDialog(
            title = stringResource(R.string.add_prompt_title),
            initialPrompt = null,
            categories = listOf("عمومی", "تصویر", "ویدئو", "متن", "کد"),
            onDismiss = { showAddDialog = false },
            onSave = { title, category, tags, text, collection ->
                viewModel.addPrompt(title, category, tags, text, collection)
                showAddDialog = false
            }
        )
    }

    // Edit Prompt Dialog
    editingPrompt?.let { prompt ->
        PromptFormDialog(
            title = stringResource(R.string.edit_prompt_title),
            initialPrompt = prompt,
            categories = listOf("عمومی", "تصویر", "ویدئو", "متن", "کد"),
            onDismiss = { editingPrompt = null },
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
                editingPrompt = null
            }
        )
    }

    // Analyze Dialog
    analyzePrompt?.let { prompt ->
    AnalyzeDialog(
        prompt = prompt,
        onDismiss = { analyzePrompt = null }
    )
}

// Live Test Dialog
    liveTestPrompt?.let { prompt ->
    LiveTestDialog(
        prompt = prompt,
        onDismiss = { liveTestPrompt = null }
    )
}

    // Preview Dialog
    previewPrompt?.let { prompt ->
        AlertDialog(
            onDismissRequest = { previewPrompt = null },
            containerColor = cardColor,
            title = { Text(prompt.title.ifBlank { "بدون عنوان" }, color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (prompt.category.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = purplePrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                getLocalizedCategory(prompt.category),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 11.sp,
                                color = purplePrimary
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    Text(
                        prompt.text,
                        color = textColor,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Hushka Prompt", prompt.text))
                    Toast.makeText(context, "کپی شد!", Toast.LENGTH_SHORT).show()
                    previewPrompt = null
                }) {
                    Text("کپی", color = purplePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { previewPrompt = null }) {
                    Text(stringResource(R.string.close), color = textGrayColor)
                }
            }
        )
    }

    // Delete Confirm Dialog
    deletePrompt?.let { prompt ->
        AlertDialog(
            onDismissRequest = { deletePrompt = null },
            containerColor = cardColor,
            title = { Text("حذف پرامپت", color = textColor, fontWeight = FontWeight.Bold) },
            text = { Text("آیا از حذف «${prompt.title}» مطمئن هستید؟", color = textGrayColor) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePrompt(prompt)
                    Toast.makeText(context, "حذف شد", Toast.LENGTH_SHORT).show()
                    deletePrompt = null
                }) {
                    Text("حذف", color = Color(0xFFE57373))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletePrompt = null }) {
                    Text("لغو", color = textGrayColor)
                }
            }
        )
    }

    // Categories Dialog
    if (showCategoryDialog) {
        val uniqueCategories = uiState.prompts
            .map { it.category }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            containerColor = cardColor,
            titleContentColor = textColor,
            textContentColor = textColor,
            title = { Text(stringResource(R.string.select_category_title), color = textColor) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                ) {
                    item {
                        val isSelected = uiState.selectedCategory == ALL_CATEGORY
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) purplePrimary else cardLightColor,
                            onClick = {
                                viewModel.onCategorySelected(ALL_CATEGORY)
                                showCategoryDialog = false
                            }
                        ) {
                            Text(
                                stringResource(R.string.all_categories),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                color = if (isSelected) Color.White else textColor,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    items(uniqueCategories) { category ->
                        val isSelected = uiState.selectedCategory == category
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) purplePrimary else cardLightColor,
                            onClick = {
                                viewModel.onCategorySelected(category)
                                showCategoryDialog = false
                            }
                        ) {
                            Text(
                                getLocalizedCategory(category),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                color = if (isSelected) Color.White else textColor,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text(stringResource(R.string.close), color = purplePrimary)
                }
            }
        )
    }

    // Web Prompts Dialog
    if (showWebDialog) {
        WebPromptsDialog(
            prompts = webPrompts,
            isLoading = isFetchingWeb,
            error = webError,
            onImport = { selected ->
                viewModel.importWebPrompts(selected)
                showWebDialog = false
            },
            onDismiss = {
                showWebDialog = false
                viewModel.clearWebPrompts()
            }
        )
    }
}

@Composable
fun PromptCard(
    title: String,
    description: String,
    mainTag: String,
    tags: List<String>,
    provider: String,
    onFavoriteToggle: () -> Unit,
    onPinToggle: () -> Unit,
    onCardClick: () -> Unit,
    onPreviewClick: () -> Unit,
    onCopyClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onLiveTestClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    title.ifBlank { "بدون عنوان" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
                Spacer(Modifier.width(8.dp))
                Row {
                    // Preview (Eye)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = cardLightColor,
                        modifier = Modifier.size(32.dp),
                        onClick = onPreviewClick
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👁", fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    // Pin
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = cardLightColor,
                        modifier = Modifier.size(32.dp),
                        onClick = onPinToggle
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📌", fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    // Favorite
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = cardLightColor,
                        modifier = Modifier.size(32.dp),
                        onClick = onFavoriteToggle
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⭐", fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (mainTag.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = purplePrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        getLocalizedCategory(mainTag),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 11.sp,
                        color = purplePrimary
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            if (tags.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(tags) { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = cardLightColor
                        ) {
                            Text(
                                tag,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 11.sp,
                                color = textColor
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            Text(
                description,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = textGrayColor
            )

            Spacer(Modifier.height(14.dp))

            // Provider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = purplePrimary.copy(alpha = 0.3f)
                ) {
                    Text(
                        provider,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            HorizontalDivider(color = cardLightColor.copy(alpha = 0.5f))

            Spacer(Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy
                ActionButton(emoji = "📋", label = "کپی", onClick = onCopyClick, color = purplePrimary)
                // Analyze
                ActionButton(emoji = "✨", label = "آنالیز", onClick = onAnalyzeClick, color = Color(0xFFFFB74D))
                // Live Test
                ActionButton(emoji = "▶", label = "تست", onClick = onLiveTestClick, color = Color(0xFF64B5F6))
                // Edit
                ActionButton(emoji = "✏️", label = "ویرایش", onClick = onEditClick, color = textGrayColor)
                // Delete
                ActionButton(emoji = "🗑", label = "حذف", onClick = onDeleteClick, color = Color(0xFFE57373))
            }
        }
    }
}

@Composable
fun ActionButton(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp)
        ) {
            Text(emoji, fontSize = 18.sp)
        }
        Text(
            label,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
