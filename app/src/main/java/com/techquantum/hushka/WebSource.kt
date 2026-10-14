package com.techquantum.hushka

/**
 * منابع پرامپت برای واکشی از وب
 */
enum class WebSource(
    val id: String,
    val labelResId: Int,
    val emoji: String
) {
    GITHUB("github", R.string.web_source_github, "📦"),
    HF_PROMPTS_CHAT("hf_prompts_chat", R.string.web_source_hf_prompts, "🤗"),
    HF_STABLE_DIFFUSION("hf_stable_diffusion", R.string.web_source_hf_sd, "🎨"),
    HF_JAILBREAK("hf_jailbreak", R.string.web_source_hf_jailbreak, "🔓"),
    HF_PERSIAN("hf_persian", R.string.web_source_hf_persian, "🇮🇷"),
    HF_DOLLY("hf_dolly", R.string.web_source_hf_dolly, "📋"),
    HF_WRITING("hf_writing", R.string.web_source_hf_writing, "✍️"),
    HF_CODE("hf_code", R.string.web_source_hf_code, "💻"),
    HF_MEDICAL("hf_medical", R.string.web_source_hf_medical, "🩺");

    companion object {
        fun fromId(id: String): WebSource =
            values().firstOrNull { it.id == id } ?: GITHUB
    }
}
