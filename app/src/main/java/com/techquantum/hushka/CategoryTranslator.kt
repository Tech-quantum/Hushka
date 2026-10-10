package com.techquantum.hushka

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun getLocalizedCategory(category: String): String {
    if (category.isBlank()) return ""
    
    return when {
        category.equals("web", true) -> stringResource(R.string.cat_web)
        category.equals("github", true) -> stringResource(R.string.cat_github)
        category.equals("image", true) -> stringResource(R.string.cat_image)
        category.equals("video", true) -> stringResource(R.string.cat_video)
        category.equals("audio", true) -> stringResource(R.string.cat_audio)
        category.startsWith("agent.", true) -> stringResource(R.string.domain_agent)
        category.startsWith("analysis.", true) -> stringResource(R.string.domain_analysis)
        category.startsWith("prompting.", true) -> stringResource(R.string.domain_prompting)
        category.startsWith("text.", true) -> stringResource(R.string.domain_text)
        category.startsWith("coding.", true) -> stringResource(R.string.domain_coding)
        category.startsWith("image.", true) -> stringResource(R.string.domain_image)
        category.startsWith("video.", true) -> stringResource(R.string.domain_video)
        else -> category
    }
}
