package com.techquantum.hushka
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.HorizontalPager
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.CircularProgressIndicator
import android.media.MediaPlayer
import android.widget.VideoView
import androidx.compose.ui.viewinterop.AndroidView

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
    var videoPrompt by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf<String?>(null) }
    var videoErrorMsg by remember { mutableStateOf<String?>(null) }
    var musicPrompt by remember { mutableStateOf("") }
    var musicUrl by remember { mutableStateOf<String?>(null) }
    var musicFileExtension by remember { mutableStateOf("wav") }
    var isMusicLoading by remember { mutableStateOf(false) }
    var musicErrorMsg by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // --- Presentation Gen State ---
    var presentationTopic by remember { mutableStateOf("") }
    var presentationSlideCount by remember { mutableStateOf(5) }
    var presentationSlides by remember { mutableStateOf<List<PresentationSlide>>(emptyList()) }
    var isGeneratingPresentation by remember { mutableStateOf(false) }
    var presentationErrorMsg by remember { mutableStateOf<String?>(null) }

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
                text = when (selectedTab) { 0 -> if (isRtl) "افزودن پرامپت" else "Add Prompt"; 1 -> if (isRtl) "تولید تصویر" else "Generate Image"; 2 -> if (isRtl) "تولید ویدیو" else "Generate Video"; else -> if (isRtl) "تولید موسیقی" else "Generate Music" },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                var dropdownExpanded by remember { mutableStateOf(false) }
                val tabOptions = listOf(
                    Triple(0, "پرامپت جدید", "New Prompt"),
                    Triple(1, "تولید تصویر 🎨", "Image Gen 🎨"),
                    Triple(2, "تولید ویدیو 🎬", "Video Gen 🎬"),
                    Triple(3, "تولید موسیقی 🎵", "Music Gen 🎵"),
                    Triple(4, "تولید ارائه 📊", "Slides Gen 📊")
                )
                val currentOption = tabOptions[selectedTab]

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isRtl) currentOption.second else currentOption.third,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Select tab",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.widthIn(min = 180.dp, max = 300.dp)
                    ) {
                        tabOptions.forEach { (index, fa, en) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (isRtl) fa else en,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedTab = index
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
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
                } else if (selectedTab == 1) {
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
                } else if (selectedTab == 2) {
                    // ==================== TAB 3: VIDEO GEN ====================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = videoPrompt, onValueChange = { videoPrompt = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(if (isRtl) "توصیف ویدیو (ترجیحاً انگلیسی)..." else "Video description (preferably English)...", fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        videoErrorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                        videoUrl?.let { url ->
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        setVideoURI(Uri.parse(url))
                                        setOnPreparedListener { it.isLooping = true; start() }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(250.dp).padding(top = 8.dp)
                            )
                        }
                    }
                } else if (selectedTab == 3) {
                    // ==================== TAB 4: MUSIC GEN ====================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = musicPrompt, onValueChange = { musicPrompt = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(if (isRtl) "توصیف موسیقی (ترجیحاً انگلیسی)..." else "Music description (preferably English)...", fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        musicErrorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                        
                        if (isMusicLoading) {
                            CircularProgressIndicator(color = purplePrimary)
                        }
                        
                        musicUrl?.let { url ->
                            Button(
                                onClick = {
                                    if (mediaPlayer?.isPlaying == true) {
                                        mediaPlayer?.pause()
                                    } else {
                                        mediaPlayer?.start()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("پخش / توقف", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
                else {
                    // ==================== TAB 5: PRESENTATION GEN ====================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = presentationTopic, onValueChange = { presentationTopic = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(if (isRtl) "موضوع ارائه..." else "Topic...", fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(3, 5, 7).forEach { cnt ->
                                FilterChip(
                                    selected = presentationSlideCount == cnt,
                                    onClick = { presentationSlideCount = cnt },
                                    label = { Text("$cnt اسلاید", fontSize = 11.sp) }
                                )
                            }
                        }
                        presentationErrorMsg?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        }
                        if (isGeneratingPresentation) {
                            CircularProgressIndicator(color = purplePrimary)
                            Text(if (isRtl) "در حال تولید..." else "Generating...", fontSize = 11.sp, color = textGrayColor)
                        }
                        if (presentationSlides.isNotEmpty()) {
                            val pagerState = rememberPagerState(pageCount = { presentationSlides.size })
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth().height(280.dp),
                                pageSpacing = 8.dp
                            ) { page ->
                                val slide = presentationSlides[page]
                                Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))) {
                                    AsyncImage(
                                        model = slide.imageUrl,
                                        contentDescription = slide.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(
                                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                                listOf(
                                                    androidx.compose.ui.graphics.Color.Transparent,
                                                    androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.75f)
                                                )
                                            )
                                        )
                                    )
                                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                                        Text(slide.title, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(slide.content, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f), fontSize = 11.sp, maxLines = 4)
                                    }
                                    Text("${page + 1} / ${presentationSlides.size}",
                                        color = androidx.compose.ui.graphics.Color.White, fontSize = 10.sp,
                                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
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
            } else if (selectedTab == 1) {
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
            } else if (selectedTab == 2) {
                if (videoUrl == null) {
                    Button(
                        onClick = {
                            if (videoPrompt.isBlank()) { videoErrorMsg = if (isRtl) "توضیح را وارد کنید" else "Enter description"; return@Button }
                            videoErrorMsg = null
                            val encoded = java.net.URLEncoder.encode(videoPrompt, "UTF-8")
                            videoUrl = "https://gen.pollinations.ai/video/$encoded"
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(if (isRtl) "تولید ویدیو" else "Generate Video", fontWeight = FontWeight.Bold) }
                } else {
                    Button(
                        onClick = {
                            val request = DownloadManager.Request(Uri.parse(videoUrl))
                                .setTitle("Hushka AI Video").setDescription("Downloaded from Hushka")
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Hushka_${System.currentTimeMillis()}.mp4")
                            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            dm.enqueue(request)
                            Toast.makeText(context, if (isRtl) "دانلود شروع شد..." else "Downloading...", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(if (isRtl) "دانلود" else "Download", fontWeight = FontWeight.Bold) }
                }
            } else if (selectedTab == 3) {
                // ==================== MUSIC CONFIRM ====================
                if (isMusicLoading) {
                    CircularProgressIndicator(color = purplePrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else if (musicUrl == null) {
                    Button(
                        onClick = {
                            if (musicPrompt.isBlank()) {
                                musicErrorMsg = if (isRtl) "توضیح را وارد کنید" else "Enter description"
                                return@Button
                            }
                            isMusicLoading = true
                            musicErrorMsg = null
                            coroutineScope.launch {
                                val result = MusicGenService.generateMusic(musicPrompt)
                                val musicBytes = result.audioBytes
                                if (musicBytes != null) {
                                    musicFileExtension = result.fileExtension
                                    val file = java.io.File(
                                        context.cacheDir,
                                        "music_${System.currentTimeMillis()}.$musicFileExtension"
                                    )
                                    file.writeBytes(musicBytes)
                                    musicUrl = file.toURI().toString()
                                    mediaPlayer = android.media.MediaPlayer().apply {
                                        setDataSource(file.absolutePath)
                                        prepare()
                                    }
                                } else {
                                    val details = buildString {
                                        result.httpCode?.let { append("HTTP $it: ") }
                                        append(result.error ?: "Unknown error")
                                    }
                                    musicErrorMsg = if (isRtl) "خطا در تولید: $details" else "Generation failed: $details"
                                }
                                isMusicLoading = false
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(if (isRtl) "تولید موسیقی" else "Generate Music", fontWeight = FontWeight.Bold) }
                } else {
                    Button(
                        onClick = {
                            val request = DownloadManager.Request(Uri.parse(musicUrl))
                                .setTitle("Hushka AI Music").setDescription("Downloaded from Hushka")
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Hushka_${System.currentTimeMillis()}.$musicFileExtension")
                            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            dm.enqueue(request)
                            Toast.makeText(context, if (isRtl) "دانلود شروع شد..." else "Downloading...", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(if (isRtl) "دانلود" else "Download", fontWeight = FontWeight.Bold) }
                }
            } else {
                // ==================== PRESENTATION CONFIRM ====================
                if (isGeneratingPresentation) {
                    CircularProgressIndicator(color = purplePrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else if (presentationSlides.isEmpty()) {
                    Button(
                        onClick = {
                            if (presentationTopic.isBlank()) {
                                presentationErrorMsg = if (isRtl) "موضوع را وارد کنید" else "Enter topic"
                                return@Button
                            }
                            if (AppSettings.getAiApiKey(context).isBlank()) {
                                presentationErrorMsg = if (isRtl) "کلید API وارد نشده" else "API key not set"
                                return@Button
                            }
                            isGeneratingPresentation = true
                            presentationErrorMsg = null
                            coroutineScope.launch {
                                val result = PresentationGenerator.generateSlides(
                                    context = context, topic = presentationTopic, slideCount = presentationSlideCount
                                )
                                result.fold(
                                    onSuccess = { presentationSlides = it; isGeneratingPresentation = false },
                                    onFailure = { presentationErrorMsg = it.message; isGeneratingPresentation = false }
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(if (isRtl) "تولید ارائه" else "Generate Slides", fontWeight = FontWeight.Bold) }
                } else {
                    Button(
                        onClick = { presentationSlides = emptyList(); presentationErrorMsg = null },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(if (isRtl) "تولید مجدد" else "Regenerate", fontWeight = FontWeight.Bold) }
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
