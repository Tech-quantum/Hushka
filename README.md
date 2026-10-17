# 💎 هوشکا (Hushka)

<div align="center">

**بستری برای کاوش، خلق و مدیریت ایده‌های هوش مصنوعی**

[![Build Hushka APK](https://github.com/Tech-quantum/Hushka/actions/workflows/build-apk.yml/badge.svg)](https://github.com/Tech-quantum/Hushka/actions/workflows/build-apk.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-BOM%202024.09.03-green.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-M3-blue.svg)](https://m3.material.io)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## 📖 درباره پروژه

**Hushka (هوشکا)** یک اپلیکیشن اندروید مدرن برای مدیریت، کاوش و خلق ایده‌های هوش مصنوعی است. این اپ به شما امکان می‌دهد پرامپت‌های خود را ذخیره، دسته‌بندی، جستجو کنید، با ۴ سرویس هوش مصنوعی چت کنید و از مخازن آنلاین پرامپت واکشی کنید.

### 🎯 چرا Hushka؟

- 🤖 **چت با AI**: پشتیبانی از Gemini, Groq, OpenAI, DeepSeek
- 💾 **کش هوشمند**: ذخیره‌سازی نتایج واکشی برای بارگذاری فوری (In-Memory + Disk)
- 🎨 **رابط کاربری مدرن**: Material 3، ۵ تم رنگی، انیمیشن‌های روان
- 🌍 **چندزبانه**: فارسی، انگلیسی، عربی، ترکی (با پشتیبانی کامل RTL/LTR)
- 🚀 **UX حرفه‌ای**: Swipe-to-Delete، دکمه شناور رفتن به بالا، Splash Screen
- 🌐 **واکشی از ۹ منبع**: GitHub و Hugging Face
- 📚 **~۱۹۱۳ پرامپت داخلی** + ~۱۶۸۰ پرامپت آنلاین

---

## ✨ قابلیت‌ها

### 🤖 چت با هوش مصنوعی
- **صفحه چت اختصاصی**: ارسال خودکار پرامپت با کلیک روی دکمه اشتراک‌گذاری
- **۴ سرویس‌دهنده**:
  - 🔵 Google Gemini (رایگان): [دریافت کلید](https://aistudio.google.com/apikey)
  - ⚡ Groq (رایگان و سریع): [دریافت کلید](https://console.groq.com/keys)
  - 🟢 OpenAI (شارژ): [دریافت کلید](https://platform.openai.com/account/api-keys)
  - 🐋 DeepSeek (شارژ): [دریافت کلید](https://platform.deepseek.com/api_keys)
- **تب AI در تنظیمات**: انتخاب سرویس، وارد کردن کلید و انتخاب مدل
- **مدیریت خطا**: پیام‌های واضح برای کلید نامعتبر، اعتبار ناکافی، محدودیت نرخ و...

### 💾 کش هوشمند (Web Cache)
- **ذخیره‌سازی دوگانه**: در حافظه (RAM) برای سرعت فوری + فایل برای پایداری
- **اعتبار ۲۴ ساعته**: کش بعد از ۲۴ ساعت خودکار باطل می‌شود
- **دکمه به‌روزرسانی (🔄)**: در دیالوگ واکشی برای دور زدن کش و دریافت داده جدید
- **دکمه پاک کردن کش (🗑)**: در تنظیمات -> تب داده‌ها
- **عملکرد**: بار اول از وب، بار دوم <۱۰۰ میلی‌ثانیه

### 🌐 واکشی از وب (۹ منبع)
GitHub (awesome-chatgpt-prompts) + ۸ دیتاست Hugging Face (Prompts.chat, Stable Diffusion, Jailbreak, Persian, Dolly, WritingPrompts, CodeAlpaca, Medical).

### 🎨 رابط کاربری و UX
- **Splash Screen**: لوگوی LED درخشان با انیمیشن Fade + Scale
- **۵ تم رنگی**: بنفش، قرمز، سبز، آبی، نارنجی + حالت روشن/تیره/سیستم
- **انیمیشن کارت‌ها**: ورود تدریجی (Staggered) با Fade + Slide Up
- **Swipe-to-Delete**: حذف کارت با کشیدن به چپ + دیالوگ تایید
- **Scroll-to-Top FAB**: دکمه شناور برای بازگشت سریع به بالای لیست (همیشه چپ)
- **جستجوی هوشمند**: جستجو در عنوان و متن با پشتیبانی از فیلترهای مختلف
- **ترجمه خودکار عنوان‌ها**: عناوین پرامپت‌ها به صورت خودکار به زبان رابط کاربری (فارسی، انگلیسی، عربی، ترکی) ترجمه می‌شوند.
- **زبان‌ها**: فارسی (RTL)، انگلیسی (LTR)، عربی (RTL)، ترکی (LTR)

---

## 🛠 تکنولوژی‌ها

| تکنولوژی | نسخه | کاربرد |
|---|---|---|
| Kotlin | 1.9.24 | زبان اصلی |
| Jetpack Compose | BOM 2024.09.03 | رابط کاربری |
| Material 3 | M3 | طراحی |
| Room Database | 2.6.1 | دیتابیس محلی (نسخه ۸) |
| Coroutines & Flow | - | برنامه‌نویسی async |
| DataStore | 1.1.1 | ذخیره تنظیمات |
| Retrofit + OkHttp | 2.11.0 / 4.12.0 | ارتباط با APIهای AI |
| Vazirmatn Font | - | فونت فارسی |

---

## 🚀 نصب و اجرا

### پیش‌نیازها
- Android Studio Hedgehog (2023.1.1) یا جدیدتر
- JDK 17
- Android SDK 35
- حداقل اندروید 8.0 (API 26)

### مراحل نصب
```bash
# 1. کلون پروژه
git clone https://github.com/Tech-quantum/Hushka.git
cd Hushka

# 2. باز کردن در Android Studio
# File -> Open -> مسیر پروژه

# 3. Build و Run
# دکمه Run (Shift+F10)
```

---

## 📂 ساختار پروژه

```text
app/src/main/java/com/techquantum/hushka/
├── MainActivity.kt              # نقطه ورود
├── SplashScreen.kt              # صفحه آغازین
├── HomeScreen.kt                # صفحه اصلی + Scroll FAB
├── PromptCard.kt                # کارت پرامپت + Swipe + Animation
│
├── ChatScreen.kt                # صفحه چت با AI
├── ChatViewModel.kt             # مدیریت State چت
├── ChatMessage.kt               # مدل داده چت
├── AiService.kt                 # سرویس API (Retrofit)
├── AiProvider.kt                # enum سرویس‌ها
│
├── WebPromptsDialog.kt          # دیالوگ واکشی + دکمه Refresh
├── WebSource.kt                 # enum منابع وب
├── WebCacheManager.kt           # کش In-Memory + Disk
│
├── SettingsDialog.kt            # تنظیمات (۴ تب: ظاهر، داده، AI، درباره)
├── PromptDatabase.kt            # Room DB (v8)
├── PromptRepository.kt          # ریپازیتوری
├── PromptViewModel.kt           # مدیریت State اصلی
└── ... (سایر فایل‌ها)
```

---

## 📄 مجوز

این پروژه تحت لایسنس **MIT** منتشر شده است.

---

**ساخته شده با ❤️ برای جامعه‌ی هوش مصنوعی فارسی‌زبان**
