package com.techquantum.promptvault

import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource

@Composable
fun HomeFloatingActionButton(
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            Text(
                "+",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                stringResource(R.string.new_prompt),
                fontWeight = FontWeight.Bold
            )
        }
    )
}
