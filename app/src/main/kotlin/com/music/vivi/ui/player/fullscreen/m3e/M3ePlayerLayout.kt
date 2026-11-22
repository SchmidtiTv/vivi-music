/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen.m3e

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.music.vivi.constants.PlayerBackgroundStyle
import com.music.vivi.models.MediaMetadata
import com.music.vivi.ui.component.BottomSheetState
import com.music.vivi.ui.component.Lyrics
import com.music.vivi.ui.player.fullscreen.InlineLyricsView
import com.music.vivi.ui.player.Thumbnail

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun M3ePlayerLayout(
    state: BottomSheetState,
    bottomPadding: Dp,
    mediaMetadata: MediaMetadata?,
    showInlineLyrics: Boolean,
    sliderPosition: Long?,
    effectivePosition: () -> Long,
    spotifyCanvasUrl: String?,
    playerBackground: PlayerBackgroundStyle,
    isListenTogetherGuest: Boolean,
    controlsContent: @Composable ColumnScope.(MediaMetadata) -> Unit,
) {
Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier =
    Modifier
        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
        .padding(bottom = bottomPadding)
        .animateContentSize(),
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.weight(1f),
    ) {
        val currentSliderPosition by rememberUpdatedState(sliderPosition)
        val sliderPositionProvider = remember { { currentSliderPosition } }
        val isExpandedProvider = remember(state) { { state.isExpanded } }
        AnimatedContent(
            targetState = showInlineLyrics,
            label = "Lyrics",
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { showLyrics ->
            if (showLyrics) {
                InlineLyricsView(
                    mediaMetadata = mediaMetadata,
                    showLyrics = showLyrics,
                    positionProvider = { sliderPosition ?: effectivePosition() }
                )
            } else if (spotifyCanvasUrl == null && playerBackground != PlayerBackgroundStyle.APPLE_MUSIC) {
                Thumbnail(
                    sliderPositionProvider = sliderPositionProvider,
                    modifier = Modifier.nestedScroll(state.preUpPostDownNestedScrollConnection),
                    isPlayerExpanded = isExpandedProvider,
                    isListenTogetherGuest = isListenTogetherGuest
                )
            }
        }
    }

    mediaMetadata?.let {
        controlsContent(it)
    }

    Spacer(Modifier.height(30.dp))
}
}
