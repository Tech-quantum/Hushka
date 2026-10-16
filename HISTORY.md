# 📱 پروژه Hushka — تاریخچه کامل تغییرات

> **آخرین به‌روزرسانی:** مهر ۱۴۰۵
> **ریپو:** https://github.com/Tech-quantum/Hushka
> **پکیج:** com.techquantum.hushka

---

## 🎯 هدف پروژه

ساخت یک اپلیکیشن اندروید حرفه‌ای برای مدیریت، کاوش و خلق ایده‌های هوش مصنوعی.

**شعار:** «هوشکا بستری برای کاوش، خلق و مدیریت ایده‌های هوش مصنوعی»

---

## 📊 آمار نهایی پروژه

### پرامپت‌های داخلی اپ

| دسته | تعداد |
|---|---|
| 🧠 روانشناسی | 525 |
| 🌍 پرامپت‌های فارسی | 1120 |
| 🏡 زندگی | 80 |
| 📦 Catalog (انگلیسی) | 94 |
| 🌱 Seed | 22 |
| 🎯 OpenAI/Claude | 30 |
| 💜 Lovable/DeepSeek | 22 |
| 🎨 Midjourney | 20 |
| **جمع داخلی** | **~1913** |

### پرامپت‌های قابل واکشی از وب

| منبع | تعداد |
|---|---|
| 🌐 همه (یکجا) | ~1680 |
| 📦 GitHub | ~200 |
| 🤗 Prompts.chat | ~500 |
| 🎨 Stable Diffusion | ~500 |
| 🔓 Jailbreak | ~100 |
| 🇮🇷 Persian | ~300 |
| 📋 Dolly 15k | ~300 |
| ✍️ WritingPrompts | ~300 |
| 💻 CodeAlpaca | ~300 |
| 🩺 Medical | ~200 |
| **جمع کل** | **~3600** |

---

## 🎨 UI/UX

- ✅ 5 تم رنگی (بنفش، قرمز، سبز، آبی، نارنجی)
- ✅ تم روشن/تیره/سیستم
- ✅ 4 زبان (فارسی، انگلیسی، عربی، ترکی)
- ✅ RTL/LTR خودکار
- ✅ ModernChip با انیمیشن spring
- ✅ chip های رنگی برند
- ✅ گروه‌بندی دسته‌ها با header
- ✅ کارت‌های پرامپت با گرادیان ملایم
- ✅ تگ‌های رنگی
- ✅ metadata (زمان آخرین استفاده)
- ✅ انیمیشن press (scale 0.98)
- ✅ دکمه‌های کارت مدرن
- ✅ تنظیمات با 4 تب (ظاهر، داده، AI، درباره)
- ✅ دو بار بک برای خروج
- ✅ لوگوی LED درخشان 💎
- ✅ Splash Screen با انیمیشن
- ✅ انیمیشن ورود کارت‌ها (staggered)
- ✅ Swipe-to-Delete

---

## 🤖 چت با هوش مصنوعی

### سرویس‌های پشتیبانی‌شده

| سرویس | Emoji | رایگان | لینک کلید |
|---|---|---|---|
| Google Gemini | 🔵 | ✅ | https://aistudio.google.com/app/apikey |
| Groq | ⚡ | ✅ | https://console.groq.com/keys |
| OpenAI | 🟢 | ❌ | https://platform.openai.com/account/api-keys |
| DeepSeek | 🐋 | ❌ | https://platform.deepseek.com/api_keys |

### قابلیت‌ها

- ✅ صفحه چت با UI مدرن
- ✅ ارسال خودکار پرامپت هنگام باز شدن
- ✅ حباب‌های پیام (کاربر/هوشکا/خطا)
- ✅ نشانگر تایپ
- ✅ regenerate و clear chat
- ✅ تب AI در تنظیمات (انتخاب Provider، کلید، مدل)
- ✅ خطاهای کاربرپسند

---

## 💾 کش هوشمند

- ✅ کش فایل‌محور (WebCacheManager)
- ✅ اعتبار 24 ساعته
- ✅ دکمه 🔄 به‌روزرسانی در دیالوگ واکشی
- ✅ پیام موفقیت مرکزی چندزبانه
- ✅ دکمه 🗑 پاک کردن کش در تنظیمات
- ✅ Fallback آفلاین

---

## 🌐 واکشی از وب

### منابع (9 منبع)

1. 📦 GitHub: awesome-chatgpt-prompts (CSV)
2. 🤗 Hugging Face - fka/prompts.chat (API)
3. 🎨 Hugging Face - Stable-Diffusion-Prompts (API)
4. 🔓 Hugging Face - ChatGPT-Jailbreak-Prompts (API)
5. 🇮🇷 Hugging Face - Persian datasets (API)
6. 📋 Hugging Face - Dolly 15k (API)
7. ✍️ Hugging Face - WritingPrompts (API)
8. 💻 Hugging Face - CodeAlpaca (API)
9. 🩺 Hugging Face - Medical Flashcards (API)

### قابلیت‌ها

- ✅ لودینگ پویا با متن‌های زمان‌بندی شده
- ✅ جستجوی زنده در نتایج
- ✅ انتخاب همه / لغو انتخاب
- ✅ ساب‌تایتل داینامیک
- ✅ تحمل خطا (per-source try/catch)
- ✅ Pagination (100 ردیف/درخواست)

---

## 🌐 ترجمه

- ✅ MyMemory API (رایگان، بدون توکن)
- ✅ ترجمه درون‌برنامه‌ای پرامپت‌ها
- ✅ حفاظت از markup ها
- ✅ Chunked translation (480 کاراکتر/درخواست)

---

## 🔧 تکنولوژی‌ها

| مورد | نسخه |
|---|---|
| Kotlin | 1.9.24 |
| Jetpack Compose BOM | 2024.09.03 |
| Material 3 | M3 |
| Room Database | 2.6.1 |
| DataStore | 1.1.1 |
| Retrofit | 2.11.0 |
| OkHttp Logging | 4.12.0 |
| Vazirmatn Font | - |
| Min SDK | 26 |
| Target SDK | 35 |

---

## 📁 فایل‌های اصلی پروژه

### صفحه اصلی و UI

- `MainActivity.kt` — نقطه ورود
- `HomeScreen.kt` — صفحه اصلی
- `HomeTopBar.kt` / `HomeBottomBar.kt` / `HomeFloatingActionButton.kt`
- `PromptCard.kt` — کارت پرامپت
- `PromptFormDialog.kt` — فرم افزودن
- `SettingsDialog.kt` — تنظیمات (4 تب)
- `SplashScreen.kt` — صفحه آغازین
- `HushkaLogo.kt` — لوگوی LED
- `ModernChip.kt` — چیپ مدرن
- `StatisticsScreen.kt` — آمار
- `TagColorManagerDialog.kt`

### چت AI

- `ChatScreen.kt` — صفحه چت
- `ChatViewModel.kt` — مدیریت State
- `ChatMessage.kt` — مدل داده
- `AiService.kt` — سرویس API
- `AiProvider.kt` — enum

### دیتابیس

- `PromptDatabase.kt` — Room DB
- `PromptDao.kt` / `WebCacheDao.kt`
- `PromptEntity.kt` / `WebCacheEntity.kt`
- `PromptRepository.kt` / `PromptRepositoryImpl.kt`
- `PromptViewModel.kt` / `PromptViewModelFactory.kt`

### وب

- `WebPromptsDialog.kt`
- `WebSource.kt`
- `WebCacheManager.kt` — کش فایل‌محور

### ترجمه

- `TranslationService.kt`
- `TranslatedPromptDialog.kt`
- `CategoryTranslator.kt`
- `LanguageDetector.kt`

---

## 📦 فایل‌های Assets (JSON)

- `prompts_catalog_v1.2.0.json` (94)
- `prompts_persian_v0.1.0.json` (1120)
- `prompts_life_v0.1.0.json` (80)
- `prompts_psychology_v0.2.0.json` (110)
- `prompts_psychology_v0.3.0.json` (127)
- `prompts_psychology_v0.4.0.json` (120)
- `prompts_psychology_v0.5.0.json` (123)
- `prompts_psychology_v0.6.0.json` (45)
- `prompts_seed_v0.1.0.json` (22)
- `prompts_openai_claude_v0.1.0.json` (30)
- `prompts_midjourney_v0.1.0.json` (20)
- `prompts_lovable_deepseek_v0.1.0.json` (22)

---

## 🎯 Pull Requests Merge شده

| # | عنوان | وضعیت |
|---|---|---|
| #2 | Add AI Chat + Web Cache | ✅ |
| #3 | Add Splash Screen | ✅ |
| #4 | Cache UI: refresh + clear | ✅ |
| #5 | i18n: full translation | ✅ |
| — | Add card animation | ✅ |
| — | Add Swipe-to-Delete | ✅ |

---

## 🏆 دستاورد کل پروژه

- ✅ اپ حرفه‌ای با 4 زبان کامل
- ✅ ~1913 پرامپت داخلی
- ✅ ~1680 پرامپت قابل واکشی
- ✅ ~3600 پرامپت در دسترس کاربر
- ✅ چت با 4 سرویس AI
- ✅ کش هوشمند 24 ساعته
- ✅ ترجمه درون‌برنامه‌ای
- ✅ UI/UX مدرن
- ✅ 525 زیردسته روانشناسی
- ✅ 9 منبع واکشی وب
- ✅ Splash Screen با انیمیشن
- ✅ انیمیشن ورود کارت‌ها
- ✅ Swipe-to-Delete
- ✅ README کامل

**پروژه Hushka در سطح یک اپ حرفه‌ای کامله! 🚀**

---

## 📝 پایان فایل — تهیه شده در مهر ۱۴۰۵
