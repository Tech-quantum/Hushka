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

private val PurplePrimary = Color(0xFF7C7CFF)
private val PurpleLight = Color(0xFFB8B8FF)
private val DarkBg = Color(0xFF1A1A2E)
private val CardDark = Color(0xFF252538)
private val CardLight = Color(0xFF2E2E45)
private val TextLight = Color(0xFFEAEAEA)
private val TextGray = Color(0xFF9E9EAE)
private val BorderColor = Color(0xFF3A3A4E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PromptViewModel,
    onSettingsClick: () -> Unit = {},
    onAddPromptClick: () -> Unit = {},
    onPromptClick: (PromptEntity) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("اخیر") }

    Scaffold(
        containerColor = DarkBg,
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
                        Text("Hushka", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "هوشکا بستری برای کاوش، خلق و مدیریت ایده‌های هوش مصنوعی",
                            fontSize = 11.sp,
                            color = TextGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextLight, modifier = Modifier.size(26.dp))
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPromptClick,
                containerColor = PurplePrimary,
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
            // Search Bar (وصل به ViewModel)
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.onQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("جستجو در عنوان، دسته‌بندی و متن پرامپت‌ها...", fontSize = 12.sp, color = TextGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        TextButton(onClick = { viewModel.onQueryChanged("") }) {
                            Text("پاک", fontSize = 11.sp, color = TextGray)
                        }
                    } else {
                        Text("🎤", fontSize = 18.sp)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = PurplePrimary,
                    unfocusedContainerColor = CardDark,
                    focusedContainerColor = CardDark,
                    unfocusedTextColor = TextLight,
                    focusedTextColor = TextLight,
                    cursorColor = PurplePrimary
                )
            )

            Spacer(Modifier.height(16.dp))

            // Categories & Filter Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { /* TODO: Open categories screen */ },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleLight.copy(alpha = 0.35f),
                        contentColor = TextLight
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("📁", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("دسته‌بندی‌ها", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { /* TODO: Open filter dialog */ },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleLight.copy(alpha = 0.35f),
                        contentColor = TextLight
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("⚙", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("فیلتر", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Provider Chips
            val providers = listOf("همه" to "", "anthropic" to "anthropic", "deepseek" to "deepseek", "gemini" to "gemini", "openai" to "openai")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(providers) { (label, value) ->
                    val isSelected = uiState.selectedProvider == value
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) PurplePrimary.copy(alpha = 0.25f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (isSelected) PurplePrimary else BorderColor),
                        onClick = { viewModel.onProviderSelected(value) }
                    ) {
                        Text(
                            if (label == "همه") "همه ارائه‌دهندگان" else label,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            color = if (isSelected) PurpleLight else TextGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Tab Chips
            val tabs = listOf("اخیر", "پین‌شده", "علاقه‌مندی‌ها")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tabs) { tab ->
                    val isSelected = selectedTab == tab
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) PurplePrimary.copy(alpha = 0.25f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (isSelected) PurplePrimary else BorderColor),
                        onClick = {
                            selectedTab = tab
                            when (tab) {
                                "اخیر" -> {
                                    if (!uiState.recentOnly) viewModel.onRecentToggled()
                                    if (uiState.pinnedOnly) viewModel.onPinnedToggled()
                                    if (uiState.favoritesOnly) viewModel.onFavoritesToggled()
                                }
                                "پین‌شده" -> {
                                    if (!uiState.pinnedOnly) viewModel.onPinnedToggled()
                                    if (uiState.recentOnly) viewModel.onRecentToggled()
                                    if (uiState.favoritesOnly) viewModel.onFavoritesToggled()
                                }
                                "علاقه‌مندی‌ها" -> {
                                    if (!uiState.favoritesOnly) viewModel.onFavoritesToggled()
                                    if (uiState.recentOnly) viewModel.onRecentToggled()
                                    if (uiState.pinnedOnly) viewModel.onPinnedToggled()
                                }
                            }
                        }
                    ) {
                        Text(
                            tab,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            color = if (isSelected) PurpleLight else TextGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Sort Dropdown (کاربردی)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                var sortExpanded by remember { mutableStateOf(false) }
                val sortLabel = when (uiState.sortMode) {
                    "latest" -> "جدیدترین"
                    "title" -> "عنوان"
                    "category" -> "دسته‌بندی"
                    "favorites" -> "علاقه‌مندی‌ها"
                    else -> "جدیدترین"
                }
                Box {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, BorderColor),
                        onClick = { sortExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("مرتب‌سازی: $sortLabel", fontSize = 12.sp, color = TextGray)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false }
                    ) {
                        listOf("latest" to "جدیدترین", "title" to "عنوان", "category" to "دسته‌بندی", "favorites" to "علاقه‌مندی‌ها").forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.onSortSelected(value)
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("پرامپت‌های من", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextLight)
                    Spacer(Modifier.height(2.dp))
                    Text("${uiState.filteredPrompts.size} پرامپت ذخیره شده", fontSize = 11.sp, color = TextGray)
                }
                IconButton(onClick = { viewModel.onFavoritesToggled() }) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (uiState.favoritesOnly) PurplePrimary else TextGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Prompt List (وصل به داده‌های واقعی)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(uiState.filteredPrompts, key = { it.id }) { prompt ->
                    PromptCard(
                        prompt = prompt,
                        onToggleFavorite = { viewModel.toggleFavorite(prompt) },
                        onTogglePin = { viewModel.togglePinned(prompt) },
                        onClick = { onPromptClick(prompt) }
                    )
                }
            }
        }
    }
}

@Composable
fun PromptCard(
    prompt: PromptEntity,
    onToggleFavorite: () -> Unit,
    onTogglePin: () -> Unit,
    onClick: () -> Unit
) {
    val tags = remember(prompt.tags) {
        prompt.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    prompt.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = TextLight
                )
                Spacer(Modifier.width(8.dp))
                Row {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CardLight,
                        modifier = Modifier.size(32.dp),
                        onClick = onTogglePin
                    ) {
                        Box(content
