package com.techquantum.hushka

import android.app.Application

class HushkaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        HushkaApplicationHolder.initialize(this)
        RoleSuggester.init(this)
    }

    val promptRepository: PromptRepository by lazy {
        PromptRepositoryImpl(
            PromptDatabase.getInstance(this).promptDao()
        )
    }
}
