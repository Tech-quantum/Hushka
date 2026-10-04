package com.techquantum.promptvault

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LibraryStatusSection(
    promptCount: Int,
    isUpdating: Boolean,
    onUpdateLibrary: () -> Unit
) {
    val rotation by rememberInfiniteTransition(label = "libraryRefresh").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Restart
        ),
        label = "libraryRefreshRotation"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onUpdateLibrary,
            enabled = !isUpdating
        ) {
            Text(
                if (isUpdating) "↻" else "☁",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.graphicsLayer {
                    rotationZ = if (isUpdating) rotation else 0f
                }
            )
        }
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                " $promptCount ",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
    }
}

@Composable
fun LibraryUpdateStatus(
    messageKey: String,
    addedCount: Int,
    error: String
) {
    if (messageKey.isBlank()) return

    val message = when (messageKey) {
        "updating" -> stringResource(R.string.updating_library)
        "up_to_date" -> stringResource(R.string.library_up_to_date)
        "added" -> stringResource(R.string.library_added, addedCount)
        "error" -> stringResource(
            R.string.update_failed,
            error.ifBlank { stringResource(R.string.network_error) }
        )
        else -> return
    }

    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = if (messageKey == "error") {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}
