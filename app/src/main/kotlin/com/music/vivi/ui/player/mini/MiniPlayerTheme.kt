/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.mini

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.music.vivi.ui.player.ProgressState

/** A mini player design. Every theme must be listed in [MiniPlayerThemeRegistry]. */
interface MiniPlayerTheme {
    /** Saved in settings, so it must never change once released. */
    val id: String

    @get:StringRes
    val nameRes: Int

    /** Sits above the navigation bar with `MiniPlayerBottomSpacing` below it. */
    val floating: Boolean

    /** Hides the background behind the bottom system inset when there is no navigation bar. */
    val transparentBottomInset: Boolean

    @Composable
    fun Content(
        progressState: ProgressState,
        modifier: Modifier,
        vertical: Boolean,
        onClick: (() -> Unit)?,
    )
}
