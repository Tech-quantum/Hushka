═══════════════════════════════════════════════════════════
📱 پروژه Hushka — تاریخچه کامل تغییرات
═══════════════════════════════════════════════════════════

📅 تاریخ شروع: مهر ۱۴۰۵
🌐 ریپو: https://github.com/Tech-quantum/Hushka
📦 پکیج: com.techquantum.hushka


═══════════════════════════════════════════════════════════
🎯 هدف پروژه
═══════════════════════════════════════════════════════════

ساخت یک اپلیکیشن اندروید حرفه‌ای برای مدیریت، کاوش و خلق 
ایده‌های هوش مصنوعی.

شعار: «هوشکا بستری برای کاوش، خلق و مدیریت ایده‌های هوش مصنوعی»


═══════════════════════════════════════════════════════════
📊 آمار نهایی پروژه
═══════════════════════════════════════════════════════════

پرامپت‌های داخلی اپ:
  🧠 روانشناسی            : 525
  🌍 پرامپت‌های فارسی      : 1120
  🏡 زندگی                : 80
  📦 Catalog (انگلیسی)    : 94
  🌱 Seed                 : 22
  🎯 OpenAI/Claude        : 30
  💜 Lovable/DeepSeek     : 22
  🎨 Midjourney           : 20
  ────────────────────────────
  جمع داخلی               : ~1913

پرامپت‌های قابل واکشی از وب:
  🌐 همه (یکجا)           : ~1680
  📦 GitHub               : ~200
  🤗 Prompts.chat         : ~500
  🎨 Stable Diffusion     : ~500
  🔓 Jailbreak            : ~100
  🇮🇷 Persian             : ~300
  📋 Dolly 15k            : ~300
  ✍️ WritingPrompts       : ~300
  💻 CodeAlpaca           : ~300
  🩺 Medical              : ~200

  🚀 جمع کل قابل دسترسی: ~3600 پرامپت


═══════════════════════════════════════════════════════════
🎨 UI/UX
═══════════════════════════════════════════════════════════

✅ 5 تم رنگی (بنفش، قرمز، سبز، آبی، نارنجی)
✅ تم روشن/تیره/سیستم
✅ 4 زبان (فارسی، انگلیسی، عربی، ترکی)
✅ RTL/LTR خودکار
✅ ModernChip با انیمیشن spring
✅ chip های رنگی برند (OpenAI، Gemini، Claude، DeepSeek)
✅ گروه‌بندی دسته‌ها با header (Option B)
✅ بازطراحی کارت‌های پرامپت:
   - گرادیان ملایم بر اساس دسته
   - تگ‌های رنگی
   - metadata (زمان آخرین استفاده)
   - انیمیشن press (scale 0.98)
✅ دکمه‌های کارت مدرن:
   - ⭐ ستاره طلایی پر (Favorite)
   - 📌 پین چرخشی بنفش (Pin)
   - 👁 چشم خاکستری (Preview)
✅ تنظیمات با 3 تب:
   - 🎨 ظاهر (تم، رنگ، زبان، مدیریت رنگ‌ها)
   - 📦 داده‌ها (پشتیبان‌گیری، Cloud)
   - ℹ️ درباره (اطلاعات اپ)
✅ دو بار بک برای خروج (Toast صمیمی)
✅ ✕ → «بستن» در دیالوگ‌ها
✅ لوگوی LED درخشان 💎:
   - 8 رنگ در چرخه 6 ثانیه
   - درخشش پالس 1.4 ثانیه
   - ثابت چپ (مستقل از زبان)
   - عنوان وسط‌چین
✅ statusBarsPadding
✅ اسکرول کل صفحه با LazyColumn


═══════════════════════════════════════════════════════════
🌐 واکشی از وب
═══════════════════════════════════════════════════════════

منابع واکشی:
  1. 📦 GitHub: awesome-chatgpt-prompts (CSV)
  2. 🤗 Hugging Face - fka/prompts.chat (API)
  3. 🎨 Hugging Face - Stable-Diffusion-Prompts (API)
  4. 🔓 Hugging Face - ChatGPT-Jailbreak-Prompts (API)
  5. 🇮🇷 Hugging Face - Persian datasets (API)
  6. 📋 Hugging Face - Dolly 15k (API)
  7. ✍️ Hugging Face - WritingPrompts (API)
  8. 💻 Hugging Face - CodeAlpaca (API)
  9. 🩺 Hugging Face - Medical Flashcards (API)
 10. 🌐 همه (یکجا - fetchAllSources)

قابلیت‌ها:
✅ لودینگ پویا با متن‌های زمان‌بندی شده
✅ جستجوی زنده در نتایج (case-insensitive)
✅ انتخاب همه / لغو انتخاب
✅ ساب‌تایتل داینامیک (نام منبع)
✅ تحمل خطا (per-source try/catch)
✅ Pagination (100 ردیف/درخواست)
✅ extraField برای ادغام ستون‌ها


═══════════════════════════════════════════════════════════
🌐 ترجمه
═══════════════════════════════════════════════════════════

✅ MyMemory API (رایگان، بدون توکن)
✅ پشتیبانی از هر جفت زبان (source → target)
✅ ترجمه درون‌برنامه‌ای پرامپت‌ها:
   - دکمه 🌐 در کارت
   - دیالوگ TranslatedPromptDialog
   - حالت لودینگ، خطا، Retry
   - کپی + اشتراک
✅ حفاظت از markup ها ([xxx] و #number)
✅ Chunked translation (480 کاراکتر/درخواست)


═══════════════════════════════════════════════════════════
📚 محتوای پرامپت‌ها
═══════════════════════════════════════════════════════════

🧠 روانشناسی (525 زیردسته در 5 فاز):
  • v0.2.0: 110 (اضطراب، خلق، تروما، اعتیاد، روابط، والدگری...)
  • v0.3.0: 127 (شخصیت، تنوع عصبی، مثبت، تحولی، صمیمیت...)
  • v0.4.0: 120 (علوم اعصاب، فرهنگی، معنوی، دیجیتال...)
  • v0.5.0: 123 (مربیگری، فلسفه، هیجان، خودمراقبتی، ذهن-بدن...)
  • v0.6.0: 45 (نورولوژیک، درد مزمن، اجتماعی-سیاسی، خاص)

🌍 پرامپت‌های فارسی (1120 در 14 دسته):
  ai, business, career, code, data, fun, image, life,
  logo, seo, social, study, video, writing

🏡 زندگی (80 در 7 زیردسته):
  food, money, travel, health, family, habits, home

📦 سایر:
  • Catalog: 94 پرامپت تصویری
  • Seed: 22 پرامپت اولیه
  • OpenAI/Claude: 30 پرامپت
  • Lovable/DeepSeek: 22 پرامپت
  • Midjourney: 20 پرامپت


═══════════════════════════════════════════════════════════
🔧 تکنولوژی‌ها
═══════════════════════════════════════════════════════════

• Kotlin                  1.9.24
• Jetpack Compose BOM     2024.09.03
• Material 3              M3
• Room Database           2.6.1
• Coroutines              -
• DataStore               1.1.1
• Retrofit                2.11.0
• OkHttp Logging          4.12.0
• Vazirmatn Font          (فارسی)


═══════════════════════════════════════════════════════════
🐛 Fix های مهم
═══════════════════════════════════════════════════════════

✅ حذف پکیج زائد ai/ (14 فایل خالی)
✅ رفع خطای PromptViewModelFactory.kt
✅ جایگزینی HF منسوخ با MyMemory
✅ پاکسازی توکن HF از 7 فایل
✅ Rename HuggingFaceService → TranslationService
✅ Fix contributor (Tech-quantum)
✅ رفع دسته‌بندی‌های تکراری
✅ رفع ایموجی دوبار در CategoryPickerDialog
✅ Fix duplicate @Composable annotation
✅ Fix missing web_source_all string
✅ Fix missing LazyRow import
✅ Fix exhaustive when expressions (5 branch)
✅ Fix fetchAllSources definition
✅ Fix sourceSubtitle language-aware
✅ Fix logo position (always left + centered)
✅ حذف فایل‌های .save قدیمی


═══════════════════════════════════════════════════════════
📁 فایل‌های اصلی پروژه
═══════════════════════════════════════════════════════════

MainActivity.kt              - نقطه ورود + دو بار بک
HomeScreen.kt                - صفحه اصلی (~660 خط)
PromptCard.kt                - کارت پرامپت
PromptFormDialog.kt          - فرم افزودن/ویرایش
HomeDialogs.kt               - دیالوگ‌ها
SettingsDialog.kt            - تنظیمات (3 تب)
WebPromptsDialog.kt          - واکشی از وب
TranslatedPromptDialog.kt    - دیالوگ ترجمه
AdvancedAnalyzeDialog.kt     - آنالیز پیشرفته
PromptToolsDialogs.kt        - تست زنده
CategoryPickerDialog.kt      - انتخاب دسته
TagColorManagerDialog.kt     - مدیریت رنگ تگ
StatisticsScreen.kt          - آمار
HushkaLogo.kt                - لوگوی LED
WebSource.kt                 - enum منابع وب
ModernChip.kt                - چیپ مدرن
LanguageDetector.kt          - تشخیص زبان
CategoryTranslator.kt        - ترجمه دسته‌ها
TranslationService.kt        - سرویس ترجمه
PromptViewModel.kt           - مدیریت State
PromptDatabase.kt            - Room DB
PromptEntity.kt              - مدل داده
PromptFilter.kt              - فیلتر
PromptRepository.kt + Impl   - ریپازیتوری
PromptDao.kt                 - DAO
SmartSearchEngine.kt         - جستجو
AppSettings.kt               - تنظیمات (DataStore)
PromptVaultTheme.kt          - تم
HomeUiState.kt               - State
RoleTemplates.kt             - 20 شخصیت
CategoryTranslator.kt        - ترجمه دسته
PromptCatalogImporter.kt     - وارد کردن اولیه


═══════════════════════════════════════════════════════════
📦 فایل‌های Assets (JSON)
═══════════════════════════════════════════════════════════

prompts_catalog_v1.2.0.json         (94)
prompts_persian_v0.1.0.json         (1120)
prompts_life_v0.1.0.json            (80)
prompts_psychology_v0.2.0.json      (110)
prompts_psychology_v0.3.0.json      (127)
prompts_psychology_v0.4.0.json      (120)
prompts_psychology_v0.5.0.json      (123)
prompts_psychology_v0.6.0.json      (45)
prompts_seed_v0.1.0.json            (22)
prompts_openai_claude_v0.1.0.json   (30)
prompts_midjourney_v0.1.0.json      (20)
prompts_lovable_deepseek_v0.1.0.json (22)


═══════════════════════════════════════════════════════════
🎯 لیست انتظار (برای جلسه بعد)
═══════════════════════════════════════════════════════════

🥇 اولویت 1: کش کردن نتایج واکشی (~30 دقیقه)
🥈 اولویت 2: جستجو در Hugging Face (~45 دقیقه)
🥉 اولویت 3: Splash Screen با انیمیشن لوگو (~20 دقیقه)
🎁 اولویت 4: انیمیشن ورود کارت‌ها، Swipe to delete
🔔 اولویت 5: اعلان روزانه، آمار پیشرفته با نمودار
🌐 اولویت 6: ترجمه title پرامپت‌ها به en/ar/tr


═══════════════════════════════════════════════════════════
🏆 دستاورد کل پروژه
═══════════════════════════════════════════════════════════

✅ اپ حرفه‌ای با 4 زبان
✅ ~1913 پرامپت داخلی
✅ ~1680 پرامپت قابل واکشی
✅ ~3600 پرامپت در دسترس کاربر
✅ ترجمه درون‌برنامه‌ای
✅ UI/UX مدرن
✅ 525 زیردسته روانشناسی
✅ 9 منبع واکشی وب

پروژه Hushka در سطح یک اپ حرفه‌ای کامله! 🚀


═══════════════════════════════════════════════════════════
📝 پایان فایل — تهیه شده در مهر ۱۴۰۵
═══════════════════════════════════════════════════════════
