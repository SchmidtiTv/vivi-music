/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.datastore.preferences.core.MutablePreferences
import com.music.vivi.constants.PlayerThemeKey
import com.music.vivi.constants.UseNewPlayerDesignKey
import com.music.vivi.constants.UsePlayerV2Key
import com.music.vivi.ui.player.fullscreen.apple.ApplePlayerTheme
import com.music.vivi.ui.player.fullscreen.applemusicv17.AppleMusicV17PlayerTheme
import com.music.vivi.ui.player.fullscreen.m3e.M3ePlayerTheme
import com.music.vivi.utils.rememberMappedPreference

object PlayerThemeRegistry {
    val themes: List<PlayerTheme> = listOf(
        ApplePlayerTheme,
        M3ePlayerTheme,
        AppleMusicV17PlayerTheme,
    )

    val default: PlayerTheme get() = AppleMusicV17PlayerTheme

    init {
        require(themes.distinctBy { it.id }.size == themes.size) { "Duplicate player theme id" }
        require(themes.all { it is SlotPlayerTheme || it is StandalonePlayerTheme }) {
            "Player themes must be a SlotPlayerTheme or a StandalonePlayerTheme"
        }
    }

    fun byId(id: String?): PlayerTheme = themes.firstOrNull { it.id == id } ?: default

    fun migrateLegacySettings(settings: MutablePreferences) {
        if (settings[PlayerThemeKey] != null) return
        settings[PlayerThemeKey] = when {
            settings[UsePlayerV2Key] ?: true -> AppleMusicV17PlayerTheme.id
            settings[UseNewPlayerDesignKey] == true -> M3ePlayerTheme.id
            else -> ApplePlayerTheme.id
        }
        settings.remove(UsePlayerV2Key)
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
