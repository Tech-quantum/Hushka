package com.techquantum.hushka

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    prompts: List<PromptEntity>,
    onBack: () -> Unit
) {
    val total = prompts.size
    val favorites = prompts.count { it.favorite }
    val pinned = prompts.count { it.pinned }
    val categorized = prompts.count { it.category.isNotBlank() }
    val withRole = prompts.count { it.role.isNotBlank() }
    val withTags = prompts.count { it.tags.isNotBlank() }

    val categoryCounts = remember(prompts) {
        prompts.filter { it.category.isNotBlank() }
            .groupingBy { it.category }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(8)
    }
    val maxCategoryCount = categoryCounts.maxOfOrNull { it.second } ?: 1

    val topUsed = remember(prompts) {
        prompts.filter { it.lastUsedAt > 0 }
            .sortedByDescending { it.lastUsedAt }
            .take(5)
    }

    val purplePrimary = MaterialTheme.colorScheme.primary
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "📊 " + stringResource(R.string.statistics),
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ===== کارت‌های آمار کلی =====
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        emoji = "📚",
                        value = "$total",
                        label = stringResource(R.string.stat_total_prompts),
                        color = purplePrimary,
                        modifier = Modifier.weight(1f),
                        cardColor = cardColor,
                        textColor = textColor,
                        textGray = textGray
                    )
                    StatCard(
                        emoji = "⭐",
                        value = "$favorites",
                        label = stringResource(R.string.stat_favorites),
                        color = Color(0xFFFFB74D),
                        modifier = Modifier.weight(1f),
                        cardColor = cardColor,
                        textColor = textColor,
                        textGray = textGray
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        emoji = "📌",
                        value = "$pinned",
                        label = stringResource(R.string.stat_pinned),
                        color = Color(0xFFE57373),
                        modifier = Modifier.weight(1f),
                        cardColor = cardColor,
                        textColor = textColor,
                        textGray = textGray
                    )
                    StatCard(
                        emoji = "📁",
                        value = "$categorized",
                        label = stringResource(R.string.stat_categorized),
                        color = Color(0xFF64B5F6),
                        modifier = Modifier.weight(1f),
                        cardColor = cardColor,
                        textColor = textColor,
                        textGray = textGray
                    )
                }
            }

            // ===== کیفیت داده =====
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "✨ " + stringResource(R.string.stat_quality),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(Modifier.height(12.dp))

                        QualityBar(
                            label = stringResource(R.string.stat_with_role),
                            count = withRole,
                            total = total,
                            color = Color(0xFFFFB74D),
                            textColor = textColor,
                            textGray = textGray
                        )
                        Spacer(Modifier.height(8.dp))
                        QualityBar(
                            label = stringResource(R.string.stat_with_tags),
                            count = withTags,
                            total = total,
                            color = Color(0xFF4CAF50),
                            textColor = textColor,
                            textGray = textGray
                        )
                        Spacer(Modifier.height(8.dp))
                        QualityBar(
                            label = stringResource(R.string.stat_categorized),
                            count = categorized,
                            total = total,
                            color = purplePrimary,
                            textColor = textColor,
                            textGray = textGray
                        )
                    }
                }
            }

            // ===== پرامپت به تفکیک دسته =====
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "📁 " + stringResource(R.string.stat_by_category),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(Modifier.height(12.dp))

                        if (categoryCounts.isEmpty()) {
                            Text(
                                stringResource(R.string.stat_no_data),
                                fontSize = 12.sp,
                                color = textGray
                            )
                        } else {
                            categoryCounts.forEach { (category, count) ->
                                CategoryBar(
                                    category = category,
                                    count = count,
                                    maxCount = maxCategoryCount,
                                    color = purplePrimary,
                                    textColor = textColor,
                                    textGray = textGray
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // ===== پرکاربردترین‌ها =====
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "🔥 " + stringResource(R.string.stat_top_used),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(Modifier.height(12.dp))

                        if (topUsed.isEmpty()) {
                            Text(
                                stringResource(R.string.stat_no_usage),
                                fontSize = 12.sp,
                                color = textGray
                            )
                        } else {
                            topUsed.forEachIndexed { index, prompt ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = purplePrimary.copy(alpha = 0.15f),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                "${index + 1}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = purplePrimary
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        prompt.title.ifBlank { "بدون عنوان" },
                                        fontSize = 13.sp,
                                        color = textColor,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        stringResource(R.string.stat_used_times, prompt.lastUsedAt.toInt().coerceAtLeast(1)),
                                        fontSize = 11.sp,
                                        color = textGray
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

@Composable
private fun StatCard(
    emoji: String,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    cardColor: Color,
    textColor: Color,
    textGray: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                fontSize = 11.sp,
                color = textGray,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QualityBar(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    textColor: Color,
    textGray: Color
) {
    val percent = if (total > 0) (count * 100 / total) else 0
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = textColor)
            Text("$count ($percent%)", fontSize = 11.sp, color = textGray)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun CategoryBar(
    category: String,
    count: Int,
    maxCount: Int,
    color: Color,
    textColor: Color,
    textGray: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                getLocalizedCategory(category),
                fontSize = 12.sp,
                color = textColor,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text("$count", fontSize = 11.sp, color = textGray)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { if (maxCount > 0) count.toFloat() / maxCount else 0f },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}
