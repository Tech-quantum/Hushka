package com.techquantum.hushka

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StatisticsScreen(
    prompts: List<PromptEntity>,
    onBack: () -> Unit
) {
    val total = prompts.size
    val favorites = prompts.count { it.favorite }
    val pinned = prompts.count { it.pinned }
    
    // داده‌های دسته‌بندی برای نمودار دایره‌ای
    val categoryCounts = remember(prompts) {
        prompts.filter { it.category.isNotBlank() }
            .groupingBy { it.category }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(6)
    }
    
    // داده‌های سرویس‌دهنده برای نمودار ستونی
    val providerCounts = remember(prompts) {
        prompts.filter { it.provider.isNotBlank() }
            .groupingBy { it.provider }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(5)
    }

    val topUsed = remember(prompts) {
        prompts.filter { it.lastUsedAt > 0 }
            .sortedByDescending { it.lastUsedAt }
            .take(5)
    }

    val purplePrimary = MaterialTheme.colorScheme.primary
    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant

    // پالت رنگی برای نمودارها
    val chartColors = listOf(
        Color(0xFF8B5CF6), Color(0xFF10B981), Color(0xFFF59E0B),
        Color(0xFF3B82F6), Color(0xFFEF4444), Color(0xFF6366F1)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("📊 " + stringResource(R.string.statistics), color = textColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ===== کارت‌های آمار کلی =====
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("📚", "$total", stringResource(R.string.stat_total_prompts), purplePrimary, Modifier.weight(1f), cardColor, textColor, textGray)
                    StatCard("⭐", "$favorites", stringResource(R.string.stat_favorites), Color(0xFFFFB74D), Modifier.weight(1f), cardColor, textColor, textGray)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("📌", "$pinned", stringResource(R.string.stat_pinned), Color(0xFFE57373), Modifier.weight(1f), cardColor, textColor, textGray)
                    StatCard("🔖", "${prompts.count { it.tags.isNotBlank() }}", "دارای تگ", Color(0xFF64B5F6), Modifier.weight(1f), cardColor, textColor, textGray)
                }
            }

            // ===== نمودار دایره‌ای (دسته‌بندی‌ها) =====
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📁 توزیع دسته‌بندی‌ها", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(16.dp))
                        
                        if (categoryCounts.isEmpty()) {
                            Text("داده‌ای برای نمایش وجود ندارد", color = textGray, fontSize = 12.sp)
                        } else {
                            DonutChart(
                                data = categoryCounts,
                                colors = chartColors,
                                modifier = Modifier.size(180.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            // راهنمای رنگ‌ها
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categoryCounts.forEachIndexed { index, (cat, count) ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(10.dp).background(chartColors[index % chartColors.size], CircleShape))
                                        Spacer(Modifier.width(6.dp))
                                        Text(getLocalizedCategory(cat), fontSize = 11.sp, color = textGray)
                                        Text(" ($count)", fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ===== نمودار ستونی (سرویس‌دهنده‌ها) =====
            if (providerCounts.isNotEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🤖 پرامپت‌ها بر اساس سرویس‌دهنده", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Spacer(Modifier.height(16.dp))
                            BarChart(
                                data = providerCounts,
                                color = purplePrimary,
                                textColor = textColor,
                                textGray = textGray,
                                modifier = Modifier.fillMaxWidth().height(120.dp)
                            )
                        }
                    }
                }
            }

            // ===== پرکاربردترین‌ها =====
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🔥 " + stringResource(R.string.stat_top_used), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Spacer(Modifier.height(12.dp))
                        if (topUsed.isEmpty()) {
                            Text(stringResource(R.string.stat_no_usage), fontSize = 12.sp, color = textGray)
                        } else {
                            topUsed.forEachIndexed { index, prompt ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = CircleShape, color = purplePrimary.copy(alpha = 0.15f), modifier = Modifier.size(24.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = purplePrimary)
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(prompt.title.ifBlank { stringResource(R.string.untitled) }, fontSize = 13.sp, color = textColor, modifier = Modifier.weight(1f), maxLines = 1)
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
fun DonutChart(data: List<Pair<String, Int>>, colors: List<Color>, modifier: Modifier = Modifier) {
    val totalSum = data.sumOf { it.second }.toFloat()
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(data) {
        animatedProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            val strokeWidth = size.minDimension * 0.2f
            data.forEachIndexed { index, (_, count) ->
                val sweepAngle = (count / totalSum) * 360f * animatedProgress.value
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }
        Text(
            text = "${totalSum.toInt()}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BarChart(
    data: List<Pair<String, Int>>,
    color: Color,
    textColor: Color,
    textGray: Color,
    modifier: Modifier = Modifier
) {
    val maxVal = data.maxOfOrNull { it.second }?.toFloat() ?: 1f
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (name, count) ->
            val barHeightFraction = (count / maxVal) * animatedProgress.value
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Text("$count", fontSize = 10.sp, color = textColor, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(barHeightFraction)
                        .background(color.copy(alpha = 0.8f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = name,
                    fontSize = 9.sp,
                    color = textGray,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StatCard(emoji: String, value: String, label: String, color: Color, modifier: Modifier = Modifier, cardColor: Color, textColor: Color, textGray: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = textGray, maxLines = 1)
        }
    }
}
