package com.techquantum.promptvault

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    promptCount: Int,
    isUpdating: Boolean,
    onOpenSettings: () -> Unit,
    onUpdateLibrary: () -> Unit
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = onOpenSettings) {
                Text("⚙", style = MaterialTheme.typography.titleLarge)
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.app_name),
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    stringResource(R.string.app_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            LibraryStatusSection(
                promptCount = promptCount,
                isUpdating = isUpdating,
                onUpdateLibrary = onUpdateLibrary
            )
        }
    )
}
