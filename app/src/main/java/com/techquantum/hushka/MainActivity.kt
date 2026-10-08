package com.techquantum.hushka

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import androidx.core.os.LocaleListCompat

class MainActivity : AppCompatActivity() {
    private val viewModel: PromptViewModel by viewModels {
        PromptViewModelFactory((application as PromptVaultApplication).promptRepository)
    }

    private val speechLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            matches?.firstOrNull()?.takeIf { it.isNotBlank() }?.let(viewModel::onQueryChanged)
        }
    }

    private val exportJsonLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportToJson(this, it, viewModel.uiState.value.prompts)
            Toast.makeText(this, "پشتیبان JSON ذخیره شد", Toast.LENGTH_SHORT).show()
        }
    }

    private val exportMarkdownLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportToMarkdown(this, it, viewModel.uiState.value.prompts)
            Toast.makeText(this, "پشتیبان Markdown ذخیره شد", Toast.LENGTH_SHORT).show()
        }
    }

    private val importJsonLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importFromJson(this, it)
            Toast.makeText(this, "در حال درون‌ریزی...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag()
                    ?: AppSettings.getLanguage(this@MainActivity)
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_search_prompt))
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.voice_search_unavailable, Toast.LENGTH_SHORT).show()
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

            PromptVaultTheme(themeMode = themeMode, colorTheme = colorTheme) {
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
                            onColorThemeChange = { newColor ->
                            colorTheme = newColor
                            AppSettings.setColorTheme(this@MainActivity, newColor)
},
                        },
                        onLanguageChange = { newLang ->
                            language = newLang
                            AppSettings.setLanguage(this@MainActivity, newLang)
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
                            Toast.makeText(this@MainActivity, "این ویژگی به‌زودی اضافه می‌شود", Toast.LENGTH_SHORT).show()
                        },
                        onDismiss = { showSettings = false }
                    )
                }
            }
        }
    }
}
