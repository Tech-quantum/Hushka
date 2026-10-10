package com.techquantum.hushka

import android.content.Context

object HushkaApplicationHolder {
    lateinit var context: Context
        private set

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }
}
