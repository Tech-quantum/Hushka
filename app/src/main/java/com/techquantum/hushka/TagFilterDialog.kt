package com.techquantum.hushka

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagFilterDialog(
    allTags: List<String>,
    initialSelectedTags: Set<String>,
    initialMode: String,
    onApply: (Set<String>, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTags by remember { mutableStateOf(initialSelectedTags) }
    var filterMode by remember { mutableStateOf(initialMode) } // "AND" یا "OR"
    var searchQuery by remember { mutableStateOf("") }

    val filteredTags = remember(searchQuery, allTags) {
        if (searchQuery.isBlank()) allTags
        else allTags.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("فیلتر پیشرفته تگ‌ها", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                // حالت فیلتر (AND / OR)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = filterMode == "OR",
                        onClick = { filterMode = "OR" },
                        label = { Text("حداقل یکی (OR)") }
                    )
                    FilterChip(
                        selected = filterMode == "AND",
                        onClick = { filterMode = "AND" },
                        label = { Text("همه (AND)") }
                    )
                }

                Spacer(Modifier.height(12.dp))

                // جستجوی تگ
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("جستجوی تگ...", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(12.dp))

                if (filteredTags.isEmpty()) {
                    Text("تگی یافت نشد.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    // لیست تگ‌ها
                    Column(
                        modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredTags.forEach { tag ->
                                val isSelected = selectedTags.contains(tag)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedTags = if (isSelected) selectedTags - tag else selectedTags + tag
                                    },
                                    label = { Text(tag, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onApply(selectedTags, filterMode)
            }) {
                Text("اعمال فیلتر", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    selectedTags = emptySet()
                    onApply(emptySet(), filterMode)
                }) {
                    Text("پاک کردن", color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("بستن")
                }
            }
        }
    )
}
