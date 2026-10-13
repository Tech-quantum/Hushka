package com.techquantum.hushka

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoryPickerDialog(
    prompts: List<PromptEntity>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val cardLightColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    // استخراج و گروه‌بندی دسته‌ها
    val allCategories = prompts
        .map { it.category }
        .filter { it.isNotBlank() }
        .distinct()

    val grouped = allCategories
        .groupBy { it.substringBefore('.') }
        .toSortedMap()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        titleContentColor = textColor,
        textContentColor = textColor,
        title = {
            Text(
                stringResource(R.string.select_category_title),
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // ===== همه دسته‌ها =====
                item(key = "all") {
                    val isSelected = selectedCategory == ALL_CATEGORY
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) purplePrimary else cardLightColor,
                        onClick = { onCategorySelected(ALL_CATEGORY) }
                    ) {
                        Text(
                            "🌐  ${stringResource(R.string.all_categories)}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            color = if (isSelected) Color.White else textColor,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // ===== گروه‌ها =====
                grouped.forEach { (domain, categories) ->
                    val hasSubcategories = categories.any { it.contains(".") }

                    if (!hasSubcategories) {
                        // فقط خود دسته (مثل image, web, github)
                        item(key = "single_$domain") {
                            val cat = categories.firstOrNull() ?: domain
                            val isSelected = selectedCategory == cat
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) purplePrimary else cardLightColor,
                                onClick = { onCategorySelected(cat) }
                            ) {
                                Text(
                                    getDomainName(domain),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    color = if (isSelected) Color.White else textColor,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    } else {
                        // ===== Header گروه =====
                        item(key = "header_$domain") {
                            val isSelected = selectedCategory == domain
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected)
                                    purplePrimary.copy(alpha = 0.85f)
                                else
                                    purplePrimary.copy(alpha = 0.15f),
                                onClick = { onCategorySelected(domain) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        getDomainName(domain),
                                        color = if (isSelected) Color.White else purplePrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isSelected) {
                                        Spacer(Modifier.weight(1f))
                                        Text(
                                            "✓",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // ===== زیردسته‌ها =====
                        items(categories.sorted(), key = { it }) { category ->
                            val isSelected = selectedCategory == category
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 2.dp, bottom = 2.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) purplePrimary else cardLightColor,
                                onClick = { onCategorySelected(category) }
                            ) {
                                Text(
                                    "•  ${getSubcategoryOnly(category)}",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    color = if (isSelected) Color.White else textGrayColor,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = purplePrimary)
            }
        }
    )
}

// ============================================================
// Helpers
// ============================================================

private fun getDomainEmoji(domain: String): String = when (domain.lowercase()) {
    "agent" -> "🤖"
    "analysis" -> "📊"
    "coding" -> "💻"
    "image" -> "🎨"
    "video" -> "🎬"
    "audio" -> "🎵"
    "text" -> "📝"
    "prompting" -> "🎯"
    "marketing" -> "📢"
    "other" -> "🔧"
    "ai" -> "✨"
    else -> "📁"
}

@Composable
private fun getDomainName(domain: String): String = when (domain.lowercase()) {
    "agent" -> stringResource(R.string.domain_agent)
    "analysis" -> stringResource(R.string.domain_analysis)
    "prompting" -> stringResource(R.string.domain_prompting)
    "text" -> stringResource(R.string.domain_text)
    "coding" -> stringResource(R.string.domain_coding)
    "image" -> stringResource(R.string.domain_image)
    "video" -> stringResource(R.string.domain_video)
    "marketing" -> "بازاریابی"
    "other" -> "سایر"
    "ai" -> "هوش مصنوعی"
    "web" -> stringResource(R.string.cat_web)
    "github" -> stringResource(R.string.cat_github)
    "audio" -> stringResource(R.string.cat_audio)
    else -> domain
}

@Composable
private fun getSubcategoryOnly(category: String): String {
    val parts = category.split(".", limit = 2)
    return if (parts.size < 2) {
        getLocalizedCategory(category)
    } else {
        // در حالت غیرفارسی از Title Case استفاده می‌کنیم
        val config = androidx.compose.ui.platform.LocalConfiguration.current
        val isPersian = config.locales[0].language == "fa"
        if (isPersian) {
            getLocalizedCategory(category).substringAfter(" • ", parts[1])
        } else {
            parts[1].split("-").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
        }
    }
}
