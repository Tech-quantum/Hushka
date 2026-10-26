package com.techquantum.hushka

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * صفحه‌ی قفل؛ تا تأیید بیومتریک، هیچ‌کدام از محتوای اپ ساخته نمی‌شود.
 *
 * `showError` بعد از انصراف یا شکست تأیید روشن می‌شود تا کاربر بداند
 * دیالوگ سیستم بسته شده و می‌تواند دوباره تلاش کند.
 */
@Composable
fun BiometricLockScreen(
    showError: Boolean,
    onUnlock: () -> Unit,
) {
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔒", fontSize = 48.sp)
            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(R.string.lock_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.lock_subtitle),
                fontSize = 12.sp,
                color = textGrayColor,
                textAlign = TextAlign.Center
            )
            if (showError) {
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.lock_error),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = onUnlock,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primary)
            ) {
                Text(stringResource(R.string.lock_button), fontSize = 13.sp)
            }
        }
    }
}
