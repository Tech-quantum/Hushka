package com.techquantum.hushka

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PurplePrimary = Color(0xFF7C7CFF)
private val PurpleLight = Color(0xFFB8B8FF)
private val DarkBg = Color(0xFF1A1A2E)
private val CardDark = Color(0xFF252538)
private val CardLight = Color(0xFF2E2E45)
private val TextLight = Color(0xFFEAEAEA)
private val TextGray = Color(0xFF9E9EAE)
private val BorderColor = Color(0xFF3A3A4E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit = {},
    onPromptClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf("همه ارائه‌دهندگان") }
    var selectedTab by remember { mutableStateOf("اخیر") }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Hushka",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "هوشکا بستری برای کاوش، خلق و مدیریت ایده‌های هوش مصنوعی",
                            fontSize = 11.sp,
                            color = TextGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextLight,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { },
                containerColor = PurplePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("پرامپت جدید", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("جستجو در عنوان، دسته‌بندی و متن پرامپت‌ها...", fontSize = 12.sp, color = TextGray)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
                },
                trailingIcon = {
                    Text("🎤", fontSize = 18.sp)
                },
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = PurplePrimary,
                    unfocusedContainerColor = CardDark,
                    focusedContainerColor = CardDark,
                    unfocusedTextColor = TextLight,
                    focusedTextColor = TextLight,
                    cursorColor = PurplePrimary
                )
            )

            Spacer(Modifier.height(16.dp))

            // Categories & Filter Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleLight.copy(alpha = 0.35f),
                        contentColor = TextLight
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("📁", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("دسته‌بندی‌ها", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleLight.copy(alpha = 0.35f),
                        contentColor = TextLight
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("⚙", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("فیلتر", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Provider Chips
            val providers = listOf("همه ارائه‌دهندگان", "anthropic", "deepseek", "gemini", "openai")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(providers) { provider ->
                    val isSelected = selectedProvider == provider
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) PurplePrimary.copy(alpha = 0.25f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (isSelected) PurplePrimary else BorderColor),
                        onClick = { selectedProvider = provider }
                    ) {
                        Text(
                            provider,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            color = if (isSelected) PurpleLight else TextGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Tab Chips
            val tabs = listOf("اخیر", "پین‌شده", "مجموعه‌ها", "علاقه‌مندی‌ها")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tabs) { tab ->
                    val isSelected = selectedTab == tab
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) PurplePrimary.copy(alpha = 0.25f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (isSelected) PurplePrimary else BorderColor),
                        onClick = { selectedTab = tab }
                    ) {
                        Text(
                            tab,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            color = if (isSelected) PurpleLight else TextGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Sort Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, BorderColor),
                    onClick = { }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مرتب‌سازی: جدیدترین", fontSize = 12.sp, color = TextGray)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "پرامپت‌های من",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "197 پرامپت ذخیره شده",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Prompt List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(8) { _ ->
                    PromptCard(
                        title = "Claude Fable 5.1 & Mythos Dual-Use Safety Protocol",
                        description = "You are Claude Fable 5.1 / Mythos 5.1, the newest model tier from Anthropic. Your operational domain includes...",
                        mainTag = "agent.agentic-workflow",
                        tags = listOf("safety#", "mythos#", "claude-5-1#", "refusal#", "anthropic#"),
                        footerTags = listOf("anthropic", "text-to-text"),
                        provider = "grok"
                    )
                }
            }
        }
    }
}

@Composable
fun PromptCard(
    title: String,
    description: String,
    mainTag: String,
    tags: List<String>,
    footerTags: List<String>,
    provider: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = TextLight
                )
                Spacer(Modifier.width(8.dp))
                Row {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CardLight,
                        modifier = Modifier.size(32.dp),
                        onClick = { }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👁", fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CardLight,
                        modifier = Modifier.size(32.dp),
                        onClick = { }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⭐", fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PurplePrimary.copy(alpha = 0.15f)
            ) {
                Text(
                    mainTag,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 11.sp,
                    color = PurpleLight
                )
            }

            Spacer(Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(tags) { tag ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF3A3A55)
                    ) {
                        Text(
                            tag,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            color = TextLight
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                description,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = TextGray
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    footerTags.forEach { ft ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CardLight
                        ) {
                            Text(
                                ft,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 10.sp,
                                color = TextGray
                            )
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PurplePrimary.copy(alpha = 0.3f),
                    onClick = { }
                ) {
                    Text(
                        provider,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
