package com.techquantum.hushka

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var sortExpanded by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    // رنگ‌ها از MaterialTheme
    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val purplePrimary = MaterialTheme.colorScheme.primary
    val purpleLight = MaterialTheme.colorScheme.primary

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
                            "هوشکا بستری برای کاوش، خلق و مدیریت ایده های هوش مصنوعی",
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
                Text("پرامپت جدید", fontWeight = FontWeight.Bold)
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
                placeholder = { Text("جستجو...", fontSize = 12.sp, color = textGrayColor) },
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
                    Text("دسته بندی ها", fontSize = 14.sp)
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
                    Text("علاقه مندی ها", fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            val providers = listOf(
                "همه" to ALL_PROVIDER,
                "anthropic" to "anthropic",
                "deepseek" to "deepseek",
                "gemini" to "gemini",
                "openai" to "openai"
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(providers) { pair ->
                    val label = pair.first
                    val value = pair.second
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
                            color = if (isSelected) purpleLight else textGrayColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            val tabs = listOf("اخیر", "پین شده", "علاقه مندی ها")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tabs) { tab ->
                    val isSelected = when (tab) {
                        "اخیر" -> uiState.recentOnly
                        "پین شده" -> uiState.pinnedOnly
                        "علاقه مندی ها" -> uiState.favoritesOnly
                        else -> false
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) purplePrimary.copy(alpha = 0.25f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (isSelected) purplePrimary else borderColor),
                        onClick = {
                            when (tab) {
                                "اخیر" -> viewModel.onRecentToggled()
                                "پین شده" -> viewModel.onPinnedToggled()
                                "علاقه مندی ها" -> viewModel.onFavoritesToggled()
                            }
                        }
                    ) {
                        Text(
                            tab,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            color = if (isSelected) purpleLight else textGrayColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, borderColor),
                        onClick = { sortExpanded = true }
                    ) {
                        val sortLabel = when (uiState.sortMode) {
                            SORT_TITLE -> "عنوان"
                            SORT_CATEGORY -> "دسته بندی"
                            SORT_FAVORITES -> "علاقه مندی ها"
                            else -> "جدیدترین"
                        }
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("مرتب سازی: $sortLabel", fontSize = 12.sp, color = textGrayColor)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textGrayColor, modifier = Modifier.size(18.dp))
                        }
                    }
                    DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                        listOf(
                            SORT_LATEST to "جدیدترین",
                            SORT_TITLE to "عنوان",
                            SORT_CATEGORY to "دسته بندی",
                            SORT_FAVORITES to "علاقه مندی ها"
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
                    Text("پرامپت های من", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(Modifier.height(2.dp))
                    Text("${uiState.filteredPrompts.size} پرامپت ذخیره شده", fontSize = 11.sp, color = textGrayColor)
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
                        if (uiState.query.isNotEmpty()) "چیزی پیدا نشد" else "هنوز پرامپتی اضافه نشده",
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
                            onCardClick = { editingPrompt = prompt }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        PromptFormDialog(
            title = "افزودن پرامپت جدید",
            initialPrompt = null,
            categories = listOf("عمومی", "تصویر", "ویدئو", "متن", "کد"),
            onDismiss = { showAddDialog = false },
            onSave = { title, category, tags, text, collection ->
                viewModel.addPrompt(title, category, tags, text, collection)
                showAddDialog = false
            }
        )
    }

    editingPrompt?.let { prompt ->
        PromptFormDialog(
            title = "ویرایش پرامپت",
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
            title = { Text("انتخاب دسته‌بندی", color = textColor) },
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
                                "همه دسته‌ها",
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
                                category,
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
                    Text("بستن", color = purplePrimary)
                }
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
    onCardClick: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary
    val purpleLight = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onCardClick
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                    Spacer(Modifier.width(8.dp))
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
                        mainTag,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 11.sp,
                        color = purpleLight
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
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
        }
    }
}
