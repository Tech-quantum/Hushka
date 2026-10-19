package com.techquantum.hushka

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGeneratorDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var prompt by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "🎨 تولید تصویر با AI",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("توصیف تصویر (بهتر است انگلیسی باشد)...", fontSize = 12.sp) },
                    shape = RoundedCornerShape(12.dp)
                )

                errorMsg?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Generated Image",
                        modifier = Modifier.fillMaxWidth().height(300.dp).padding(top = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                    Button(
                        onClick = {
                            val request = DownloadManager.Request(Uri.parse(url))
                                .setTitle("Hushka AI Image")
                                .setDescription("Downloaded from Hushka")
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Hushka_${System.currentTimeMillis()}.png")
                            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            dm.enqueue(request)
                            Toast.makeText(context, "دانلود تصویر شروع شد...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("📥 دانلود تصویر", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            if (imageUrl == null) {
                Button(
                    onClick = {
                        if (prompt.isBlank()) {
                            errorMsg = "لطفاً یک توضیح وارد کنید."
                            return@Button
                        }
                        errorMsg = null
                        try {
                            // ساخت لینک تصویر با استفاده از Pollinations
                            val encodedPrompt = java.net.URLEncoder.encode(prompt, "UTF-8")
                            imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt?width=1024&height=1024&nologo=true"
                        } catch (e: Exception) {
                            errorMsg = e.message
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تولید تصویر (رایگان)", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    )
}
