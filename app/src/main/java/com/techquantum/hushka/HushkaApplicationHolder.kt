package com.techquantum.hushka

import android.content.Context

object HushkaApplicationHolder {
    private var _context: Context? = null

    val context: Context
        get() = _context
            ?: throw IllegalStateException("HushkaApplicationHolder not initialized")

    fun isInitialized(): Boolean = _context != null

    fun initialize(context: Context) {
        this._context = context.applicationContext
    }
}
