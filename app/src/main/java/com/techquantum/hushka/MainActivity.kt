package com.techquantum.hushka

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.Manifest
import android.net.ConnectivityManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {
    private val viewModel: PromptViewModel by viewModels {
        PromptViewModelFactory((application as HushkaApplication).promptRepository)
    }

    

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

            Toast.makeText(this, "ضبط آنلاین (${engine.displayName}) شروع شد...", Toast.LENGTH_SHORT).show()
            VoiceRecorderHelper.startGroqRecording(
                context = this,
                apiKey = apiKey,
                language = language,
                onResult = { text -> viewModel.onQueryChanged(text) },
                onError = { err -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show() },
                engine = engine
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

        setContent {
            var showSettings by remember { mutableStateOf(false) }
            var themeMode by remember { mutableStateOf(AppSettings.getTheme(this@MainActivity)) }
            var language by remember { mutableStateOf(savedLanguage) }
            var colorTheme by remember { mutableStateOf(AppSettings.getColorTheme(this@MainActivity)) }
            var backPressedTime by remember { mutableLongStateOf(0L) }
            var showTagColorManager by remember { mutableStateOf(false) }
            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                // انیمیشن Splash حدود ۱.۳ ثانیه طول می‌کشد؛ ۱۴۰۰ms کمی فرصت نفس می‌دهد
                // تا انیمیشن کامل دیده شود و بعد صفحه اصلی بیاید.
                delay(1400)
                showSplash = false
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

            HushkaTheme(themeMode = themeMode, colorTheme = colorTheme) {
                if (showSplash) {
                    SplashScreen()
                } else {
                HomeScreen(
                    viewModel = viewModel,
                    onSettingsClick = { showSettings = true },
                    onStartVoiceSearch = ::startVoiceSearch
                )

                if (showSettings) {
                    SettingsDialog(
                        themeMode = themeMode,
                        colorTheme = colorTheme,
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // مدل سنگین Vosk را آزاد می‌کنیم تا رم بعد از بستن اپ نگه داشته نشود.
        VoskOfflineEngine.releaseAll()
    }
}
