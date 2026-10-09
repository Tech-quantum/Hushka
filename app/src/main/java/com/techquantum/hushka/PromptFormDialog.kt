package com.techquantum.hushka

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptFormDialog(
    title: String,
    initialPrompt: PromptEntity?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var promptTitle by rememberSaveable(initialPrompt?.id) { mutableStateOf(initialPrompt?.title.orEmpty()) }
    var category by rememberSaveable(initialPrompt?.id) { mutableStateOf(initialPrompt?.category.orEmpty()) }
    var role by rememberSaveable(initialPrompt?.id) { mutableStateOf(initialPrompt?.role.orEmpty()) }
    var tags by rememberSaveable(initialPrompt?.id) { mutableStateOf(initialPrompt?.tags.orEmpty()) }
    var text by rememberSaveable(initialPrompt?.id) { mutableStateOf(initialPrompt?.text.orEmpty()) }
    var collection by rememberSaveable(initialPrompt?.id) { mutableStateOf(initialPrompt?.collection.orEmpty()) }

    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var roleMenuExpanded by remember { mutableStateOf(false) }

    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    // پیشنهاد نقش بر اساس دسته‌بندی
    LaunchedEffect(category) {
        if (role.isBlank() && category.isNotBlank()) {
            val suggestedId = RoleTemplates.suggestForCategory(category)
            if (suggestedId != "custom") {
                role = suggestedId
            }
        }
    }

    val currentRole = RoleTemplates.findById(role)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        titleContentColor = textColor,
        title = { Text(title, color = textColor, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ===== عنوان =====
                OutlinedTextField(
                    value = promptTitle,
                    onValueChange = { promptTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.title), fontSize = 12.sp) },
                    placeholder = { Text(stringResource(R.string.title_placeholder), fontSize = 11.sp, color = textGrayColor) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = purplePrimary,
                        unfocusedBorderColor = textGrayColor.copy(alpha = 0.4f),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = purplePrimary,
                        unfocusedLabelColor = textGrayColor
                    )
                )

                // ===== دسته‌بندی =====
                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.category), fontSize = 12.sp) },
                        placeholder = { Text(stringResource(R.string.category_placeholder), fontSize = 11.sp, color = textGrayColor) },
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            TextButton(onClick = { categoryMenuExpanded = true }) {
                                Text("▼", fontSize = 10.sp, color = purplePrimary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = purplePrimary,
                            unfocusedBorderColor = textGrayColor.copy(alpha = 0.4f),
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = purplePrimary,
                            unfocusedLabelColor = textGrayColor
                        )
                    )
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // ===== انتخاب شخصیت/نقش =====
                Box {
                    OutlinedTextField(
                        value = currentRole?.let { "${it.emoji} ${it.nameFa}" } ?: "",
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("شخصیت / نقش", fontSize = 12.sp) },
                        placeholder = { Text("یک نقش انتخاب کن", fontSize = 11.sp, color = textGrayColor) },
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            TextButton(onClick = { roleMenuExpanded = true }) {
                                Text("▼", fontSize = 10.sp, color = purplePrimary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = purplePrimary,
                            unfocusedBorderColor = textGrayColor.copy(alpha = 0.4f),
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = purplePrimary,
                            unfocusedLabelColor = textGrayColor
                        )
                    )
                    DropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false },
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        RoleTemplates.all.forEach { template ->
                            DropdownMenuItem(
                                text = {
                                    Text("${template.emoji}  ${template.nameFa}", fontSize = 13.sp)
                                },
                                onClick = {
                                    role = template.id
                                    roleMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // پیش‌نمایش متن Role
                if (currentRole != null && currentRole.promptPrefix.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = purplePrimary.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            currentRole.promptPrefix,
                            modifier = Modifier.padding(10.dp),
                            fontSize = 10.sp,
                            color = purplePrimary,
                            lineHeight = 14.sp
                        )
                    }
                }

                // ===== برچسب‌ها =====
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.tags), fontSize = 12.sp) },
                    placeholder = { Text(stringResource(R.string.tags_placeholder), fontSize = 11.sp, color = textGrayColor) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = purplePrimary,
                        unfocusedBorderColor = textGrayColor.copy(alpha = 0.4f),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = purplePrimary,
                        unfocusedLabelColor = textGrayColor
                    )
                )

                // ===== مجموعه =====
                OutlinedTextField(
                    value = collection,
                    onValueChange = { collection = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.collection), fontSize = 12.sp) },
                    placeholder = { Text(stringResource(R.string.collection_placeholder), fontSize = 11.sp, color = textGrayColor) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = purplePrimary,
                        unfocusedBorderColor = textGrayColor.copy(alpha = 0.4f),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = purplePrimary,
                        unfocusedLabelColor = textGrayColor
                    )
                )

                // ===== متن پرامپت =====
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                    label = { Text(stringResource(R.string.prompt_text), fontSize = 12.sp) },
                    placeholder = { Text(stringResource(R.string.prompt_text_placeholder), fontSize = 11.sp, color = textGrayColor) },
                    minLines = 8,
                    maxLines = 20,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = purplePrimary,
                        unfocusedBorderColor = textGrayColor.copy(alpha = 0.4f),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = purplePrimary,
                        unfocusedLabelColor = textGrayColor
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        promptTitle.trim(),
                        category.trim(),
                        tags.trim(),
                        text.trim(),
                        collection.trim()
                    )
                },
                enabled = promptTitle.isNotBlank() && text.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = purplePrimary,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Text(stringResource(R.string.save), fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = textGrayColor, fontSize = 13.sp)
            }
        }
    )
}
