package com.techquantum.hushka

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HelpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "راهنمای کاربری هوشکا 📚",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HelpItem(
                    emoji = "🤖",
                    title = "چت با هوش مصنوعی",
                    desc = "ابتدا در تنظیمات (تب AI) سرویس‌دهنده را انتخاب کرده و کلید API خود را وارد کنید. سپس روی هر پرامپت دکمه «اشتراک‌گذاری» را بزنید تا صفحه چت باز شده و پرامپت به صورت خودکار برای هوش مصنوعی ارسال شود."
                )
                HelpItem(
                    emoji = "💾",
                    title = "کش هوشمند (Cache)",
                    desc = "نتایج واکشی از وب برای بارگذاری سریع‌تر در حافظه ذخیره می‌شوند. اگر می‌خواهید داده‌های جدیدی از اینترنت دریافت کنید، در دیالوگ واکشی دکمه «به‌روزرسانی» را بزنید. برای آزادسازی حافظه، از تنظیمات (تب داده‌ها) کش را پاک کنید."
                )
                HelpItem(
                    emoji = "🏷️",
                    title = "فیلتر پیشرفته تگ‌ها",
                    desc = "با کلیک روی چیپ «تگ‌ها» در صفحه اصلی، می‌توانید چند تگ را همزمان انتخاب کنید. با انتخاب حالت AND فقط پرامپت‌هایی نمایش داده می‌شوند که همه تگ‌های انتخاب‌شده را با هم دارند."
                )
                HelpItem(
                    emoji = "🗑",
                    title = "حذف سریع (Swipe)",
                    desc = "برای حذف سریع، کارت پرامپت را با انگشت به سمت چپ بکشید (Swipe-to-Delete) و سپس حذف را تایید کنید."
                )
                HelpItem(
                    emoji = "⬆",
                    title = "حرکت سریع به بالا",
                    desc = "با اسکرول کردن به پایین لیست، دکمه‌ای شناور در گوشه پایین-چپ صفحه ظاهر می‌شود که با کلیک روی آن، فوراً به بالای لیست منتقل می‌شوید."
                )
                HelpItem(
                    emoji = "🌐",
                    title = "جستجوی زنده در وب",
                    desc = "در دیالوگ واکشی، لزوماً نیازی به دانلود کل دیتاست نیست. کافیست کلمه‌ای (حداقل ۳ حرف) را در نوار جستجو تایپ کنید تا نتایج مستقیماً از سرور Hugging Face دریافت شوند."
                )
                HelpItem(
                    emoji = "🌍",
                    title = "ترجمه خودکار عنوان‌ها",
                    desc = "عنوان تمام پرامپت‌ها (مثل پرامپت‌های انگلیسی) به صورت خودکار به زبان انتخاب‌شده در تنظیمات اپ (فارسی، انگلیسی، عربی، ترکی) ترجمه می‌شوند."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("متوجه شدم", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun HelpItem(
    emoji: String,
    title: String,
    desc: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(emoji, fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
