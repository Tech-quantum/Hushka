# 🐧 TERMUX_COMMANDS.md — مرجع کامل دستورات (پروژه هوشکا)

**نسخه:** 1.0 · **تاریخ:** 2026-11-12
**مرجع قوانین:** AGENTS.md § ۳ · HUSHKA_MANIFEST § ۵ و § ۶
**مخاطب:** هوش مصنوعی جدیدی که می‌خواهد روی Hushka کار کند

> این فایل مکمل AGENTS.md است. هر دستوری که اینجاست از یک PR واقعی آمده —
> یا دردسر ایجاد کرده، یا آن را حل کرده. هیچ‌چیز اینجا حدس نیست.

## 📑 فهرست
۰. شروع سریع (۵ دقیقهٔ اول)
۱. آماده‌سازی محیط
۲. Git Workflow
۳. ویرایش امن با Python (قانون ۳ + ۵ + ۱۱)
۴. Pre-Check Cookbook
۵. Post-Check Cookbook
۶. Build & Test (قانون ۱۰)
۷. Debug CI (gh CLI)
۸. APK: دانلود، تأیید، نصب
۹. Troubleshooting
۱۰. Cheatsheet: ۱۲ قانون × ۱ دستور
🔗 ارجاعات

---

## ۰. شروع سریع — ۵ دقیقهٔ اول

هر باری که می‌خواهی تغییر بدهی، این چهار دستور را **به همین ترتیب** اجرا کن:

```bash
git status --short              # working tree تمیز؟
git branch --show-current       # هرگز main نباشی (قانون ۱۲)
git switch -c <prefix>/<name> origin/main   # شاخه از origin
python3 check_voice_models.py   # محیط سالم است؟
```

اگر هر مرحله شکست خورد → **متوقف شو**، گزارش بده، ادامه نده.

پیشوندهای شاخه (از PRهای واقعی HISTORY.md):
`feature/*` · `fix/*` · `ui/*` · `docs/*` · `ci/*` · `perf/*`

---

## ۱. آماده‌سازی محیط (یک‌بار در هر کلون)

```bash
# مدل‌های صوتی Vosk (~۲۳۱MB) با Git-LFS
git lfs install
git lfs pull

# سلامت مدل‌ها (بدون LFS، ZIPها pointer متنی‌اند → خطای شفاف)
python3 check_voice_models.py

# gh CLI برای دیباگ CI
pkg install gh
gh auth login
```

⚠️ **هشدار:** کلون کامل ~۳۱۹MB است چون بایت‌های ZIP در تاریخچهٔ گیت هستند (Handoff، «بدهی فنی»). شاخه را از `origin/main` بساز، نه از تاریخچهٔ محلی.

---

## ۲. Git Workflow

### ساخت شاخه (نه از local، از origin)
```bash
git fetch origin
git switch -c feature/<name> origin/main
```
دلیل: شاخه از تاریخچهٔ محلی سنگین می‌شود (Handoff).

### قبل از کامیت — چک‌لیست اجباری
```bash
git branch --show-current      # قانون ۱۲ — هرگز main
git diff --stat                # چه چیزی تغییر کرد؟
git diff -- '*.kt'             # تغییرات کد را ببین
```

### پیام کامیت (Conventional)
```bash
git commit -m "feat(recording): increase online cap 5s → 30s"
git commit -m "fix(silence): handle MAX_TIME edge case"
git commit -m "docs(handoff): sync STATUS with PR #TBD"
git commit -m "ci(release): add lite-apk guard"
```

### پوش
```bash
git push -u origin "$(git branch --show-current)"
```

### ساخت PR با gh CLI
```bash
gh pr create \
  --title "feat: increase recording cap to 30s" \
  --body "## What
افزایش سقف ضبط آنلاین از ۵ به ۳۰ ثانیه.

## How tested
- ./gradlew :app:compileDebugKotlin ✅
- ./gradlew testDebugUnitTest --tests '*SilenceDetector*' ✅

Closes #TBD"
```

---

## ۳. ویرایش امن با Python (قانون ۳ + ۵ + ۱۱)

**هرگز** `cat << EOF`. **هرگز** ویرایش دستی. **همیشه** Python با count check.

### Template استاندارد (کپی-پیست)
```bash
python3 << 'PYEOF'
import sys
from pathlib import Path

path = Path("app/src/main/java/com/techquantum/hushka/File.kt")
text = path.read_text(encoding="utf-8")

old = 'EXACT_ANCHOR_HERE'
new = 'REPLACEMENT_HERE'

count = text.count(old)
if count == 0:
    print("❌ anchor پیدا نشد")
    sys.exit(1)
if count > 1:
    print(f"⚠️ {count} تطابق — بازبینی لازم")
    sys.exit(1)

path.write_text(text.replace(old, new, 1), encoding="utf-8")
print("✅ موفق")
PYEOF
```

### چرا `'PYEOF'` داخل کوتیشن؟
تک‌کوتیشن جلوی shell expansion را می‌گیرد — `$`, backtick, `\` امن می‌مانند.

### اگر anchor چندخطی است
```python
old = """خط اول
خط دوم"""
```
اما **هرگز** regex برای تغییر ساختار (قانون ۵).

### اگر anchor در چند فایل است
```python
for p in Path(".").rglob("*.kt"):
    t = p.read_text(encoding="utf-8")
    if old in t:
        print(f"یافت شد در: {p} ({t.count(old)} بار)")
```
اول **گزارش** بده، سپس تصمیم بگیر.

---

## ۴. Pre-Check Cookbook (پروتکل § ۶، مرحله ۱)

### پیدا کردن تعریف
```bash
grep -RInE "MAX_TIME|MAX_RECORD_MS" --include='*.kt' .
```

### پیدا کردن ایمپورت
```bash
grep -RIn "^import .*SilenceDetector" --include='*.kt' .
```

### نمایش محدودهٔ یک تابع
```bash
sed -n '/fun startRecording/,/^}/p' \
  app/src/main/java/com/techquantum/hushka/VoiceRecorderHelper.kt
```

### شمارش وقوع
```bash
grep -c "MAX_TIME" app/src/main/java/com/techquantum/hushka/SilenceDetector.kt
```

### کشف ساختار Flat
```bash
find app/src/main/java/com/techquantum/hushka -maxdepth 1 -name '*.kt' | sort
find app/src/test -name '*Test.kt' | sort
```

### کاراکترهای مخفی (CRLF، فاصلهٔ اضافه)
```bash
grep -n "anchor" file.kt | cat -A
```

---

## ۵. Post-Check Cookbook (پروتکل § ۶، مرحله ۴)

### مقدار جدید نشست؟
```bash
grep -n "MAX_TIME = 30_000\|MAX_TIME = 30000" \
  app/src/main/java/com/techquantum/hushka/SilenceDetector.kt
```

### مقدار قبلی پاک شد؟
```bash
! grep -n "MAX_TIME = 5_000\|MAX_TIME = 5000" \
  app/src/main/java/com/techquantum/hushka/SilenceDetector.kt && echo "✅ پاک شد"
```

### فقط فایل‌های هدف تغییر کرده؟
```bash
git diff --stat
# فقط فایل‌های مورد انتظار باید در لیست باشند
```

### ساختار فایل دست‌نخورده؟
```bash
git diff -- '*.kt' | head -40
# آکولادها، ایمپورت‌ها، ساختار باید سالم باشند
```

---

## ۶. Build & Test (قانون ۱۰)

### کامپایل سریع (قبل از push اجباری)
```bash
./gradlew :app:compileDebugKotlin
```

### تست واحد
```bash
./gradlew testDebugUnitTest
./gradlew testDebugUnitTest --tests "*SilenceDetector*"
./gradlew testDebugUnitTest --tests "*FabPositionTest*"
```

### APK دیباگ
```bash
./gradlew assembleDebug
```

### اگر PerfettoTrace خطا داد (Termux)
> از Handoff: «در Termux ممکن است PerfettoTrace خطا دهد»

```bash
./gradlew --no-daemon :app:compileDebugKotlin
./gradlew --offline :app:compileDebugKotlin
```
اگر بازهم شکست خورد → **به CI تکیه کن** (قانون ۱۰).

---

## ۷. Debug CI (مانیفست § ۸)

```bash
gh run list --limit 5           # آخرین اجراها
gh run view                     # جزئیات آخرین اجرا
gh run view <run-id> --log      # لاگ کامل
gh run view <run-id> --log-failed  # فقط شکست‌خورده‌ها
gh run view <run-id> --web      # در مرورگر
```

> «لاگ CI را با gh run view بگیر و ریشه‌یابی کن — حدس نزن» (مانیفست § ۸)

---

## ۸. APK: دانلود، تأیید، نصب

### نسخه کامل (قابل ادامه)
```bash
curl -L -C - -o app-debug.apk \
  https://github.com/Tech-quantum/Hushka/releases/download/test-latest/app-debug.apk
curl -L -C - -o app-debug.apk.sha256 \
  https://github.com/Tech-quantum/Hushka/releases/download/test-latest/app-debug.apk.sha256
sha256sum -c app-debug.apk.sha256
```

### نسخه سبک (~۴۰MB، بدون مدل صوتی)
```bash
curl -L -C - -o app-debug-lite.apk \
  https://github.com/Tech-quantum/Hushka/releases/download/test-latest/app-debug-lite.apk
```

### تکه‌تکه (شبکه ضعیف)
```bash
for i in 00 01 02 03 04 05; do
  curl -L -C - -O "https://github.com/.../apk.part-$i"
done
cat apk.part-* > app-debug.apk
sha256sum -c apk-parts.sha256
```

### قبل از نصب
> هر بیلد CI با کلید debug تازه امضا می‌شود (Handoff)

```bash
adb uninstall com.techquantum.hushka
adb install app-debug.apk
```

### تأیید محتوای APK (گاردهای CI)
```bash
unzip -l app-debug-lite.apk | grep -c vosk-model   # باید 0 باشد
ls -la app-debug-lite.apk                          # باید < 100MB
```

---

## ۹. Troubleshooting

| نشانه | علت | راه‌حل |
|---|---|---|
| «anchor پیدا نشد» ولی هست | `\r` یا فاصلهٔ اضافه | `grep -n "..." file \| cat -A` |
| `git push` رد شد | remote جلوتر | `git pull --rebase origin "$(git branch --show-current)"` |
| `VoskOfflineEngine` خطا | LFS ناقص | `git lfs pull && python3 check_voice_models.py` |
| `assembleDebug` شکست | کش خراب | `./gradlew clean && rm -rf app/build` |
| APK سبک ۲۷۴MB است | بسته‌بندی افزایشی AGP | `rm -rf app/build/intermediates/assets` سپس `assembleDebug` |
| `App not installed` | امضای متفاوت | `adb uninstall com.techquantum.hushka` |
| CI قرمز ولی محلی سبز | کش/محیط | `gh run view <id> --log-failed` |

### ⛔ Danger Zone (نیاز به تأیید صریح کاربر)
- `git push --force` (هرگز به `main`)
- `git filter-repo` (بازنویسی تاریخچه — Handoff: «پرریسک و انجام‌نشده»)
- `rm -rf app/build` (بی‌خطر برای APK، اما بیلد کامل لازم می‌شود)
- `git reset --hard` (تغییرات uncommitted را پاک می‌کند)

---

## ۱۰. Cheatsheet: ۱۲ قانون × ۱ دستور

| # | قانون | دستور |
|---|---|---|
| ۰۱ | شاخه | `git switch -c feature/<name> origin/main` |
| ۰۲ | مستندات | آپدیت `HISTORY.md` + `STATUS.txt` + `Handoff Document.txt` |
| ۰۳ | هردوک | `python3 << 'PYEOF'` (نه `cat << EOF`) |
| ۰۴ | Compose | `grep -n "LocalContext.current" <file>` — نباید داخل `LaunchedEffect` باشد |
| ۰۵ | Regex | با Exact Match، نه regex ساختاری |
| ۰۶ | XML | `grep -n "'" res/values*/strings.xml` — آپاستروف خام نداشته باشد |
| ۰۷ | PR | `#TBD` در مستندات |
| ۰۸ | اسکریپت | `python3 << 'PYEOF'` برای هر تغییر |
| ۰۹ | Post-Check | `grep -n "<new>" <file>` بعد از هر تغییر |
| ۱۰ | تست محلی | `./gradlew :app:compileDebugKotlin` |
| ۱۱ | replace | `count = t.count(old)` قبل از `replace` |
| ۱۲ | شاخه قبل کامیت | `git branch --show-current` |

### ✅ همیشه
`git branch --show-current` · count check · grep قبل/بعد · Gradle compile · آپدیت ۳ فایل مرجع

### ⛔ هرگز
`cat << EOF` · regex ساختاری · آپاستروف خام در XML · commit روی main · push --force · ویرایش دستی فایل بزرگ

---

## 🔗 ارجاعات
- `AGENTS.md` § ۳ (۱۲ قانون طلایی) · § ۴ (پروتکل ۵ مرحله‌ای)
- `HUSHKA_MANIFEST_v1.0.txt` § ۵ · § ۶ · § ۸
- `Handoff Document.txt` (قوانین حیاتی + بدهی فنی)
- `STATUS.txt` (وضعیت فنی فعلی)
- `HISTORY.md` (ریشهٔ هر قانون — چرا اینجاست؟)
