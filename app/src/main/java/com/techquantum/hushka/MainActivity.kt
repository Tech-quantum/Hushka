package com.techquantum.hushka
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.CompositionLocalProvider

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.Manifest
import android.net.ConnectivityManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

// بعد از این مدت حضور در پس‌زمینه، اپ هنگام برگشت دوباره قفل می‌شود
private const val BACKGROUND_LOCK_TIMEOUT_MS = 30_000L

class MainActivity : AppCompatActivity() {
    private val viewModel: PromptViewModel by viewModels {
        PromptViewModelFactory((application as HushkaApplication).promptRepository)
    }

    // === 📤 قابلیت Share Sheet (متغیر سطح کلاس) ===
    private val sharedTextState = mutableStateOf<String?>(null)

    

    private val exportJsonLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportToJson(this, it, viewModel.uiState.value.prompts)
            Toast.makeText(this, getString(R.string.toast_json_saved), Toast.LENGTH_SHORT).show()
        }
    }

    private val exportMarkdownLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportToMarkdown(this, it, viewModel.uiState.value.prompts)
            Toast.makeText(this, getString(R.string.toast_md_saved), Toast.LENGTH_SHORT).show()
        }
    }

    private val importJsonLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importFromJson(this, it)
            Toast.makeText(this, getString(R.string.toast_importing), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startVoiceSearch() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 101)
            return
        }

        val cm = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val isOnline = cm.activeNetwork != null

        val language = AppSettings.getLanguage(this)

        if (isOnline) {
            // موتور تشخیص گفتار آنلاین: اول Groq، بعد Gemini.
            // کلید اختصاصی گفتار مقدم است؛ اگر خالی بود از کلید هوش مصنوعی استفاده می‌کنیم.
            val speechKey = AppSettings.getSpeechApiKey(this)
            val aiKey = AppSettings.getAiApiKey(this)
            val provider = AppSettings.getAiProvider(this)

            val engine: OnlineSpeechEngine
            val apiKey: String
            if (speechKey.isNotBlank()) {
                // کاربر کلید گفتار را جدا وارد کرده؛ نوعش را از provider حدس می‌زنیم.
                engine = if (provider == "gemini") OnlineSpeechEngine.GEMINI else OnlineSpeechEngine.GROQ
                apiKey = speechKey
            } else if (aiKey.isNotBlank()) {
                // بدون کلید اختصاصی، از کلید هوش مصنوعی فعلی استفاده کن.
                engine = OnlineSpeechEngine.fromId(provider)
                apiKey = aiKey
            } else {
                Toast.makeText(this, "برای جستجوی صوتی آنلاین کلید API را در تنظیمات وارد کن", Toast.LENGTH_SHORT).show()
                return
            }

            // فالبک خودکار: اگر موتور اول Groq باشد، در صورت فشار یا خطا، بدون ضبط مجدد با Gemini امتحان کن
            var fallbackEngine: OnlineSpeechEngine? = null
            var fallbackApiKey: String? = null
            if (engine == OnlineSpeechEngine.GROQ) {
                if (speechKey.isNotBlank() && provider == "gemini") {
                    fallbackEngine = OnlineSpeechEngine.GEMINI
                    fallbackApiKey = speechKey
                } else if (aiKey.isNotBlank() && provider == "gemini") {
                    fallbackEngine = OnlineSpeechEngine.GEMINI
                    fallbackApiKey = aiKey
                } else if (speechKey.isNotBlank() && provider != "gemini") {
                    // کاربر ممکن است کلید Gemini را در فیلد کلید تشخیص گفتار وارد کرده باشد
                    fallbackEngine = OnlineSpeechEngine.GEMINI
                    fallbackApiKey = speechKey
                }
            }

            Toast.makeText(this, "ضبط آنلاین (${engine.displayName}) شروع شد...", Toast.LENGTH_SHORT).show()
            VoiceRecorderHelper.startGroqRecording(
                context = this,
                apiKey = apiKey,
                language = language,
                onResult = { text -> viewModel.onQueryChanged(text) },
                onError = { err -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show() },
                engine = engine,
                fallbackEngine = fallbackEngine,
                fallbackApiKey = fallbackApiKey
            )
        } else {
            Toast.makeText(this, "ضبط آفلاین (Vosk) شروع شد... (۵ ثانیه)", Toast.LENGTH_SHORT).show()
            VoiceRecorderHelper.startVoskRecording(
                context = this,
                language = language,
                onResult = { text -> viewModel.onQueryChanged(text) },
                onError = { err -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show() }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savedLanguage = AppSettings.getLanguage(this)
        val currentLanguage = AppCompatDelegate.getApplicationLocales().get(0)?.language
        if (currentLanguage.isNullOrBlank() || currentLanguage != savedLanguage) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedLanguage))
        }

        // === 📤 بررسی اینکه آیا اپ از طریق Share باز شده است ===
        handleIntent(intent)

        setContent {
            var showSettings by remember { mutableStateOf(false) }
            var themeMode by remember { mutableStateOf(AppSettings.getTheme(this@MainActivity)) }
            var fontScale by remember { mutableStateOf(AppSettings.getFontScale(this@MainActivity)) }
            var language by remember { mutableStateOf(savedLanguage) }
            var colorTheme by remember { mutableStateOf(AppSettings.getColorTheme(this@MainActivity)) }
            var backPressedTime by remember { mutableLongStateOf(0L) }
            var showTagColorManager by remember { mutableStateOf(false) }
            var showSplash by remember { mutableStateOf(true) }
            // با هر بار «بازنشانی موقعیت» یک واحد اضافه می‌شود تا FAB به گوشه‌ی پیش‌فرض برگردد
            var fabResetToken by remember { mutableIntStateOf(0) }

            // === 🔒 قفل بیومتریک ===
            var biometricLock by remember { mutableStateOf(AppSettings.isBiometricLockEnabled(this@MainActivity)) }
            var locked by remember { mutableStateOf(AppSettings.isBiometricLockEnabled(this@MainActivity)) }
            var lockError by remember { mutableStateOf(false) }
            var backgroundedAt by remember { mutableLongStateOf(0L) }
            val biometricAvailable = remember { BiometricGate.isAvailable(this@MainActivity) }

            // === 📤 خواندن استیت متنِ Share شده ===
            val sharedText by sharedTextState

            LaunchedEffect(Unit) {
                // انیمیشن Splash حدود ۱.۳ ثانیه طول می‌کشد؛ ۱۴۰۰ms کمی فرصت نفس می‌دهد
                // تا انیمیشن کامل دیده شود و بعد صفحه اصلی بیاید.
                delay(1400)
                showSplash = false
            }

            // اگر اپ بیش از ۳۰ ثانیه در پس‌زمینه بماند، هنگام برگشت دوباره قفل می‌شود.
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner, biometricLock) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_STOP -> backgroundedAt = SystemClock.elapsedRealtime()
                        Lifecycle.Event.ON_START -> {
                            if (biometricLock && backgroundedAt > 0L &&
                                SystemClock.elapsedRealtime() - backgroundedAt >= BACKGROUND_LOCK_TIMEOUT_MS
                            ) {
                                lockError = false
                                locked = true
                            }
                        }
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            // بعد از اسپلش، اگر اپ قفل است یک‌بار خودکار اثر انگشت می‌خواهیم.
            LaunchedEffect(showSplash, locked) {
                if (!showSplash && locked) {
                    BiometricGate.prompt(
                        activity = this@MainActivity,
                        onSuccess = { locked = false; lockError = false },
                        onFailure = { lockError = true }
                    )
                }
            }

            // === 🚪 دو بار بک برای خروج ===
            BackHandler {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime < 2000) {
                    // بار دوم: خروج
                    finish()
                } else {
                    // بار اول: پیغام صمیمی
                    backPressedTime = currentTime
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.press_back_again),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
            HushkaTheme(themeMode = themeMode, colorTheme = colorTheme) {
                if (showSplash) {
                    SplashScreen()
                } else if (locked) {
                    BiometricLockScreen(
                        showError = lockError,
                        onUnlock = {
                            BiometricGate.prompt(
                                activity = this@MainActivity,
                                onSuccess = { locked = false; lockError = false },
                                onFailure = { lockError = true }
                            )
                        }
                    )
                } else {
                // === 🎨 افکت بلور (Frosted Glass) وقتی تنظیمات باز است ===
                Box(
                    modifier = Modifier.fillMaxSize().then(
                        if (showSettings) Modifier.blur(16.dp) else Modifier
                    )
                ) {
                    HomeScreen(
                        viewModel = viewModel,
                        onSettingsClick = { showSettings = true },
                        onStartVoiceSearch = ::startVoiceSearch,
                        fabResetToken = fabResetToken,
                        // === 📤 پاس دادن متن به صفحه اصلی ===
                        sharedText = sharedText,
                        onSharedTextConsumed = { sharedTextState.value = null }
                    )
                }

                if (showSettings) {
                    SettingsDialog(
                        themeMode = themeMode,
                        colorTheme = colorTheme,
                        fontScale = fontScale,
                        onFontScaleChange = { newScale -> fontScale = newScale; AppSettings.setFontScale(this@MainActivity, newScale) },
                        language = language,
                        onThemeChange = { newMode ->
                            themeMode = newMode
                            AppSettings.setTheme(this@MainActivity, newMode)
                        },
                        onColorThemeChange = { newColor ->
                            colorTheme = newColor
                            AppSettings.setColorTheme(this@MainActivity, newColor)
                        },
                        onLanguageChange = { newLang ->
                            language = newLang
                            AppSettings.setLanguage(this@MainActivity, newLang)
                            // مدل Vosk مختص زبان است؛ با تغییر زبان آزادش می‌کنیم
                            // تا فضای رم نگه داشته نشود.
                            VoskOfflineEngine.releaseAll()
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(newLang)
                            )
                        },
                        onExportJson = {
                            exportJsonLauncher.launch("hushka_backup_${System.currentTimeMillis()}.json")
                        },
                        onExportMarkdown = {
                            exportMarkdownLauncher.launch("hushka_backup_${System.currentTimeMillis()}.md")
                        },
                        onImportJson = {
                            importJsonLauncher.launch(arrayOf("application/json"))
                        },
                        onBrowseWeb = {
                            showSettings = false
                            viewModel.fetchWebPrompts()
                        },
                        onManageColors = {
                            showSettings = false
                            showTagColorManager = true
                        },
                        onClearWebCache = {
                            viewModel.clearWebCache()
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.web_cache_cleared_toast),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onResetFabPosition = {
                            AppSettings.clearFabPosition(this@MainActivity)
                            fabResetToken++
                        },
                        biometricLockEnabled = biometricLock,
                        biometricAvailable = biometricAvailable,
                        onBiometricLockChange = { wanted ->
                            if (!wanted) {
                                AppSettings.setBiometricLockEnabled(this@MainActivity, false)
                                biometricLock = false
                            } else {
                                // فعال‌سازی فقط بعد از یک تأیید موفق انجام می‌شود
                                // تا کاربر خودش را بیرون قفل نکند.
                                BiometricGate.prompt(
                                    activity = this@MainActivity,
                                    onSuccess = {
                                        AppSettings.setBiometricLockEnabled(this@MainActivity, true)
                                        biometricLock = true
                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(R.string.lock_enabled_toast),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onFailure = {
                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(R.string.lock_failed_toast),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        },
                        onDismiss = { showSettings = false }
                    )
                }

                if (showTagColorManager) {
                    val allTags = viewModel.uiState.value.prompts
                        .flatMap { it.tags.split(",").map { t -> t.trim() } }
                        .filter { it.isNotEmpty() }
                        .distinct()
                    TagColorManagerDialog(
                        allTags = allTags,
                        onDismiss = { showTagColorManager = false }
                    )
                }
                }  // close else (showSplash)
            }
    } // close CompositionLocalProvider
        }
    }

    // === 📤 توابع مربوط به Share Sheet ===
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                sharedTextState.value = text
            }
        }
    }
    // ==========================================

    override fun onDestroy() {
        super.onDestroy()
        // مدل سنگین Vosk را آزاد می‌کنیم تا رم بعد از بستن اپ نگه داشته نشود.
        VoskOfflineEngine.releaseAll()
    }
}
