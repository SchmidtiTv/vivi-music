/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.mini

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.datastore.preferences.core.MutablePreferences
import com.music.vivi.constants.MiniPlayerThemeKey
import com.music.vivi.constants.UseAppleMiniPlayerKey
import com.music.vivi.constants.UseNewMiniPlayerDesignKey
import com.music.vivi.ui.player.mini.apple.AppleMiniPlayerTheme
import com.music.vivi.ui.player.mini.classic.ClassicMiniPlayerTheme
import com.music.vivi.ui.player.mini.m3e.M3eMiniPlayerTheme
import com.music.vivi.utils.rememberMappedPreference

object MiniPlayerThemeRegistry {
    val themes: List<MiniPlayerTheme> = listOf(
        ClassicMiniPlayerTheme,
        M3eMiniPlayerTheme,
        AppleMiniPlayerTheme,
    )

    val default: MiniPlayerTheme get() = M3eMiniPlayerTheme

    init {
        require(themes.distinctBy { it.id }.size == themes.size) { "Duplicate mini player theme id" }
    }

    fun byId(id: String?): MiniPlayerTheme = themes.firstOrNull { it.id == id } ?: default

    fun migrateLegacySettings(settings: MutablePreferences) {
        if (settings[MiniPlayerThemeKey] != null) return
        settings[MiniPlayerThemeKey] = when {
            settings[UseAppleMiniPlayerKey] == true -> AppleMiniPlayerTheme.id
            settings[UseNewMiniPlayerDesignKey] != false -> M3eMiniPlayerTheme.id
            else -> ClassicMiniPlayerTheme.id
        }
        settings.remove(UseAppleMiniPlayerKey)
        settings.remove(UseNewMiniPlayerDesignKey)
    }
}

@Composable
fun rememberMiniPlayerTheme(): MutableState<MiniPlayerTheme> = rememberMappedPreference(
    key = MiniPlayerThemeKey,
    defaultValue = MiniPlayerThemeRegistry.default,
    fromKey = MiniPlayerThemeRegistry::byId,
    toKey = MiniPlayerTheme::id,
)
