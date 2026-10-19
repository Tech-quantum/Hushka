package com.techquantum.hushka

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDialog(
    isRtl: Boolean,
    onDismiss: () -> Unit,
    onSavePrompt: (String, String, String, String, String, String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // --- Prompt Form State ---
    var promptTitle by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("عمومی") }
    var role by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var collection by remember { mutableStateOf("") }
    var showRolePicker by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    val categories = listOf("عمومی", "تصویر", "ویدئو", "متن", "کد")
    val currentRole = RoleTemplates.findById(role)

    // --- Image Gen State ---
    val context = LocalContext.current
    var imagePrompt by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Colors
    val textColor = MaterialTheme.colorScheme.onSurface
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val purplePrimary = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (selectedTab == 0) (if (isRtl) "افزودن پرامپت" else "Add Prompt") else (if (isRtl) "تولید تصویر" else "Generate Image"),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(if (isRtl) "پرامپت جدید" else "New Prompt", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(if (isRtl) "تولید تصویر 🎨" else "Image Gen 🎨", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
                Spacer(Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // ==================== TAB 1: PROMPT FORM ====================
                    Column(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = promptTitle, onValueChange = { promptTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.title), fontSize = 12.sp) },
                            placeholder = { Text(stringResource(R.string.title_placeholder), fontSize = 11.sp, color = textGrayColor) },
                            singleLine = true, shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = purplePrimary, unfocusedBorderColor = textGrayColor.copy(alpha = 0.4f))
                        )

                        Box {
                            OutlinedTextField(
                                value = category, onValueChange = { },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.category), fontSize = 12.sp) },
                                readOnly = true, singleLine = true, shape = RoundedCornerShape(12.dp),
                                trailingIcon = { TextButton(onClick = { categoryMenuExpanded = true }) { Text("▼", fontSize = 10.sp, color = purplePrimary) } },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = purplePrimary, unfocusedBorderColor = textGrayColor.copy(alpha = 0.4f))
                            )
                            DropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; categoryMenuExpanded = false })
                                }
                            }
                        }

                        Box {
                            OutlinedTextField(
                                value = currentRole?.let { "${it.emoji} ${it.displayName()}"} ?: "",
                                onValueChange = { }, modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.role_label), fontSize = 12.sp) },
                                readOnly = true, singleLine = true, shape = RoundedCornerShape(12.dp),
                                trailingIcon = { TextButton(onClick = { showRolePicker = true }) { Text("▼", fontSize = 10.sp, color = purplePrimary) } },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = purplePrimary, unfocusedBorderColor = textGrayColor.copy(alpha = 0.4f))
                            )
                        }

                        OutlinedTextField(
                            value = tags, onValueChange = { tags = it }, modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.tags_label), fontSize = 12.sp) },
                            placeholder = { Text(stringResource(R.string.tags_placeholder), fontSize = 11.sp, color = textGrayColor) },
                            singleLine = true, shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = purplePrimary, unfocusedBorderColor = textGrayColor.copy(alpha = 0.4f))
                        )

                        OutlinedTextField(
                            value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            label = { Text(stringResource(R.string.text_label), fontSize = 12.sp) },
                            placeholder = { Text(stringResource(R.string.text_placeholder), fontSize = 11.sp, color = textGrayColor) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = purplePrimary, unfocusedBorderColor = textGrayColor.copy(alpha = 0.4f))
                        )
                    }
                } else {
                    // ==================== TAB 2: IMAGE GEN ====================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = imagePrompt, onValueChange = { imagePrompt = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(if (isRtl) "توصیف تصویر (ترجیحاً انگلیسی)..." else "Image description (preferably English)...", fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                        imageUrl?.let { url ->
                            AsyncImage(
                                model = url, contentDescription = "Generated",
                                modifier = Modifier.fillMaxWidth().height(250.dp).padding(top = 8.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 0) {
                Button(
                    onClick = { if (text.isNotBlank()) onSavePrompt(promptTitle, category, tags, text, collection, role) },
                    shape = RoundedCornerShape(12.dp)
                ) { Text(stringResource(R.string.save), fontWeight = FontWeight.Bold) }
            } else {
                if (imageUrl == null) {
                    Button(
                        onClick = {
                            if (imagePrompt.isBlank()) { errorMsg = if (isRtl) "توضیح را وارد کنید" else "Enter description"; return@Button }
                            errorMsg = null
                            val encoded = java.net.URLEncoder.encode(imagePrompt, "UTF-8")
                            imageUrl = "https://image.pollinations.ai/prompt/$encoded?width=1024&height=1024&nologo=true"
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(if (isRtl) "تولید تصویر" else "Generate", fontWeight = FontWeight.Bold) }
                } else {
                    Button(
                        onClick = {
                            val request = DownloadManager.Request(Uri.parse(imageUrl))
                                .setTitle("Hushka AI Image").setDescription("Downloaded from Hushka")
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Hushka_${System.currentTimeMillis()}.png")
                            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            dm.enqueue(request)
                            Toast.makeText(context, if (isRtl) "دانلود شروع شد..." else "Downloading...", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(if (isRtl) "دانلود" else "Download", fontWeight = FontWeight.Bold) }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close), color = textGrayColor) }
        }
    )

    if (showRolePicker) {
        RolePickerDialog(onRoleSelected = { role = it }, onDismiss = { showRolePicker = false })
    }
}
