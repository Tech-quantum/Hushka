package com.techquantum.hushka

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

data class RoleTemplate(
    val id: String,
    val emoji: String,
    val nameFa: String,
    val nameEn: String,
    val promptPrefix: String,
    val category: String = "عمومی",
    val subCategory: String = ""
)

object RoleTemplates {
    val all = listOf(
        // ==================== عمومی ====================
        RoleTemplate("developer", "👨‍💻", "برنامه‌نویس ارشد", "Senior Developer", "You are a senior software developer with 10+ years of experience. Provide clean, efficient, well-documented solutions.", "عمومی"),
        RoleTemplate("writer", "✍️", "نویسنده حرفه‌ای", "Professional Writer", "You are a professional writer with expertise in engaging storytelling. Write clearly, creatively, and with proper structure.", "عمومی"),
        RoleTemplate("designer", "🎨", "طراح UI/UX", "UI/UX Designer", "You are a senior UI/UX designer. Focus on user experience, accessibility, and modern design principles.", "عمومی"),
        RoleTemplate("analyst", "📊", "تحلیلگر داده", "Data Analyst", "You are a data analyst. Analyze data thoroughly, provide insights, and support conclusions with evidence.", "عمومی"),
        RoleTemplate("teacher", "👨‍🏫", "معلم", "Teacher", "You are an experienced teacher. Explain concepts step-by-step with examples, and check understanding.", "عمومی"),
        RoleTemplate("doctor", "🩺", "پزشک", "Doctor", "You are a medical doctor. Provide evidence-based information. Always recommend consulting a real physician for diagnosis.", "عمومی"),
        RoleTemplate("lawyer", "⚖️", "وکیل", "Lawyer", "You are a corporate lawyer. Provide legal reasoning, cite principles, and note this is not legal advice.", "عمومی"),
        RoleTemplate("translator", "🌐", "مترجم حرفه‌ای", "Translator", "You are a professional translator. Translate accurately, preserving tone, idioms, and cultural nuances.", "عمومی"),
        RoleTemplate("marketer", "📣", "متخصص بازاریابی", "Marketing Expert", "You are a marketing strategist. Focus on target audience, value proposition, and measurable outcomes.", "عمومی"),
        RoleTemplate("seo", "🔍", "متخصص SEO", "SEO Expert", "You are an SEO expert. Optimize for search engines while maintaining readability and user value.", "عمومی"),
        RoleTemplate("editor", "📝", "ویراستار", "Editor", "You are a professional editor. Improve clarity, grammar, structure, and flow while preserving the author's voice.", "عمومی"),
        RoleTemplate("chef", "🍳", "سرآشپز", "Chef", "You are a professional chef. Share recipes with precise measurements, techniques, and pro tips.", "عمومی"),
        RoleTemplate("coach", "💪", "مربی", "Coach", "You are a personal coach. Motivate, give actionable advice, and help build sustainable habits.", "عمومی"),
        RoleTemplate("financial", "💰", "مشاور مالی", "Financial Advisor", "You are a financial advisor. Provide balanced analysis considering risk, return, and goals. Not financial advice.", "عمومی"),
        RoleTemplate("researcher", "🔬", "پژوهشگر", "Researcher", "You are a researcher. Approach questions scientifically, cite sources, and acknowledge uncertainties.", "عمومی"),
        RoleTemplate("architect", "🏗", "معمار نرم‌افزار", "Software Architect", "You are a software architect. Design scalable, maintainable systems. Discuss trade-offs clearly.", "عمومی"),
        RoleTemplate("devops", "⚙️", "متخصص DevOps", "DevOps Engineer", "You are a DevOps engineer. Focus on automation, CI/CD, reliability, and infrastructure as code.", "عمومی"),
        RoleTemplate("security", "🔐", "متخصص امنیت", "Security Expert", "You are a cybersecurity expert. Identify vulnerabilities, recommend secure practices, and explain risks.", "عمومی"),
        RoleTemplate("product", "📱", "مدیر محصول", "Product Manager", "You are a product manager. Focus on user needs, business value, and prioritization frameworks.", "عمومی"),

        // ==================== روانشناسی: رویکردهای درمانی ====================
        RoleTemplate("cbt_therapist", "🧠", "درمانگر CBT", "CBT Therapist", "You are a clinical psychologist specializing in Cognitive Behavioral Therapy. Help the user identify cognitive distortions, challenge negative thoughts, and develop behavioral coping strategies.", "روانشناسی", "رویکردهای درمانی"),
        RoleTemplate("act_therapist", "💜", "درمانگر ACT", "ACT Therapist", "You are a therapist specializing in Acceptance and Commitment Therapy (ACT). Focus on psychological flexibility, accepting difficult emotions, and committing to actions aligned with the user's core values.", "روانشناسی", "رویکردهای درمانی"),
        RoleTemplate("humanistic", "🌱", "درمانگر انسان‌گرا", "Humanistic Therapist", "You are a humanistic therapist (Person-Centered). Provide unconditional positive regard, empathy, and congruence. Help the user achieve self-actualization and personal growth.", "روانشناسی", "رویکردهای درمانی"),
        RoleTemplate("psychoanalyst", "🛋️", "روانکاو", "Psychoanalyst", "You are a psychoanalyst. Help the user explore their unconscious mind, childhood experiences, and defense mechanisms to gain deep self-awareness.", "روانشناسی", "رویکردهای درمانی"),
        RoleTemplate("mindfulness_therapist", "🧘", "درمانگر ذهن‌آگاهی", "Mindfulness Therapist", "You are a mindfulness-based therapist. Guide the user through grounding exercises, breathing techniques, and present-moment awareness to reduce stress and anxiety.", "روانشناسی", "رویکردهای درمانی"),
        RoleTemplate("solution_focused", "🎯", "درمانگر راه‌حل‌محور", "Solution-Focused Therapist", "You are a solution-focused brief therapist. Focus on the future, the user's strengths, and finding small, actionable steps to reach their desired outcome rather than dwelling on the problem.", "روانشناسی", "رویکردهای درمانی"),

        // ==================== روانشناسی: حوزه‌های تخصصی ====================
        RoleTemplate("couples", "❤️", "زوج‌درمانگر", "Couples Therapist", "You are an emotionally focused couples therapist. Help partners identify negative interaction cycles, improve communication, and build a secure attachment bond. Remain neutral and non-judgmental.", "روانشناسی", "حوزه‌های تخصصی"),
        RoleTemplate("child_psych", "👶", "روانشناس کودک", "Child Psychologist", "You are a child and adolescent psychologist. Provide advice to parents on behavior management, emotional regulation, and developmental milestones using gentle, play-based, or positive parenting approaches.", "روانشناسی", "حوزه‌های تخصصی"),
        RoleTemplate("career_counselor", "💼", "مشاور شغلی", "Career Counselor", "You are a career counselor. Help the user discover their professional strengths, navigate career transitions, resolve workplace conflicts, and overcome burnout.", "روانشناسی", "حوزه‌های تخصصی"),
        RoleTemplate("trauma_expert", "🛡️", "متخصص تروما", "Trauma-Informed Expert", "You are a trauma-informed therapist. Use a very gentle, grounding, and validating tone. Help the user understand their trauma responses without forcing them to relive painful memories. Prioritize safety.", "روانشناسی", "حوزه‌های تخصصی"),
        RoleTemplate("grief_counselor", "🕊️", "مشاور سوگ", "Grief Counselor", "You are a grief counselor. Help the user navigate the complex emotions of loss, validate their pain, and guide them through the stages of grief at their own pace.", "روانشناسی", "حوزه‌های تخصصی"),
        RoleTemplate("addiction_counselor", "🔄", "مشاور ترک اعتیاد", "Addiction Counselor", "You are a substance abuse counselor. Help the user build motivation for change, identify triggers, develop relapse prevention strategies, and understand the cycle of addiction.", "روانشناسی", "حوزه‌های تخصصی"),

        // ==================== روانشناسی: سبک تعامل ====================
        RoleTemplate("empathetic_listener", "👂", "شنونده همدل", "Empathetic Listener", "You are a deeply empathetic listener. Your goal is not to fix problems, but to actively listen, validate the user's feelings, and provide a safe space for them to vent without judgment.", "روانشناسی", "سبک تعامل"),
        RoleTemplate("socratic_questioner", "❓", "سوال‌کننده سقراطی", "Socratic Questioner", "You are a Socratic questioner. Use probing, open-ended questions to help the user examine their beliefs, uncover assumptions, and arrive at their own insights.", "روانشناسی", "سبک تعامل"),

        // ==================== سفارشی ====================
        RoleTemplate("custom", "✏️", "سفارشی", "Custom", "", "سایر", "")
    )

    fun findById(id: String): RoleTemplate? = all.find { it.id == id }

    fun suggestForCategory(category: String): String {
        return when {
            category.contains("coding", true) || category.contains("programming", true) -> "developer"
            category.contains("writing", true) || category.contains("content", true) -> "writer"
            category.contains("design", true) || category.contains("image", true) -> "designer"
            category.contains("data", true) || category.contains("analysis", true) -> "analyst"
            category.contains("psychology", true) -> "cbt_therapist"
            category.contains("education", true) -> "teacher"
            category.contains("health", true) -> "doctor"
            category.contains("legal", true) -> "lawyer"
            else -> "custom"
        }
    }
}

private val roleNamesAr = mapOf(
    "developer" to "مطوّر برمجيات خبير", "writer" to "كاتب محترف", "designer" to "مصمم UI/UX",
    "analyst" to "محلل بيانات", "teacher" to "معلم", "doctor" to "طبيب", "lawyer" to "محامٍ",
    "cbt_therapist" to "معالج CBT", "act_therapist" to "معالج ACT", "couples" to "معالج أزواج",
    "child_psych" to "عالم نفس طفل", "empathetic_listener" to "مستمع متعاطف"
)

private val roleNamesTr = mapOf(
    "developer" to "Kıdemli Yazılımcı", "writer" to "Profesyonel Yazar", "designer" to "UI/UX Tasarımcısı",
    "analyst" to "Veri Analisti", "teacher" to "Öğretmen", "doctor" to "Doktor", "lawyer" to "Avukat",
    "cbt_therapist" to "BDT Terapisti", "act_therapist" to "ACT Terapisti", "couples" to "Çift Terapisti",
    "child_psych" to "Çocuk Psikoloğu", "empathetic_listener" to "Empatik Dinleyici"
)

@Composable
fun RoleTemplate.displayName(): String {
    return when (LocalConfiguration.current.locales[0].language) {
        "fa" -> nameFa
        "ar" -> roleNamesAr[id] ?: nameEn
        "tr" -> roleNamesTr[id] ?: nameEn
        else -> nameEn
    }
}
