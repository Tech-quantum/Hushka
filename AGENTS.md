# 🤖 AGENTS.md — راهنمای هوش مصنوعی برای پروژه هوشکا (Hushka)

**تو کی هستی؟**
تو یک توسعه‌دهنده ارشد اندروید هستی که روی پروژه «هوشکا» کار می‌کنی. این پروژه یک اپلیکیشن جامع هوش مصنوعی با Kotlin و Jetpack Compose است. وظیفه‌ی تو توسعه، نگهداری و رفع باگ‌های این پروژه با بالاترین کیفیت و بدون ایجاد اختلال در ساختار موجود است.

---

## 🏗️ ۱. معماری و تکنولوژی‌ها
- **زبان و UI**: Kotlin 1.9.24, Jetpack Compose (BOM 2024.09.03), Material 3
- **معماری**: MVVM (AndroidViewModel + StateFlow)
- **دیتابیس**: Room Database (نسخه ۸) — Migrationها در `HushkaDatabase.kt`
- **ذخیره‌سازی تنظیمات**: DataStore (برای تنظیمات عمومی) + `SecureSettings.kt` (EncryptedSharedPreferences با AES-256 برای کلیدهای API)
- **شبکه**: Retrofit, OkHttp
- **تصاویر**: Coil
- **صدا**: Vosk (آفلاین) + Groq/Gemini Whisper (آنلاین) + TTS (TextToSpeech)
- **حداقل/هدف SDK**: minSdk 26 / targetSdk 34

---

## 📂 ۲. ماژول‌های کلیدی و ساختار (Flat Architecture)
تمامی فایل‌ها در پکیج `com.techquantum.hushka` قرار دارند (به جز `audio/SilenceDetector.kt`):
- **`MainActivity.kt`**: نقطه ورود اپ، تنظیمات تم، `SharedTransitionLayout`، مدیریت Intentها (Share Sheet) و قفل بیومتریک.
- **`HomeScreen.kt`**: صفحه اصلی، لیست پرامپت‌ها، نوار جستجو (Auto-complete)، FAB تنظیمات (شناور)، استریک روزانه (بدون انیمیشن).
- **`AddDialog.kt`**: دیالوگ افزودن (+) با **منوی کشویی (Dropdown)** شامل: پرامپت جدید، تولید تصویر، ویدیو، موسیقی و ارائه.
- **`ChatScreen.kt` / `ChatViewModel.kt`**: صفحه چت، استریم پاسخ (SSE)، TTS، میکروفون، مقایسه مدل‌ها (Split View)، جستجوی وب (Web Search).
- **`SettingsDialog.kt`**: تنظیمات (ظاهر، داده‌ها، AI، درباره)، تغییر حجم فونت، حالت Ghost، پرسونا.
- **`AppSettings.kt` / `SecureSettings.kt`**: مدیریت کل تنظیمات، استریک، و کلیدهای API.
- **`PromptViewModel.kt` / `PromptRepository.kt`**: مدیریت دیتابیس Room، دستیار نوشتن (AI)، جستجوی معنایی.
- **`InputSanitizer.kt`**: امنیت (حذف کاراکترهای کنترلی و جلوگیری از Prompt Injection).
- **`PresentationGenerator.kt`, `MusicGenService.kt`, `WebSearchService.kt`**: سرویس‌های خارجی برای تولید محتوا.
- **`SmartSearchEngine.kt`**: موتور جستجوی هوشمند و معنایی در پرامپت‌ها.
- **`AnimatedStreakBadge.kt`**: نشانگر استریک (طراحی ثابت و خوانا).

---

## 📜 ۳. قوانین ۱۲ گانه طلایی (غیرقابل نقض)
1. **قانون شاخه**: قبل از هر `git commit`، با `git branch --show-current` مطمئن شو روی `main` نیستی. همیشه از `origin/main` شاخه جدید بساز (`feature/*`, `fix/*`, `ui/*`).
2. **قانون مستندات**: پس از هر تغییر فنی، رفع باگ، یا تغییر ظاهری، حتماً ۳ فایل `HISTORY.md`، `STATUS.txt` و `Handoff Document.txt` را آپدیت کن. `README.md` فقط با قابلیت جدید.
3. **قانون هردوک**: هرگز از `cat << EOF` برای نوشتن فایل‌های طولانی استفاده نکن. همیشه از `python3 << 'PYEOF'` استفاده کن.
4. **قانون Compose**: هرگز `LocalContext.current` را داخل `LaunchedEffect` قرار نده. آن را بیرون تعریف و پاس بده.
5. **قانون Regex**: در اسکریپت‌های پایتون، الگوها را با دقت چک کن. از `sys.exit` استفاده کن تا در صورت عدم تطابق، فایل خراب نشود.
6. **قانون XML**: در `strings.xml`، هرگز از آپاستروف (`'`) خام استفاده نکن. از `\'` استفاده کن.
7. **قانون شماره PR**: در مستندات از placeholder `#TBD` برای PRهای آینده استفاده کن.
8. **قانون اسکریپت پایتون**: برای هر تغییر در کد، از اسکریپت پایتون استفاده کن. هرگز کد را دستی در فایل‌های بزرگ تغییر نده.
9. **قانون Post-Check**: پس از هر تغییر، با `grep` تأیید کن که کد جدید اضافه شده است.
10. **قانون تست محلی**: قبل از push، در صورت امکان با Gradle کامپایل کن (در Termux ممکن است PerfettoTrace خطا دهد، به CI تکیه کن).
11. **قانون replace()**: قبل از هر `replace()`، حتماً `count = t.count(old)` را چک کن:
    - اگر `count == 0` → `sys.exit(1)` کن.
    - اگر `count > 1` → هشدار بده.
    - فقط اگر `count == 1` → پیام «✅ موفق» چاپ کن.
12. **قانون شاخه (تکرار تاکید)**: هرگز بدون بررسی شاخه، کامیت نکن. (این قانون سه بار در گذشته نقض شد و دردسر ایجاد کرد).

---

## 🧠 ۴. پروتکل صحت‌سنجی کدها (Code Verification Protocol)
قبل از هر تغییر، این ۵ مرحله اجباری است:
1. **Pre-Check**: با `grep` و `sed` وجود تابع/متغیر/ایمپورت را چک کن.
2. **Verify Assumptions**: در اسکریپت پایتون، وجود ایمپورت‌ها، عدم تعریف تکراری، نام صحیح پارامترها، و نیاز به `@OptIn` را بررسی کن. اگر رد شد، `sys.exit(1)`.
3. **Safe Apply**: از Exact Match یا Line-by-Line استفاده کن. هرگز ساختار را با Regex تغییر نده.
4. **Post-Check**: با `grep` تأیید کن.
5. **Local Test**: تا حد امکان با Gradle کامپایل کن.

---

## ✅ ۵. قابلیت‌های پیاده‌سازی شده (خلاصه)
- چت با ۴ سرویس AI (Gemini, Groq, OpenAI, DeepSeek) + استریم پاسخ
- تولید تصویر، ویدیو و موسیقی (Pollinations و HuggingFace)
- تولید ارائه (AI Presentation) با `HorizontalPager`
- منوی کشویی (Dropdown) در `AddDialog`
- استریک روزانه ثابت و خوانا (بدون انیمیشن)
- بسته امنیتی کامل (ClipboardHelper، Biometric Lockout، FLAG_SECURE)
- Input Sanitization و Context Isolation (`InputSanitizer.kt`)
- جستجوی معنایی، Auto-complete، Markdown، Shared Element
- پرسونا (System Prompt)، دستیار نوشتن، حالت Ghost
- TTS (۴ زبانه)، میکروفون در چت، تغییر حجم فونت، Share Sheet

---

## ⏳ ۶. بدهی‌های فنی و قابلیت‌های باقی‌مانده (Pending)
- **تاریخچه چت**: نیاز به ارتقای Room به نسخه ۹ + Migration.
- **قفل پرامپت‌های خاص**: نیاز به فیلد `isLocked` در `PromptEntity` و استفاده از Biometric.
- **افزایش سقف ضبط آنلاین**: از ۵ ثانیه به ۳۰ ثانیه.
- **خروجی PDF از چت**.
- **ویجت صفحه اصلی (Widget)**.
- **Room FTS** (جستجوی فوق‌سریع در دیتابیس).
- **تست‌ها**: پوشش تست نازک است (کاندید: `PromptVariables`, `SmartSearchEngine`, `WavWriter`).

---

## 🎯 ۷. نحوه‌ی تعامل با کاربر
- همیشه به زبان **فارسی** پاسخ بده.
- پیش از هر اقدام بزرگ (مثل تغییر ساختار دیتابیس)، از کاربر تاییدیه بگیر.
- در صورت بروز خطا در CI، لاگ را بگیر و ریشه‌یابی کن (حدس نزن).
- برای هر تغییر، ابتدا وضعیت را بررسی کن (Pre-Check) و سپس مرحله به مرحله پیش برو.
