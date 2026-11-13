package com.techquantum.hushka

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureSettings {
    private const val PREFS_NAME = "hushka_secure_prefs"
    @Volatile
    private var sharedPreferences: SharedPreferences? = null

    @Synchronized
    fun init(context: Context) {
        if (sharedPreferences == null) {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            sharedPreferences = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences?.getString(key, defaultValue) ?: defaultValue
    }

    fun putString(key: String, value: String) {
        sharedPreferences?.edit()?.putString(key, value)?.apply()
    }
    
    fun remove(key: String) {
        sharedPreferences?.edit()?.remove(key)?.apply()
    }

    // Synchronous variants: call only from Dispatchers.IO.
    fun putStringSync(key: String, value: String): Boolean {
        return sharedPreferences?.edit()?.putString(key, value)?.commit() ?: false
    }

    fun removeSync(key: String): Boolean {
        return sharedPreferences?.edit()?.remove(key)?.commit() ?: false
    }
}
