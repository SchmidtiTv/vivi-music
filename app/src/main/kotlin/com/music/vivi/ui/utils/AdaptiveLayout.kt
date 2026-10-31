package com.music.vivi.ui.utils

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

@Immutable
data class AdaptiveLayout(
    val useNavRail: Boolean,
    // Landscape phones are too short to fit the vertical mini player under the rail items.
    val miniPlayerInRail: Boolean,
    val constrainMiniPlayer: Boolean,
    val playerSideBySide: Boolean,
)

val LocalAdaptiveLayout = staticCompositionLocalOf<AdaptiveLayout> {
    error("AdaptiveLayout not provided")
}

@Composable
fun rememberAdaptiveLayout(): AdaptiveLayout {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return remember(sizeClass) {
        val mediumWidth = sizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
        val expandedWidth = sizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)
        val compactHeight = !sizeClass.isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND)
        AdaptiveLayout(
            useNavRail = mediumWidth,
            miniPlayerInRail = mediumWidth && !compactHeight,
            constrainMiniPlayer = expandedWidth,
            playerSideBySide = expandedWidth || compactHeight,
        )
    }
}
