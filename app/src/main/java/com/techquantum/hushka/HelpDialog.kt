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
fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("راهنمای کاربری هوشکا 📚", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HelpItem("🤖", "چت با هوش مصنوعی", "ابتدا در تنظیمات (تب AI) سرویس‌دهنده را انتخاب کرده و کلید API خود را وارد کنید. سپس روی هر پرامپت دکمه «اشتراک‌گذاری» را بزنید تا صفحه چت باز شده و پرامپت به صورت خودکار برای هوش مصنوعی ارسال شود.")
                HelpItem("🎤", "جستجوی صوتی هوشمند", "میکروفون هوشکا به‌صورت خودکار انتخاب می‌کند: اگر اینترنت دارید از Groq Whisper (سریع و دقیق) و اگر آفلاین هستید از Vosk به‌صورت کاملاً محلی استفاده می‌شود. کافیست روی آیکون میکروفون بزنید و صحبت کنید.")
                HelpItem("💾", "کش هوشمند (Cache)", "نتایج واکشی از وب برای بارگذاری سریع‌تر در حافظه ذخیره می‌شوند. اگر می‌خواهید داده‌های جدیدی از اینترنت دریافت کنید، در دیالوگ واکشی دکمه «به‌روزرسانی» را بزنید. برای آزادسازی حافظه، از تنظیمات (تب داده‌ها) کش را پاک کنید.")
                HelpItem("🏷️", "فیلتر پیشرفته تگ‌ها", "با کلیک روی چیپ «تگ‌ها» در صفحه اصلی، می‌توانید چند تگ را همزمان انتخاب کنید. با انتخاب حالت AND فقط پرامپت‌هایی نمایش داده می‌شوند که همه تگ‌های انتخاب‌شده را با هم دارند.")
                HelpItem("🌍", "ترجمه خودکار عنوان‌ها", "عنوان تمام پرامپت‌ها (مثل پرامپت‌های انگلیسی) به صورت خودکار به زبان انتخاب‌شده در تنظیمات اپ (فارسی، انگلیسی، عربی، ترکی) ترجمه می‌شوند.")
                HelpItem("📊", "آمار و تحلیل", "از نوار پایین صفحه، به بخش «آمار» بروید تا تعداد کل پرامپت‌ها، علاقه‌مندی‌ها، و نمودارهای پیشرفته (توزیع دسته‌بندی و سرویس‌دهنده‌ها) را ببینید.")
                HelpItem("🗑", "حذف سریع (Swipe)", "برای حذف سریع، کارت پرامپت را با انگشت به سمت چپ بکشید (Swipe-to-Delete) و سپس حذف را تایید کنید.")
                HelpItem("⬆", "حرکت سریع به بالا", "با اسکرول کردن به پایین لیست، دکمه‌ای شناور در گوشه پایین-چپ صفحه ظاهر می‌شود که با کلیک روی آن، فوراً به بالای لیست منتقل می‌شوید.")
                HelpItem("🌐", "جستجوی زنده در وب", "در دیالوگ واکشی، لزوماً نیازی به دانلود کل دیتاست نیست. کافیست کلمه‌ای (حداقل ۳ حرف) را در نوار جستجو تایپ کنید تا نتایج مستقیماً از سرور Hugging Face دریافت شوند.")
                HelpItem("➕", "افزودن و ویرایش پرامپت", "با کلیک روی دکمه شناور (+) در پایین صفحه، می‌توانید پرامپت جدیدی بسازید. برای ویرایش، روی کارت پرامپت کلیک کرده و دکمه ✏️ (مداد) را بزنید.")
                HelpItem("⭐", "علاقه‌مندی‌ها و پین", "روی هر کارت، آیکون ⭐ برای نشانه‌گذاری (علاقه‌مندی) و آیکون 📌 برای پین کردن (ثابت ماندن در بالای لیست) استفاده می‌شود.")
                HelpItem("📥", "واکشی از وب (حالت عادی)", "در دیالوگ واکشی، می‌توانید منبع (مثل GitHub) را انتخاب کرده و دکمه «واکشی» را بزنید تا لیست پرامپت‌های آن دانلود شود. سپس با تیک زدن هر پرامپت و زدن دکمه «افزودن»، آن را به دیتابیس خود منتقل کنید.")
                HelpItem("📤", "پشتیبان‌گیری و بازیابی", "در تنظیمات (تب داده‌ها)، می‌توانید از کل پرامپت‌های خود یک فایل JSON یا Markdown تهیه کنید. همچنین می‌توانید فایل JSON قبلی خود را وارد (Import) کنید.")
                HelpItem("✨", "تحلیل پیشرفته پرامپت", "روی هر کارت، دکمه ✨ (آنالیز) را بزنید تا هوشکا کیفیت پرامپت شما را از نظر شفافیت، تعریف نقش، ساختار خروجی و امنیت بررسی کرده و امتیاز (0 تا 100) بدهد.")
                HelpItem("▶", "تست زنده و متغیرها", "دکمه ▶ (تست) روی کارت‌ها، متغیرهای داخل پرامپت (مثل [SUBJECT]) را شناسایی می‌کند. با پر کردن فرم، می‌توانید پیش‌نمایش نهایی پرامپت را قبل از کپی شدن ببینید.")
                HelpItem("🌐", "ترجمه دستی پرامپت", "با کلیک روی دکمه 🌐 (ترجمه) روی هر کارت، می‌توانید متن آن پرامپت خاص را به زبان دلخواه (فارسی، انگلیسی، عربی، ترکی) ترجمه کنید.")
                HelpItem("🎨", "تم‌ها و ظاهر", "در تنظیمات (تب ظاهر)، می‌توانید بین ۵ تم رنگی (بنفش، قرمز، سبز، آبی، نارنجی) و حالت روشن/تیره/سیستم جابه‌جا شوید.")
                HelpItem("🔄", "به‌روزرسانی کتابخانه", "در نوار پایین صفحه، آیکون «به‌روزرسانی» (ابر) پرامپت‌های جدید و تغییرات کتابخانه آنلاین را دریافت کرده و با دیتابیس شما همگام می‌کند.")
                HelpItem("☁️", "وضعیت اتصال به اینترنت", "در کنار آیکون به‌روزرسانی، یک نقطه سبز (آنلاین) یا قرمز (آفلاین) وجود دارد. در حالت آفلاین، اپ از داده‌های ذخیره‌شده استفاده می‌کند.")
                HelpItem("🔀", "مرتب‌سازی هوشمند", "با کلیک روی دکمه مرتب‌سازی (کنار نوار جستجو)، می‌توانید پرامپت‌ها را بر اساس «مرتبط‌ترین»، «جدیدترین»، «پربازدیدترین» یا «بیشترین امتیاز» مرتب کنید.")
                HelpItem("🎭", "شخصیت‌ها و نقش‌ها", "هوشکا شامل ۲۰ نقش آماده (مثل برنامه‌نویس، نویسنده، دکتر) است. هنگام ساخت پرامپت جدید، می‌توانید یک نقش انتخاب کنید تا هوش مصنوعی بهتر در آن زمینه پاسخ دهد.")
                HelpItem("🧩", "فیلتر Provider و دسته‌بندی", "در بالای صفحه اصلی، می‌توانید پرامپت‌ها را بر اساس سرویس‌دهنده (مثل Gemini یا OpenAI) یا دسته‌بندی (مثل روانشناسی یا کدنویسی) فیلتر کنید.")
                HelpItem("📋", "تفاوت دکمه‌های کارت", "دکمه «کپی» فقط متن را در کلیپ‌بورد کپی می‌کند. دکمه «اشتراک‌گذاری» صفحه چت AI را باز می‌کند. دکمه «ترجمه» (🌐) پرامپت را به زبان دلخواه ترجمه می‌کند.")
                HelpItem("✏️", "مدیریت فیلترها", "با انتخاب چند فیلتر (مثل علاقه‌مندی‌ها + یک تگ خاص)، می‌توانید ترکیبی از آن‌ها را بسازید. با دکمه ضربدر (✕) در چیپ‌ها، فیلتر را لغو کنید.")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("متوجه شدم", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun HelpItem(emoji: String, title: String, desc: String) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(emoji, fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            AnimatedVisibility(visible = expanded) {
                Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp), lineHeight = 18.sp)
            }
        }
    }
}
