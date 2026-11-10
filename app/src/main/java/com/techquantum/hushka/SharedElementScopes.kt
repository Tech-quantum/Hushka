@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.techquantum.hushka

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

// Local برای دسترسی به SharedTransitionScope در هر جای اپ
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

// Local برای دسترسی به AnimatedVisibilityScope در هر جای اپ
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
