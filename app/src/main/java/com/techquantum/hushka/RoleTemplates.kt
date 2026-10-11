package com.techquantum.hushka

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

data class RoleTemplate(
    val id: String,
    val emoji: String,
    val nameFa: String,
    val nameEn: String,
    val promptPrefix: String
)

object RoleTemplates {
    val all = listOf(
        RoleTemplate("developer", "👨‍💻", "برنامه‌نویس ارشد", "Senior Developer",
            "You are a senior software developer with 10+ years of experience. Provide clean, efficient, well-documented solutions."),
        RoleTemplate("writer", "✍️", "نویسنده حرفه‌ای", "Professional Writer",
            "You are a professional writer with expertise in engaging storytelling. Write clearly, creatively, and with proper structure."),
        RoleTemplate("designer", "🎨", "طراح UI/UX", "UI/UX Designer",
            "You are a senior UI/UX designer. Focus on user experience, accessibility, and modern design principles."),
        RoleTemplate("analyst", "📊", "تحلیلگر داده", "Data Analyst",
            "You are a data analyst. Analyze data thoroughly, provide insights, and support conclusions with evidence."),
        RoleTemplate("teacher", "👨‍🏫", "معلم", "Teacher",
            "You are an experienced teacher. Explain concepts step-by-step with examples, and check understanding."),
        RoleTemplate("doctor", "🩺", "پزشک", "Doctor",
            "You are a medical doctor. Provide evidence-based information. Always recommend consulting a real physician for diagnosis."),
        RoleTemplate("lawyer", "⚖️", "وکیل", "Lawyer",
            "You are a corporate lawyer. Provide legal reasoning, cite principles, and note this is not legal advice."),
        RoleTemplate("translator", "🌐", "مترجم حرفه‌ای", "Professional Translator",
            "You are a professional translator. Translate accurately, preserving tone, idioms, and cultural nuances."),
        RoleTemplate("marketer", "📣", "متخصص بازاریابی", "Marketing Expert",
            "You are a marketing strategist. Focus on target audience, value proposition, and measurable outcomes."),
        RoleTemplate("seo", "🔍", "متخصص SEO", "SEO Expert",
            "You are an SEO expert. Optimize for search engines while maintaining readability and user value."),
        RoleTemplate("editor", "📝", "ویراستار", "Editor",
            "You are a professional editor. Improve clarity, grammar, structure, and flow while preserving the author's voice."),
        RoleTemplate("chef", "🍳", "سرآشپز", "Chef",
            "You are a professional chef. Share recipes with precise measurements, techniques, and pro tips."),
        RoleTemplate("coach", "💪", "مربی", "Coach",
            "You are a personal coach. Motivate, give actionable advice, and help build sustainable habits."),
        RoleTemplate("financial", "💰", "مشاور مالی", "Financial Advisor",
            "You are a financial advisor. Provide balanced analysis considering risk, return, and goals. Not financial advice."),
        RoleTemplate("researcher", "🔬", "پژوهشگر", "Researcher",
            "You are a researcher. Approach questions scientifically, cite sources, and acknowledge uncertainties."),
        RoleTemplate("architect", "🏗", "معمار نرم‌افزار", "Software Architect",
            "You are a software architect. Design scalable, maintainable systems. Discuss trade-offs clearly."),
        RoleTemplate("devops", "⚙️", "متخصص DevOps", "DevOps Engineer",
            "You are a DevOps engineer. Focus on automation, CI/CD, reliability, and infrastructure as code."),
        RoleTemplate("security", "🔐", "متخصص امنیت", "Security Expert",
            "You are a cybersecurity expert. Identify vulnerabilities, recommend secure practices, and explain risks."),
        RoleTemplate("product", "📱", "مدیر محصول", "Product Manager",
            "You are a product manager. Focus on user needs, business value, and prioritization frameworks."),
        RoleTemplate("custom", "✏️", "سفارشی", "Custom", "")
    )

    fun findById(id: String): RoleTemplate? = all.find { it.id == id }

    fun suggestForCategory(category: String): String {
        return when {
            category.contains("coding", true) || category.contains("programming", true) -> "developer"
            category.contains("writing", true) || category.contains("content", true) -> "writer"
            category.contains("design", true) || category.contains("image", true) -> "designer"
            category.contains("data", true) || category.contains("analysis", true) -> "analyst"
            category.contains("education", true) -> "teacher"
            category.contains("health", true) -> "doctor"
            category.contains("legal", true) -> "lawyer"
            category.contains("translation", true) -> "translator"
            category.contains("marketing", true) -> "marketer"
            category.contains("seo", true) -> "seo"
            category.contains("finance", true) -> "financial"
            category.contains("security", true) -> "security"
            category.contains("devops", true) -> "devops"
            else -> "custom"
        }
    }
}

private val roleNamesAr = mapOf(
    "developer" to "مطوّر برمجيات خبير", "writer" to "كاتب محترف", "designer" to "مصمم UI/UX",
    "analyst" to "محلل بيانات", "teacher" to "معلم", "doctor" to "طبيب", "lawyer" to "محامٍ",
    "translator" to "مترجم محترف", "marketer" to "خبير تسويق", "seo" to "خبير SEO",
    "editor" to "محرر", "chef" to "طاهٍ", "coach" to "مدرب", "financial" to "مستشار مالي",
    "researcher" to "باحث", "architect" to "مهندس معماري للبرمجيات", "devops" to "خبير DevOps",
    "security" to "خبير أمن", "product" to "مدير منتج", "custom" to "مخصص"
)

private val roleNamesTr = mapOf(
    "developer" to "Kıdemli Yazılımcı", "writer" to "Profesyonel Yazar", "designer" to "UI/UX Tasarımcısı",
    "analyst" to "Veri Analisti", "teacher" to "Öğretmen", "doctor" to "Doktor", "lawyer" to "Avukat",
    "translator" to "Profesyonel Çevirmen", "marketer" to "Pazarlama Uzmanı", "seo" to "SEO Uzmanı",
    "editor" to "Editör", "chef" to "Şef", "coach" to "Koç", "financial" to "Mali Danışman",
    "researcher" to "Araştırmacı", "architect" to "Yazılım Mimarı", "devops" to "DevOps Uzmanı",
    "security" to "Güvenlik Uzmanı", "product" to "Ürün Yöneticisi", "custom" to "Özel"
)

/** نام نقش بر اساس زبان فعلی برنامه */
@Composable
fun RoleTemplate.displayName(): String {
    return when (LocalConfiguration.current.locales[0].language) {
        "fa" -> nameFa
        "ar" -> roleNamesAr[id] ?: nameEn
        "tr" -> roleNamesTr[id] ?: nameEn
        else -> nameEn
    }
}
