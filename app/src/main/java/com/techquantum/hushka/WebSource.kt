package com.techquantum.hushka

/**
 * منابع پرامپت برای واکشی از وب
 */
enum class WebSource(
    val id: String,
    val labelResId: Int,
    val emoji: String
) {
    GITHUB(
        id = "github",
        labelResId = R.string.web_source_github,
        emoji = "📦"
    ),
    HF_PROMPTS_CHAT(
        id = "hf_prompts_chat",
        labelResId = R.string.web_source_hf_prompts,
        emoji = "🤗"
    ),
    HF_STABLE_DIFFUSION(
        id = "hf_stable_diffusion",
        labelResId = R.string.web_source_hf_sd,
        emoji = "🎨"
    ),
    HF_JAILBREAK(
        id = "hf_jailbreak",
        labelResId = R.string.web_source_hf_jailbreak,
        emoji = "🔓"
    );
    
    companion object {
        fun fromId(id: String): WebSource =
            values().firstOrNull { it.id == id } ?: GITHUB
    }
}
