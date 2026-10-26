package com.techquantum.hushka

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * لایه‌ی نازک روی `androidx.biometric`؛ همه‌ی جزئیات نسخه‌های اندروید همین‌جا متمرکز است.
 *
 * روی اندروید ۱۱+ قفل دستگاه (PIN/الگو) هم پذیرفته می‌شود تا اگر حسگر اثر انگشت
 * روزی از کار بیفتد کاربر پشت قفل گیر نکند. روی نسخه‌های قدیمی‌تر
 * `BiometricPrompt` ترکیب «بیومتریک + قفل دستگاه» را پشتیبانی نمی‌کند،
 * پس فقط بیومتریک پیشنهاد می‌شود.
 */
object BiometricGate {

    private fun authenticators(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        }

    /** آیا این دستگاه می‌تواند قفل بیومتریک (یا روی ۱۱+ قفل دستگاه) را تأیید کند؟ */
    fun isAvailable(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(authenticators()) ==
            BiometricManager.BIOMETRIC_SUCCESS

    /**
     * دیالوگ سیستم را باز می‌کند. هر دو کال‌بک روی ترد اصلی اجرا می‌شوند.
     * انصراف کاربر هم `onFailure` حساب می‌شود؛ تصمیم «قفل بماند یا نه» با صداکننده است.
     */
    fun prompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    ) {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onFailure()
            }
        }

        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.lock_title))
            .setSubtitle(activity.getString(R.string.lock_subtitle))
            .setAllowedAuthenticators(authenticators())

        // با روش «قفل دستگاه» دکمه‌ی منفی مجاز نیست؛ سیستم خودش گزینه‌ی PIN را می‌گذارد.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            builder.setNegativeButtonText(activity.getString(R.string.lock_cancel))
        }

        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
            .authenticate(builder.build())
    }
}
