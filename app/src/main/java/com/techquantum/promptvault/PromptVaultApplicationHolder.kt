package com.techquantum.promptvault

import android.content.Context

object PromptVaultApplicationHolder {
    lateinit var context: Context
        private set

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }
}
