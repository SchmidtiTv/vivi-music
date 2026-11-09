/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.mini

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.music.vivi.ui.player.ProgressState

interface MiniPlayerTheme {
    val id: String

    @get:StringRes
    val nameRes: Int

    val floating: Boolean

    val transparentBottomInset: Boolean

    @Composable
    fun Content(
        progressState: ProgressState,
        modifier: Modifier,
        vertical: Boolean,
        onClick: (() -> Unit)?,
    )
}
