package com.techquantum.hushka

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeBottomBar(
    categoriesActive: Boolean,
    favoritesActive: Boolean,
    isOnline: Boolean,
    isUpdating: Boolean,
    onCategories: () -> Unit,
    onFavorites: () -> Unit,
    onAdd: () -> Unit,
    onUpdate: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit
) {
    val active = MaterialTheme.colorScheme.primary
    val idle = MaterialTheme.colorScheme.onSurfaceVariant
    val transition = rememberInfiniteTransition(label = "sync")
    val rotation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "rot"
    )

    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
        Column(Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BarItem(Modifier.weight(1f), Icons.Default.Category,
                    stringResource(R.string.categories_btn),
                    if (categoriesActive) active else idle, onCategories)
                BarItem(Modifier.weight(1f), Icons.Default.Star,
                    stringResource(R.string.favorites_btn),
                    if (favoritesActive) active else idle, onFavorites)

                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onAdd).padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(active),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.new_prompt_btn),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onUpdate).padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isUpdating) {
                            Icon(
                                Icons.Default.Sync, null, tint = active,
                                modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = rotation.value)
                            )
                        } else {
                            Icon(
                                Icons.Default.Cloud, null,
                                tint = if (isOnline) idle else idle.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                            Box(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .offset(x = 3.dp, y = (-1).dp).size(9.dp).clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF4CAF50) else Color(0xFFE57373))
                            )
                        }
                    }
                    BarLabel(stringResource(R.string.nav_update), idle)
                }

                BarItem(Modifier.weight(1f), Icons.Default.BarChart,
                    stringResource(R.string.nav_stats), idle, onStats)
                BarItem(Modifier.weight(1f), Icons.Default.Settings,
                    stringResource(R.string.settings), idle, onSettings)
            }
        }
    }
}

@Composable
private fun BarItem(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        BarLabel(label, tint)
    }
}

@Composable
private fun BarLabel(text: String, tint: Color) {
    Text(
        text = text, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Medium,
        maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
    )
}
