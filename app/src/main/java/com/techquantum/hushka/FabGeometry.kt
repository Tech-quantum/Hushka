package com.techquantum.hushka

/**
 * منطق خالص موقعیت‌دهی FAB تنظیمات.
 *
 * این فایل عمداً هیچ وابستگی‌ای به Compose یا Android ندارد تا بتوان
 * بدون شبیه‌ساز و در JVM تستش کرد (`FabPositionTest`).
 */

/**
 * موقعیت را داخل ناحیه‌ی مجاز نگه می‌دارد.
 *
 * کفِ مجاز ([maxY]) شامل ارتفاع نوار پایین است تا دکمه هرگز روی
 * ناوبری نیفتد و آیتم‌های فوتر را بلاک نکند.
 */
fun clampFabPosition(
    x: Float,
    y: Float,
    maxX: Float,
    minY: Float,
    maxY: Float,
): Pair<Float, Float> {
    val safeMinY = minY.coerceAtLeast(0f)
    val safeMaxX = maxX.coerceAtLeast(0f)
    val safeMaxY = maxY.coerceAtLeast(safeMinY)
    return x.coerceIn(0f, safeMaxX) to y.coerceIn(safeMinY, safeMaxY)
}

/**
 * موقعیت اولیه از کسر ذخیره‌شده.
 *
 * اگر کاربر هنوز دکمه را جابه‌جا نکرده باشد (`null`)، پیش‌فرض
 * «راست-پایین» است؛ یعنی گوشه‌ای که با شست راست راحت‌تر در دسترس است.
 */
fun initialFabPosition(
    fraction: Pair<Float, Float>?,
    maxX: Float,
    minY: Float,
    maxY: Float,
): Pair<Float, Float> {
    val fx = (fraction?.first ?: 1f).coerceIn(0f, 1f)
    val fy = (fraction?.second ?: 1f).coerceIn(0f, 1f)
    val safeMaxX = maxX.coerceAtLeast(0f)
    val safeMinY = minY.coerceAtLeast(0f)
    val safeMaxY = maxY.coerceAtLeast(safeMinY)
    return fx * safeMaxX to safeMinY + fy * (safeMaxY - safeMinY)
}

/**
 * تبدیل موقعیت پیکسلی به کسر ۰..۱.
 *
 * کسری ذخیره می‌شود (نه پیکسل) تا بعد از چرخش صفحه، تغییر رزولوشن
 * یا اجرا روی تبلت، دکمه بیرون از صفحه نیفتد.
 */
fun fabFraction(value: Float, range: Float): Float =
    if (range <= 0f) 0f else (value / range).coerceIn(0f, 1f)
