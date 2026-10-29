package com.techquantum.hushka
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.lazy.LazyRow

import android.app.Application
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@Composable
fun ChatScreen(
    initialPrompt: String,
    isRtl: Boolean = true,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val clipboard = LocalClipboardManager.current

    val factory = remember(initialPrompt) {
        ChatViewModelFactory(application, initialPrompt)
    }
    val viewModel: ChatViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ttsHelper = remember { TtsHelper(context) }
    
    DisposableEffect(Unit) {
        onDispose { ttsHelper.shutdown() }
    }
    val listState = rememberLazyListState()

    // === 🔄 همگام‌سازی مدل با تنظیمات (رفع باگ دیالوگ و چت) ===
    LaunchedEffect(Unit) {
        val savedProviderId = AppSettings.getAiProvider(context)
        if (viewModel.uiState.value.provider.id != savedProviderId) {
            viewModel.updateProvider(AiProvider.fromId(savedProviderId))
        }
    }


    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        if (uiState.messages.isNotEmpty()) {
            delay(60)
            listState.animateScrollToItem((uiState.messages.size - 1).coerceAtLeast(0))
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "بستن")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isRtl) "چت هوشکا" else "Hushka Chat",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.provider.displayName} • ${uiState.messages.size} پیام",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                        if (uiState.messages.isNotEmpty()) {
                            IconButton(onClick = { viewModel.regenerate() }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "تولید مجدد")
                            }
                        }
                        IconButton(onClick = { viewModel.clearChat() }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "پاک کردن")
                        }
                    }
                }

                if (!uiState.hasApiKey) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFF3CD)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "کلید API تنظیم نشده",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF856404)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "از تنظیمات، ${uiState.provider.displayName} را فعال کن و کلید را وارد کن.",
                                color = Color(0xFF856404),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // === 📊 نوار انتخاب سریع مدل ===
                ModelSelectorChips(
                    selectedProvider = uiState.provider.id,
                    onProviderSelected = { 
                        viewModel.updateProvider(AiProvider.fromId(it)) 
                    },
                    isRtl = isRtl,
                    onLocalLanguageSelected = { lang ->
                        AppSettings.setLanguage(context, lang)
                        Toast.makeText(context, "زبان بومی: $lang", Toast.LENGTH_SHORT).show()
                    }
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = uiState.messages, key = { it.id }) { message ->
                        ChatBubble(
                            message = message,
                            isRtl = isRtl,
                            onCopy = { clipboard.setText(AnnotatedString(message.content)) },
                            onSpeak = { ttsHelper.speak(message.content, AppSettings.getLanguage(context)) }
                        )
                    }
                    if (uiState.isLoading) {
                        item(key = "typing") { TypingIndicator(isRtl = isRtl) }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.inputText,
                            onValueChange = viewModel::onInputChange,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(if (isRtl) "پیام بعدی..." else "Next message...") },
                            enabled = uiState.hasApiKey && !uiState.isLoading,
                            maxLines = 5,
                            shape = RoundedCornerShape(22.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = { if (uiState.inputText.isNotBlank()) viewModel.sendMessage() }
                            )
                        )
                        FilledIconButton(
                            onClick = { viewModel.sendMessage() },
                            enabled = uiState.hasApiKey && uiState.inputText.isNotBlank() && !uiState.isLoading,
                            modifier = Modifier.size(48.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Filled.Send, contentDescription = "ارسال")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    isRtl: Boolean,
    onCopy: () -> Unit,
    onSpeak: () -> Unit
) {
    val isUser = message.role == ChatRole.USER
    val isError = message.isError

    val bubbleColor = when {
        isError -> MaterialTheme.colorScheme.errorContainer
        isUser -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        isError -> MaterialTheme.colorScheme.onErrorContainer
        isUser -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = when {
                isError -> "خطا"
                isUser -> "شما"
                else -> if (isRtl) "هوشکا" else "Hushka"
            },
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
        Spacer(Modifier.height(4.dp))

        val shape = RoundedCornerShape(
            topStart = 16.dp, topEnd = 16.dp,
            bottomStart = if (isUser) 16.dp else 4.dp,
            bottomEnd = if (isUser) 4.dp else 16.dp
        )
        Surface(
            shape = shape,
            color = bubbleColor,
            modifier = Modifier.widthIn(max = 320.dp).clip(shape)
        ) {
            SelectionContainer {
                Text(
                    text = message.content,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (!isUser && !isError) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = onSpeak,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, null, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isRtl) "پخش" else "Speak", fontSize = 10.sp)
                }
                TextButton(
                    onClick = onCopy,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isRtl) "کپی" else "Copy", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun TypingIndicator(isRtl: Boolean) {
    val transition = rememberInfiniteTransition(label = "typing")
    val dot1 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        ), label = "d1"
    )
    val dot2 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "d2"
    )
    val dot3 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "d3"
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRtl) "هوشکا می‌نویسد" else "Hushka is typing",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                listOf(dot1, dot2, dot3).forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

class ChatViewModelFactory(
    private val application: Application,
    private val initialPrompt: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(application, initialPrompt) as T
    }
}


@Composable
fun ModelSelectorChips(
    selectedProvider: String,
    onProviderSelected: (String) -> Unit,
    isRtl: Boolean,
    onLocalLanguageSelected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val providers = listOf(
        "gemini" to "Gemini 💎",
        "openai" to "OpenAI 🟢",
        "deepseek" to "DeepSeek 🟣",
        "groq" to "Groq ⚡",
        "anthropic" to "Anthropic 🟠",
        "mistral" to "Mistral 🌪",
        "xai" to "XAI 🚀",
        "local" to "بومی 🏠"
    )

    val localLanguages = listOf(
        "fa" to "فارسی 🇮🇷",
        "en" to "English 🇬🇧",
        "ar" to "العربية 🇸🇦",
        "tr" to "Türkçe 🇹🇷"
    )

    var showLocalLangMenu by remember { mutableStateOf(false) }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(providers) { (id, label) ->
            val isSelected = selectedProvider == id
            Box {
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (id == "local") {
                            showLocalLangMenu = true
                        }
                        val unsupported = listOf("anthropic", "mistral", "xai")
                        if (id in unsupported) {
                            Toast.makeText(context, "این مدل به‌زودی پیاده‌سازی می‌شود", Toast.LENGTH_SHORT).show()
                        } else {
                            onProviderSelected(id)
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    trailingIcon = if (id == "local") {
                        { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Language") }
                    } else if (isSelected) {
                        { Icon(Icons.Filled.Check, contentDescription = "Selected", modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                )
                
                if (id == "local") {
                    DropdownMenu(
                        expanded = showLocalLangMenu,
                        onDismissRequest = { showLocalLangMenu = false }
                    ) {
                        localLanguages.forEach { (langId, langName) ->
                            DropdownMenuItem(
                                text = { Text(langName) },
                                onClick = {
                                    onLocalLanguageSelected(langId)
                                    showLocalLangMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
