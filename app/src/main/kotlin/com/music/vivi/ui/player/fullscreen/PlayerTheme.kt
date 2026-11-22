/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen

import androidx.annotation.StringRes

/** A full-screen player design. Every theme must be listed in [PlayerThemeRegistry]. */
interface PlayerTheme {
    /** Saved in settings, so it must never change once released. */
    val id: String

    @get:StringRes
    val nameRes: Int

    /** Whether this is the expressive design with the sheet queue, as opposed to the inline classic one. */
    val isNewDesign: Boolean
}
