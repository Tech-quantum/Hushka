package com.techquantum.hushka

/**
 * منابع پرامپت برای واکشی از وب
 */
enum class WebSource(
    val id: String,
    val labelResId: Int,
    val emoji: String
) {
    ALL(
        id = "all",
        labelResId = R.string.web_source_all,
        emoji = "🌐"
    ),
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
    ),
    HF_PERSIAN(
        id = "hf_persian",
        labelResId = R.string.web_source_hf_persian,
        emoji = "🇮🇷"
    ),
    HF_DOLLY(
        id = "hf_dolly",
        labelResId = R.string.web_source_hf_dolly,
        emoji = "📋"
    ),
    HF_WRITING(
        id = "hf_writing",
        labelResId = R.string.web_source_hf_writing,
        emoji = "✍️"
    ),
    HF_CODE(
        id = "hf_code",
        labelResId = R.string.web_source_hf_code,
        emoji = "💻"
    ),
    HF_MEDICAL(
        id = "hf_medical",
        labelResId = R.string.web_source_hf_medical,
        emoji = "🩺"
    );

    companion object {
        fun fromId(id: String): WebSource =
            values().firstOrNull { it.id == id } ?: GITHUB
    }
}
