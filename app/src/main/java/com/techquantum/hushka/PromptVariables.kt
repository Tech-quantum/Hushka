package com.techquantum.hushka

/**
 * تشخیص متغیرهای داخل پرامپت.
 *
 * تنها منبع حقیقت برای استخراج متغیر است؛ هم دیالوگ تکمیل متغیرها و هم
 * موتور امتیازدهی از همین‌جا استفاده می‌کنند تا در همه‌جا یکسان شمرده شود.
 *
 * پشتیبانی از هر دو شکل `[VARIABLE]` و `{VARIABLE}` و هر زبانی
 * (فارسی/عربی/ترکی/انگلیسی). نسخه‌ی قبلی فقط `[A-Z_]+` را می‌شناخت و
 * بنابراین متغیرهای فارسی مثل `[محصول]` را از دست می‌داد.
 */
object PromptVariables {

    private val BRACKET = Regex("\\[([^\\[\\]]+)\\]")
    private val CURLY = Regex("\\{([^{}]+)\\}")

    /** نام متغیرها بدون تکرار، به ترتیب ظهور در متن. */
    fun extract(text: String): List<String> {
        val seen = LinkedHashSet<String>()
        BRACKET.findAll(text).forEach { seen += it.groupValues[1].trim() }
        CURLY.findAll(text).forEach { seen += it.groupValues[1].trim() }
        seen.remove("")
        return seen.toList()
    }

    /** تعداد کل متغیرهای یکتا در متن. */
    fun count(text: String): Int = extract(text).size
}
