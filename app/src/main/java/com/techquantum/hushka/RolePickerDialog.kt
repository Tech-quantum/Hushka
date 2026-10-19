package com.techquantum.hushka

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolePickerDialog(
    onRoleSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = remember { RoleTemplates.all.map { it.category }.distinct() }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "عمومی") }

    val filteredRoles = remember(selectedCategory) {
        RoleTemplates.all.filter { it.category == selectedCategory }
    }
    val groupedRoles = remember(filteredRoles) {
        filteredRoles.groupBy { it.subCategory }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "انتخاب نقش (Role)",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                // چیپ‌های دسته‌بندی بالای دیالوگ
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(Modifier.height(12.dp))

                // لیست نقش‌ها گروه‌بندی شده بر اساس زیردسته
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    groupedRoles.forEach { (subCat, roles) ->
                        if (subCat.isNotBlank()) {
                            item {
                                Text(
                                    text = "🔹 $subCat",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                        }
                        items(roles) { role ->
                            RoleItem(
                                role = role,
                                onClick = {
                                    onRoleSelected(role.id)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    )
}

@Composable
private fun RoleItem(role: RoleTemplate, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(role.emoji, fontSize = 20.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = role.displayName(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (role.promptPrefix.isNotBlank()) {
                    Text(
                        text = role.promptPrefix.take(60) + "...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
