package com.techquantum.promptvault

import android.app.Application

class PromptVaultApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PromptVaultApplicationHolder.initialize(this)
    }

    val promptRepository: PromptRepository by lazy {
        PromptRepositoryImpl(
            PromptDatabase.getInstance(this).promptDao()
        )
    }
}
