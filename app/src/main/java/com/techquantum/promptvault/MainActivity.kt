package com.techquantum.promptvault

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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

    // RecognizerIntent runs in the system speech-recognition app, which holds the
    // microphone permission itself, so this app does not need RECORD_AUDIO.
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
        setContent { PromptVaultApp(viewModel, ::startVoiceSearch) }
    }
}
