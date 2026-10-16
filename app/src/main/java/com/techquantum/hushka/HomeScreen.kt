package com.techquantum.hushka

import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techquantum.hushka.ChatScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

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
    var activeChatPrompt by remember { mutableStateOf<String?>(null) }
    var deletePrompt by remember { mutableStateOf<PromptEntity?>(null) }
    var analyzePrompt by remember { mutableStateOf<PromptEntity?>(null) }
    var liveTestPrompt by remember { mutableStateOf<PromptEntity?>(null) }
    var sortExpanded by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var languageMenuExpanded by remember { mutableStateOf(false) }
    val appContext = LocalContext.current

    // ============ Statistics Screen ============
    if (showStats) {
        StatisticsScreen(
            prompts = uiState.prompts,
            onBack = { showStats = false }
        )
        return
    }

    // وضعیت اتصال به اینترنت
    var isOnline by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val currentNetwork = connectivityManager.activeNetwork
        val currentCaps = connectivityManager.getNetworkCapabilities(currentNetwork)
        isOnline = currentCaps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline = true
            }
            override fun onLost(network: Network) {
                val activeNetwork = connectivityManager.activeNetwork
                val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
                isOnline = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            }
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                isOnline = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        onDispose {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // === Web Prompts States ===
    val webPrompts by viewModel.webPrompts.collectAsStateWithLifecycle()
    val isFetchingWeb by viewModel.isFetchingWeb.collectAsStateWithLifecycle()
    val isTranslating by viewModel.isTranslating.collectAsStateWithLifecycle()
    val webError by viewModel.webError.collectAsStateWithLifecycle()
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    val selectedWebSource by viewModel.selectedWebSource.collectAsStateWithLifecycle()

    // 🌐 In-app translation state
    val translateTargetPrompt by viewModel.translateTargetPrompt.collectAsStateWithLifecycle()
    val translateLoading by viewModel.translateLoading.collectAsStateWithLifecycle()
    val translatedResult by viewModel.translatedResult.collectAsStateWithLifecycle()
    val translateError by viewModel.translateError.collectAsStateWithLifecycle()
    var showWebDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isFetchingWeb) {
        if (isFetchingWeb) {
            showWebDialog = true
        } else if (webPrompts.isNotEmpty() || webError != null) {
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

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val isScrolledDown by remember {
        derivedStateOf { listState.canScrollBackward }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = bgColor,
        topBar = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 💎 لوگو — همیشه چپ (مستقل از زبان)
                    HushkaLogo(size = 40.dp)

                    Spacer(Modifier.width(8.dp))

                    // عنوان + زیرنویس — وسط‌چین
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Hushka",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            stringResource(R.string.app_subtitle),
                            fontSize = 11.sp,
                            color = textGrayColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Spacer تقارن (به اندازه لوگو + فاصله)
                    Spacer(Modifier.width(72.dp))
                }
            }
        },
        bottomBar = {
            HomeBottomBar(
                categoriesActive = uiState.selectedCategory != ALL_CATEGORY,
                favoritesActive = uiState.favoritesOnly,
                isOnline = isOnline,
                isUpdating = uiState.isUpdating,
                onCategories = { showCategoryDialog = true },
                onFavorites = { viewModel.onFavoritesToggled() },
                onAdd = { showAddDialog = true },
                onUpdate = {
                    if (!uiState.isUpdating) {
                        if (isOnline) {
                            viewModel.updateLibrary()
                        } else {
                            Toast.makeText(appContext, appContext.getString(R.string.toast_offline), Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onStats = { showStats = true },
                onSettings = onSettingsClick
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ===== Search Bar =====
            item {
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
            }

            // ===== Provider Chips =====
            item {
                val providerKeys = listOf("all", "anthropic", "deepseek", "gemini", "openai")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(providerKeys) { key ->
                        val value = if (key == "all") ALL_PROVIDER else key
                        val label = if (key == "all") stringResource(R.string.all_providers_short) else key
                        val isSelected = uiState.selectedProvider == value
                        ModernChip(
                            label = label,
                            selected = isSelected,
                            onClick = { viewModel.onProviderSelected(value) },
                            leadingEmoji = if (key == "all") "✨" else null
                        )
                    }
                    // ===== چیپ بومی (فیلتر زبان) =====
                    item {
                        val langs = listOf(
                            Triple(ALL_LANGUAGE, "همه", "🌐"),
                            Triple("fa", "فارسی", "🇮🇷"),
                            Triple("en", "English", "🇬🇧"),
                            Triple("ar", "العربية", "🇸🇦"),
                            Triple("tr", "Türkçe", "🇹🇷")
                        )
                        val selectedName = langs.firstOrNull { it.first == uiState.selectedLanguage }?.second
                        val chipLabel = if (uiState.selectedLanguage == ALL_LANGUAGE || selectedName == null) {
                            "بومی"
                        } else {
                            "بومی / $selectedName"
                        }

                        Box {
                            ModernChip(
                                label = chipLabel,
                                selected = uiState.selectedLanguage != ALL_LANGUAGE,
                                onClick = { languageMenuExpanded = true },
                                leadingEmoji = "🗣️"
                            )
                            DropdownMenu(
                                expanded = languageMenuExpanded,
                                onDismissRequest = { languageMenuExpanded = false }
                            ) {
                                langs.forEach { lang ->
                                    val isCurrent = lang.first == uiState.selectedLanguage
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(lang.third, fontSize = 16.sp)
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    lang.second,
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (isCurrent) {
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("✓", color = purplePrimary, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        },
                                        onClick = {
                                            languageMenuExpanded = false
                                            viewModel.onLanguageSelected(lang.first)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ===== Tab Chips =====
            item {
                val tabKeys = listOf("recent", "pinned", "favorites")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
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
                        val emoji = when (key) {
                            "recent" -> "⏱"
                            "pinned" -> "📌"
                            "favorites" -> "⭐"
                            else -> null
                        }
                        ModernChip(
                            label = label,
                            selected = isSelected,
                            onClick = {
                                when (key) {
                                    "recent" -> viewModel.onRecentToggled()
                                    "pinned" -> viewModel.onPinnedToggled()
                                    "favorites" -> viewModel.onFavoritesToggled()
                                }
                            },
                            leadingEmoji = emoji
                        )
                    }
                }
            }

            // ===== Sort Dropdown =====
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Box {
                        val sortLabel = when (uiState.sortMode) {
                            SORT_RELEVANT -> stringResource(R.string.sort_relevant)
                            SORT_MOST_VIEWED -> stringResource(R.string.sort_most_viewed)
                            SORT_TITLE -> stringResource(R.string.sort_title)
                            SORT_CATEGORY -> stringResource(R.string.sort_category)
                            SORT_FAVORITES -> stringResource(R.string.sort_favorites)
                            SORT_FASTEST -> stringResource(R.string.sort_fastest)
                            SORT_HIGHEST_SCORE -> stringResource(R.string.sort_highest_score)
                            SORT_RECOMMENDED -> stringResource(R.string.sort_recommended)
                            SORT_FEATURED -> stringResource(R.string.sort_featured)
                            else -> stringResource(R.string.sort_latest)
                        }
                        ModernChip(
                            label = stringResource(R.string.sort_label, sortLabel),
                            selected = uiState.sortMode != SORT_LATEST,
                            onClick = { sortExpanded = true },
                            leadingEmoji = "⇅",
                            trailingEmoji = "⌄"
                        )
                        DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                            listOf(
                                SORT_RELEVANT to stringResource(R.string.sort_relevant),
                                SORT_MOST_VIEWED to stringResource(R.string.sort_most_viewed),
                                SORT_LATEST to stringResource(R.string.sort_latest),
                                SORT_FAVORITES to stringResource(R.string.sort_favorites),
                                SORT_TITLE to stringResource(R.string.sort_title),
                                SORT_CATEGORY to stringResource(R.string.sort_category),
                                SORT_FASTEST to stringResource(R.string.sort_fastest),
                                SORT_HIGHEST_SCORE to stringResource(R.string.sort_highest_score),
                                SORT_RECOMMENDED to stringResource(R.string.sort_recommended),
                                SORT_FEATURED to stringResource(R.string.sort_featured)
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
            }

            // ===== Section Header =====
            item {
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
            }

            // ===== Prompt List =====
            if (uiState.filteredPrompts.isEmpty()) {
                item {
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
                }
            } else {
                itemsIndexed(uiState.filteredPrompts, key = { _, item -> item.id }) { index, prompt ->
                    PromptCard(
                        title = prompt.title,
                        description = prompt.text,
                        mainTag = prompt.category,
                        tags = prompt.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        provider = prompt.collection.ifBlank { "custom" },
                        roleId = prompt.role,
                        isPinned = prompt.pinned,
                        isFavorite = prompt.favorite,
                        lastUsedAt = prompt.lastUsedAt,
                        onFavoriteToggle = { viewModel.toggleFavorite(prompt) },
                        onPinToggle = { viewModel.togglePinned(prompt) },
                        onCardClick = { editingPrompt = prompt },
                        onPreviewClick = { previewPrompt = prompt },
                        onCopyClick = {
                            val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Hushka Prompt", prompt.text))
                            Toast.makeText(appContext, appContext.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
                            viewModel.markUsed(prompt)
                        },
                        onShareClick = {
                            activeChatPrompt = prompt.text
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, prompt.text)
                                    putExtra(Intent.EXTRA_SUBJECT, prompt.title)
                                }
                                appContext.startActivity(Intent.createChooser(shareIntent, appContext.getString(R.string.share_chooser)))
                                viewModel.markUsed(prompt)
                            } catch (e: Exception) {
                                Toast.makeText(appContext, appContext.getString(R.string.no_app_found), Toast.LENGTH_SHORT).show()
                            }
                        },
                        onTranslateClick = {
                            val targetLang = AppSettings.getLanguage(appContext)
                            viewModel.startPromptTranslation(prompt, targetLang)
                        },
                        onAnalyzeClick = { analyzePrompt = prompt },
                        onLiveTestClick = { liveTestPrompt = prompt },
                        onEditClick = { editingPrompt = prompt },
                        onDeleteClick = { deletePrompt = prompt },
                        onSwipeToDelete = { deletePrompt = prompt },
                        itemIndex = index
                    )
                }
            }
        }
    }


        // 🚀 Scroll-to-Top FAB — همیشه چپ، مستقل از زبان
        // Box داخلی با LTR اجباری برای اینکه align از والد RTL تأثیر نگیرد
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = isScrolledDown,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 100.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = { scope.launch { listState.animateScrollToItem(0) } },
                        containerColor = purplePrimary,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = "برو به بالا"
                        )
                    }
                }
            }
        }
    }

    // ============ Add Prompt Dialog ============
    if (showAddDialog) {
        PromptFormDialog(
            title = stringResource(R.string.add_prompt_title),
            initialPrompt = null,
            categories = listOf("عمومی", "تصویر", "ویدئو", "متن", "کد"),
            onDismiss = { showAddDialog = false },
            onSave = { title, category, tags, text, collection, role ->
                viewModel.addPromptWithRole(title, category, tags, text, collection, role)
                showAddDialog = false
            }
        )
    }

    // ============ Edit Prompt Dialog ============
    editingPrompt?.let { prompt ->
        PromptFormDialog(
            title = stringResource(R.string.edit_prompt_title),
            initialPrompt = prompt,
            categories = listOf("عمومی", "تصویر", "ویدئو", "متن", "کد"),
            onDismiss = { editingPrompt = null },
            onSave = { title, category, tags, text, collection, role ->
                viewModel.updatePrompt(
                    prompt.copy(
                        title = title,
                        category = category,
                        tags = tags,
                        text = text,
                        collection = collection,
                        role = role
                    )
                )
                editingPrompt = null
            }
        )
    }

    // ============ Analyze Dialog ============
    analyzePrompt?.let { prompt ->
        AdvancedAnalyzeDialog(
            prompt = prompt,
            onDismiss = { analyzePrompt = null },
            onReplace = {
                viewModel.updatePrompt(prompt.copy(text = prompt.text))
            },
            onSaveAsNew = {
                viewModel.addPrompt(
                    title = appContext.getString(R.string.upgraded_suffix, prompt.title),
                    category = prompt.category,
                    tags = prompt.tags,
                    text = prompt.text,
                    collection = prompt.collection
                )
            },
            onTestInSimulator = {
                analyzePrompt = null
                liveTestPrompt = prompt
            },
            onApplyRole = { newText ->
                val updated = prompt.copy(text = newText)
                viewModel.updatePrompt(updated)
                analyzePrompt = updated
            }
        )
    }

    // ============ Live Test Dialog ============
    liveTestPrompt?.let { prompt ->
        LiveTestDialog(
            prompt = prompt,
            onDismiss = { liveTestPrompt = null }
        )
    }

    // ============ Preview Dialog ============
    previewPrompt?.let { prompt ->
        AlertDialog(
            onDismissRequest = { previewPrompt = null },
            containerColor = cardColor,
            title = { Text(prompt.title.ifBlank { stringResource(R.string.untitled) }, color = textColor, fontWeight = FontWeight.Bold) },
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
                    val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Hushka Prompt", prompt.text))
                    Toast.makeText(appContext, appContext.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
                    previewPrompt = null
                }) {
                    Text(stringResource(R.string.copy), color = purplePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { previewPrompt = null }) {
                    Text(stringResource(R.string.close), color = textGrayColor)
                }
            }
        )
    }

    // ============ Delete Confirm Dialog ============
    deletePrompt?.let { prompt ->
        AlertDialog(
            onDismissRequest = { deletePrompt = null },
            containerColor = cardColor,
            title = { Text(stringResource(R.string.delete_prompt_title), color = textColor, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_prompt_message, prompt.title), color = textGrayColor) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePrompt(prompt)
                    Toast.makeText(appContext, appContext.getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show()
                    deletePrompt = null
                }) {
                    Text(stringResource(R.string.delete), color = Color(0xFFE57373))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletePrompt = null }) {
                    Text(stringResource(R.string.cancel), color = textGrayColor)
                }
            }
        )
    }

    // ============ Categories Dialog ============
    if (showCategoryDialog) {
        CategoryPickerDialog(
            prompts = uiState.prompts,
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = { cat ->
                viewModel.onCategorySelected(cat)
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }

    // ============ Translated Prompt Dialog ============
    translateTargetPrompt?.let { prompt ->
        TranslatedPromptDialog(
            originalTitle = prompt.title,
            sourceLang = LanguageDetector.detect(prompt.text),
            targetLang = AppSettings.getLanguage(appContext),
            isLoading = translateLoading,
            translatedText = translatedResult,
            error = translateError,
            onRetry = {
                val targetLang = AppSettings.getLanguage(appContext)
                viewModel.retryPromptTranslation(targetLang)
            },
            onDismiss = { viewModel.dismissPromptTranslation() }
        )
    }

    // ============ Web Prompts Dialog ============
    if (showWebDialog) {
        WebPromptsDialog(
            prompts = webPrompts,
            isLoading = isFetchingWeb,
            loadingMessage = loadingMessage,
            error = webError,
            isTranslating = isTranslating,
            selectedSource = selectedWebSource,
            onSourceSelected = {
                viewModel.onWebSourceSelected(it)
                viewModel.fetchWebPrompts()
            },
            onTranslate = { selected ->
                viewModel.translateWebPrompts(selected)
            },
            onRefresh = { viewModel.refreshWebPrompts() },
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

    // ═══════ ChatScreen Overlay ═══════
    activeChatPrompt?.let { promptText ->
        ChatScreen(
            initialPrompt = promptText,
            isRtl = true,
            onDismiss = { activeChatPrompt = null }
        )
    }
}
