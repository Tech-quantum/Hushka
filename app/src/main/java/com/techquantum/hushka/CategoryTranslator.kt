package com.techquantum.hushka

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource

@Composable
fun getLocalizedCategory(category: String): String {
    if (category.isBlank()) return ""

    when {
        category.equals("web", true) -> return stringResource(R.string.cat_web)
        category.equals("github", true) -> return stringResource(R.string.cat_github)
        category.equals("image", true) -> return stringResource(R.string.cat_image)
        category.equals("video", true) -> return stringResource(R.string.cat_video)
        category.equals("audio", true) -> return stringResource(R.string.cat_audio)
    }

    val parts = category.split(".", limit = 2)
    if (parts.size < 2) return category

    val domain = parts[0].lowercase()
    val sub = parts[1].lowercase()

    val domainName = getDomainName(domain)

    if (!isPersian()) {
        return "$domainName • ${toTitleCase(sub)}"
    }

    return "$domainName • ${getSubcategoryName(sub)}"
}

@Composable
fun getDomainName(domain: String): String {
    if (isPersian()) {
        return when (domain) {
            "agent" -> stringResource(R.string.domain_agent)
            "analysis" -> stringResource(R.string.domain_analysis)
            "prompting" -> stringResource(R.string.domain_prompting)
            "text" -> stringResource(R.string.domain_text)
            "coding" -> stringResource(R.string.domain_coding)
            "image" -> stringResource(R.string.domain_image)
            "video" -> stringResource(R.string.domain_video)
            "marketing" -> "بازاریابی"
            "other" -> "سایر"
            "ai" -> "هوش مصنوعی"
            "psychology" -> "Ψ روانشناسی"
            "life" -> "زندگی"
            else -> toTitleCase(domain)
        }
    }
    return if (domain == "psychology") "Ψ " + toTitleCase(domain) else toTitleCase(domain)
}

private fun toTitleCase(slug: String): String {
    if (slug.isBlank()) return slug
    return slug
        .split("-")
        .joinToString(" ") { word ->
            if (word.isBlank()) word
            else word.replaceFirstChar { it.uppercase() }
        }
}

@Composable
private fun isPersian(): Boolean {
    val config = LocalConfiguration.current
    return config.locales[0].language == "fa"
}

@Composable
private fun getSubcategoryName(sub: String): String {
    return when (sub) {
        "agentic-workflow" -> "گردش‌کار عاملی"
        "tool-use" -> "استفاده از ابزار"
        "analysis" -> "تحلیل عمومی"
        "classification" -> "طبقه‌بندی"
        "evaluation" -> "ارزیابی"
        "reasoning" -> "استدلال"
        "research" -> "تحقیق"
        "self-check" -> "خودبررسی"
        "authentication" -> "احراز هویت"
        "backend" -> "بک‌اند"
        "code-explanation" -> "توضیح کد"
        "code-generation" -> "تولید کد"
        "code-rewrite" -> "بازنویسی کد"
        "coding" -> "کدنویسی عمومی"
        "debugging" -> "اشکال‌زدایی"
        "integration" -> "یکپارچه‌سازی"
        "new-project" -> "پروژه جدید"
        "refactoring" -> "بازآرایی"
        "testing" -> "تست"
        "ui" -> "رابط کاربری"
        "version-control" -> "کنترل نسخه"
        "art-style" -> "سبک هنری"
        "aspect-ratio" -> "نسبت ابعاد"
        "background" -> "پس‌زمینه"
        "canvas-extension" -> "گسترش بوم"
        "character-consistency" -> "یکنواختی شخصیت"
        "cinematic" -> "سینمایی"
        "color" -> "رنگ"
        "composition" -> "ترکیب‌بندی"
        "editing" -> "ویرایش"
        "environment" -> "محیط"
        "identity-preservation" -> "حفظ هویت"
        "image-editing" -> "ویرایش تصویر"
        "image-generation" -> "تولید تصویر"
        "image-reference" -> "مرجع تصویر"
        "infographic" -> "اینفوگرافیک"
        "localization" -> "بومی‌سازی"
        "multi-image" -> "چند تصویری"
        "outfit" -> "لباس"
        "outpainting" -> "گسترش تصویر"
        "photography" -> "عکاسی"
        "portrait" -> "پرتره"
        "product" -> "محصول"
        "product-normalization" -> "نرمال‌سازی محصول"
        "prompt-discovery" -> "کشف پرامپت"
        "restoration" -> "ترمیم"
        "storyboard" -> "استوری‌بورد"
        "style-reference" -> "مرجع سبک"
        "style-transfer" -> "انتقال سبک"
        "targeted-edit" -> "ویرایش هدفمند"
        "text-in-image" -> "متن در تصویر"
        "time-period" -> "دوره زمانی"
        "typography" -> "تایپوگرافی"
        "copywriting" -> "کپی‌رایتینگ"
        "seo" -> "سئو"
        "social-media" -> "شبکه‌های اجتماعی"
        "prompt-engineering" -> "مهندسی پرامپت"
        "anti-hallucination" -> "ضد توهم"
        "context" -> "زمینه"
        "delimiter" -> "جداکننده"
        "few-shot" -> "چند نمونه‌ای"
        "instruction" -> "دستور"
        "iteration" -> "تکرار"
        "long-context" -> "زمینه بلند"
        "minimal-prompt" -> "پرامپت مینیمال"
        "role" -> "نقش"
        "scope-control" -> "کنترل دامنه"
        "specificity" -> "دقت"
        "xml-structure" -> "ساختار XML"
        "completion" -> "تکمیل"
        "document-creation" -> "ایجاد سند"
        "extraction" -> "استخراج"
        "formatting" -> "قالب‌بندی"
        "quote-extraction" -> "استخراج نقل‌قول"
        "roleplay" -> "نقش‌آفرینی"
        "routine" -> "روتین"
        "scenario" -> "سناریو"
        "structured-output" -> "خروجی ساختاریافته"
        "summarization" -> "خلاصه‌سازی"
        "translation" -> "ترجمه"
        "writing" -> "نوشتن"
        "improve" -> "بهبود"

        // psychology
        "emotions" -> "احساسات و هیجانات"
        "breathing" -> "تنفس و آرام‌سازی"
        "cbt" -> "درمان شناختی-رفتاری (CBT)"
        "grounding" -> "بازگشت به لحظه‌ی حال"
        "journaling" -> "ژورنالینگ"
        "communication" -> "ارتباط مؤثر"
        "stress" -> "مدیریت استرس"
        "gratitude" -> "قدردانی و شکرگزاری"
        "exposure" -> "مواجهه با ترس"
        "boundaries" -> "مرزگذاری سالم"
        "relationships" -> "روابط و زوجین"
        "teen" -> "نوجوان"
        "grief" -> "سوگ و فقدان"
        "self-esteem" -> "اعتماد به نفس"
        "procrastination" -> "غلبه بر اهمال‌کاری"

        // life
        "food" -> "غذا و تغذیه"
        "money" -> "مالی و بودجه"
        "travel" -> "سفر"
        "health" -> "سلامت و تناسب اندام"
        "family" -> "خانواده"
        "habits" -> "عادت‌ها"
        "home" -> "خانه و منزل"

        else -> toTitleCase(sub)
    }
}
