package com.techquantum.hushka

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment

@Composable
fun WebPromptsDialog(
    prompts: List<PromptEntity>,
    isLoading: Boolean,
    loadingMessage: String = "در حال اتصال...",
    error: String?,
    isTranslating: Boolean = false,
    selectedSource: WebSource = WebSource.GITHUB,
    onSourceSelected: (WebSource) -> Unit = {},
    onTranslate: (List<PromptEntity>) -> Unit,
    onImport: (List<PromptEntity>) -> Unit,
    onRefresh: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    val selected = remember { mutableStateListOf<PromptEntity>() }
    var searchQuery by remember { mutableStateOf("") }
    var wasRefreshing by remember { mutableStateOf(false) }

    // 🔄 انیمیشن چرخش آیکون
    val infiniteTransition = rememberInfiniteTransition(label = "refresh")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    var showSuccessMessage by remember { mutableStateOf(false) }

    // ✅ بعد از اتمام بروزرسانی، پیام مرکزی نمایش بده
    LaunchedEffect(isLoading, wasRefreshing) {
        if (wasRefreshing && !isLoading) {
            wasRefreshing = false
            showSuccessMessage = true
            delay(1800)
            showSuccessMessage = false
        }
    }

    // 📢 پیام موفقیت در مرکز صفحه
    if (showSuccessMessage) {
        Dialog(
            onDismissRequest = { },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.web_refresh_success),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }


    // 🔍 فیلتر پرامپت‌ها بر اساس جستجو (case-insensitive، در title و text)
    val filteredPrompts = remember(prompts, searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) prompts
        else prompts.filter { p ->
            p.title.contains(q, ignoreCase = true) ||
            p.text.contains(q, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.web_title),
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if ((prompts.isNotEmpty() || wasRefreshing) && !isTranslating) {
                        OutlinedButton(
                            onClick = {
                                wasRefreshing = true
                                onRefresh()
                            },
                            enabled = !isLoading,
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(
                                horizontal = 10.dp,
                                vertical = 0.dp
                            ),
                            border = BorderStroke(1.dp, purplePrimary.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = purplePrimary,
                                disabledContentColor = purplePrimary.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(13.dp)
                                    .graphicsLayer {
                                        rotationZ = if (wasRefreshing && isLoading) rotationAngle else 0f
                                    }
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (wasRefreshing && isLoading) "در حال بروزرسانی..." else "به‌روزرسانی",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = sourceSubtitle(selectedSource),
                    color = textGrayColor,
                    fontSize = 11.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = purplePrimary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(24.dp))
                            Text(
                                text = getLoadingText(loadingMessage),
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = sourceSubtitle(selectedSource),
                                color = textGrayColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚠️", fontSize = 40.sp)
                                Spacer(Modifier.height(12.dp))
                                Text(stringResource(R.string.web_error_title), color = textColor, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(error, color = textGrayColor, fontSize = 11.sp)
                            }
                        }
                    }
                    prompts.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.web_none), color = textGrayColor)
                        }
                    }
                    else -> {
                        // ============ 🌐 Source Chips ============
                        Text(
                            stringResource(R.string.web_source_label),
                            color = textGrayColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(WebSource.values().toList()) { source ->
                                val isSelected = source == selectedSource
                                val accent = when (source) {
                                    WebSource.ALL -> Color(0xFF9C27B0)
                                    WebSource.GITHUB -> Color(0xFF10A37F)
                                    WebSource.HF_PROMPTS_CHAT -> Color(0xFFFFB74D)
                                    WebSource.HF_STABLE_DIFFUSION -> Color(0xFF7C7CFF)
                                    WebSource.HF_JAILBREAK -> Color(0xFFE57373)
                                    WebSource.HF_PERSIAN -> Color(0xFF4CAF50)
                                    WebSource.HF_DOLLY -> Color(0xFF00BCD4)
                                    WebSource.HF_WRITING -> Color(0xFFEC407A)
                                    WebSource.HF_CODE -> Color(0xFF3B82F6)
                                    WebSource.HF_MEDICAL -> Color(0xFF26C6DA)
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) accent else cardLightColor,
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) accent else Color.Transparent
                                    ),
                                    onClick = {
                                        if (!isLoading) onSourceSelected(source)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(source.emoji, fontSize = 11.sp)
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            stringResource(source.labelResId),
                                            color = if (isSelected) Color.White else textGrayColor,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // ============ 🔍 Search Field ============
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "جستجو در عنوان یا متن...",
                                    fontSize = 12.sp,
                                    color = textGrayColor
                                )
                            },
                            leadingIcon = {
                                Text("🔍", fontSize = 14.sp)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    TextButton(onClick = { searchQuery = "" }) {
                                        Text("✕", fontSize = 14.sp, color = textGrayColor)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = purplePrimary,
                                unfocusedBorderColor = textGrayColor.copy(alpha = 0.4f),
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                cursorColor = purplePrimary,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(Modifier.height(10.dp))

                        // ============ Counts Row ============
                        val allVisibleSelected = filteredPrompts.isNotEmpty() &&
                            filteredPrompts.all { p -> selected.any { it.title == p.title && it.text == p.text } }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val countText = if (searchQuery.isBlank()) {
                                stringResource(R.string.web_counts, prompts.size, selected.size)
                            } else {
                                "نمایش ${filteredPrompts.size} از ${prompts.size} • ${selected.size} انتخاب"
                            }
                            Text(
                                countText,
                                color = textGrayColor,
                                fontSize = 12.sp
                            )
                            TextButton(
                                onClick = {
                                    if (allVisibleSelected) {
                                        // حذف انتخاب فقط visible ها
                                        selected.removeAll { s ->
                                            filteredPrompts.any { it.title == s.title && it.text == s.text }
                                        }
                                    } else {
                                        // انتخاب همه visible ها
                                        filteredPrompts.forEach { p ->
                                            if (!selected.any { it.title == p.title && it.text == p.text }) {
                                                selected.add(p)
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    if (allVisibleSelected) stringResource(R.string.web_unselect_all) else stringResource(R.string.web_select_all),
                                    color = purplePrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        // ============ List or Empty State ============
                        if (filteredPrompts.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🔍", fontSize = 32.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "نتیجه‌ای یافت نشد",
                                        color = textColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "عبارت دیگری را امتحان کنید",
                                        color = textGrayColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(filteredPrompts) { prompt ->
                                    val isSelected = selected.any { it.title == prompt.title && it.text == prompt.text }
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) purplePrimary.copy(alpha = 0.25f) else cardLightColor,
                                        onClick = {
                                            if (isSelected) {
                                                selected.removeAll { it.title == prompt.title && it.text == prompt.text }
                                            } else {
                                                selected.add(prompt)
                                            }
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { checked ->
                                                    if (checked == true) {
                                                        if (!selected.any { it.title == prompt.title && it.text == prompt.text }) {
                                                            selected.add(prompt)
                                                        }
                                                    } else {
                                                        selected.removeAll { it.title == prompt.title && it.text == prompt.text }
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = purplePrimary)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    prompt.title,
                                                    color = textColor,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    prompt.text,
                                                    color = textGrayColor,
                                                    fontSize = 11.sp,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isLoading && prompts.isNotEmpty() && error == null && onTranslate != null) {
                    Button(
                        onClick = { onTranslate(selected.toList()) },
                        enabled = selected.isNotEmpty() && !isTranslating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isTranslating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onSecondary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("🌐 ترجمه", fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                }

                if (!isLoading && prompts.isNotEmpty() && error == null) {
                    Button(
                        onClick = { onImport(selected.toList()) },
                        enabled = selected.isNotEmpty() && !isTranslating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = purplePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("افزودن ${selected.size}", fontSize = 12.sp)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (isLoading) "لغو" else stringResource(R.string.close),
                    color = textGrayColor
                )
            }
        }
    )
}

@Composable
private fun sourceSubtitle(source: WebSource): String = when (source) {
    WebSource.ALL -> "🌐 " + stringResource(R.string.web_source_all)
    WebSource.GITHUB -> stringResource(R.string.web_subtitle_repo_prefix) + " awesome-chatgpt-prompts"
    WebSource.HF_PROMPTS_CHAT -> "🤗 Hugging Face: fka/prompts.chat"
    WebSource.HF_STABLE_DIFFUSION -> "🤗 Hugging Face: Gustavosta/Stable-Diffusion-Prompts"
    WebSource.HF_JAILBREAK -> "🤗 Hugging Face: rubend18/ChatGPT-Jailbreak-Prompts"
    WebSource.HF_PERSIAN -> "🤗 Hugging Face: kasranaqhdpur/persiandatasets2"
    WebSource.HF_DOLLY -> "🤗 Hugging Face: databricks/databricks-dolly-15k"
    WebSource.HF_WRITING -> "🤗 Hugging Face: euclaise/writingprompts"
    WebSource.HF_CODE -> "🤗 Hugging Face: sahil2801/CodeAlpaca-20k"
    WebSource.HF_MEDICAL -> "🤗 Hugging Face: medalpaca/medical_meadow_medical_flashcards"
}

@Composable
private fun getLoadingText(key: String): String = when (key) {
    "loading_connecting" -> stringResource(R.string.loading_connecting)
    "loading_downloading" -> stringResource(R.string.loading_downloading)
    "loading_almost_ready" -> stringResource(R.string.loading_almost_ready)
    "loading_slow_internet" -> stringResource(R.string.loading_slow_internet)
    else -> key
}
