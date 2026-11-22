/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.datastore.preferences.core.MutablePreferences
import com.music.vivi.R
import com.music.vivi.constants.PlayerThemeKey
import com.music.vivi.constants.UseNewPlayerDesignKey
import com.music.vivi.utils.rememberMappedPreference

object ClassicPlayerTheme : PlayerTheme {
    override val id = "classic"
    override val nameRes = R.string.classic_player
    override val isNewDesign = false
}

object M3ePlayerTheme : PlayerTheme {
    override val id = "m3e"
    override val nameRes = R.string.new_player_design
    override val isNewDesign = true
}

object PlayerThemeRegistry {
    /** In the order they appear in settings. */
    val themes: List<PlayerTheme> = listOf(
        ClassicPlayerTheme,
        M3ePlayerTheme,
    )

    val default: PlayerTheme get() = ClassicPlayerTheme

    init {
        require(themes.distinctBy { it.id }.size == themes.size) { "Duplicate player theme id" }
    }

    fun byId(id: String?): PlayerTheme = themes.firstOrNull { it.id == id } ?: default

    /** Converts the old on/off design setting into [PlayerThemeKey], once. */
    fun migrateLegacySettings(settings: MutablePreferences) {
        if (settings[PlayerThemeKey] != null) return
        settings[PlayerThemeKey] = if (settings[UseNewPlayerDesignKey] == true) M3ePlayerTheme.id else ClassicPlayerTheme.id
        settings.remove(UseNewPlayerDesignKey)
    }
}

@Composable
fun rememberPlayerTheme(): MutableState<PlayerTheme> = rememberMappedPreference(
    key = PlayerThemeKey,
    defaultValue = PlayerThemeRegistry.default,
    fromKey = PlayerThemeRegistry::byId,
    toKey = PlayerTheme::id,
)

@Composable
fun rememberUseNewPlayerDesign(): Boolean = rememberPlayerTheme().value.isNewDesign
