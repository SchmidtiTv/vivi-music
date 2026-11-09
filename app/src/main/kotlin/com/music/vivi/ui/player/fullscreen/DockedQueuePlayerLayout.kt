/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import com.music.vivi.constants.PlayerBackgroundStyle
import com.music.vivi.constants.QueuePeekHeight
import com.music.vivi.models.MediaMetadata
import com.music.vivi.ui.component.BottomSheetState
import com.music.vivi.ui.component.Lyrics
import com.music.vivi.ui.player.fullscreen.classic.ClassicBottomActions
import com.music.vivi.ui.player.PlayerQueueBar
import com.music.vivi.ui.player.Queue
import com.music.vivi.ui.player.Thumbnail
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun DockedQueuePlayerLayout(
    state: BottomSheetState,
    queueSheetState: BottomSheetState,
    navController: NavController,
    sheetNestedScroll: Modifier,
    mediaMetadata: MediaMetadata?,
    controlsContent: @Composable ColumnScope.(MediaMetadata) -> Unit,
    spotifyCanvasUrl: String?,
    playerBackground: PlayerBackgroundStyle,
    sliderPosition: Long?,
    effectivePosition: () -> Long,
    isListenTogetherGuest: Boolean,
    isFullScreen: Boolean,
    useNewPlayerDesign: Boolean,
    showInlineLyrics: Boolean,
    onShowInlineLyricsChange: (Boolean) -> Unit,
    queueBackground: Color,
    onBackgroundColor: Color,
    TextBackgroundColor: Color,
    textButtonColor: Color,
    iconButtonColor: Color,
    pureBlack: Boolean,
    bluetoothDeviceName: String?,
    nameToShow: String?,
    onShowAudioDevices: () -> Unit,
) {
    val density = LocalDensity.current
    val verticalPadding = max(
        WindowInsets.systemBars.getTop(density),
        WindowInsets.systemBars.getBottom(density)
    )
    val verticalPaddingDp = with(density) { verticalPadding.toDp() }
    val verticalWindowInsets = WindowInsets(left = 0.dp, top = verticalPaddingDp, right = 0.dp, bottom = verticalPaddingDp)

    Row(
        modifier = Modifier
            .windowInsetsPadding(
                WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).add(verticalWindowInsets)
            )
            .padding(bottom = 24.dp)
            .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .then(sheetNestedScroll)
            ) {
                if (spotifyCanvasUrl == null && playerBackground != PlayerBackgroundStyle.APPLE_MUSIC) {
                    val currentSliderPosition by rememberUpdatedState(sliderPosition)
                    val sliderPositionProvider = remember { { currentSliderPosition } }
                    val isExpandedProvider = remember(state) { { state.isExpanded } }
                    Thumbnail(
                        sliderPositionProvider = sliderPositionProvider,
                        modifier = Modifier.animateContentSize(),
                        isPlayerExpanded = isExpandedProvider,
                        isLandscape = true,
                        isListenTogetherGuest = isListenTogetherGuest
                    )
                }
            }

            mediaMetadata?.let {
                controlsContent(it)
            }

            AnimatedVisibility(visible = !isFullScreen) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(QueuePeekHeight)
                        .consumeWindowInsets(WindowInsets.systemBars)
                ) {
                    if (useNewPlayerDesign) {
                        PlayerQueueBar(
                            navController = navController,
                            playerBottomSheetState = state,
                            textColor = TextBackgroundColor,
                            textButtonColor = textButtonColor,
                            iconButtonColor = iconButtonColor,
                            showInlineLyrics = showInlineLyrics,
                            playerBackground = playerBackground,
                            onToggleLyrics = { onShowInlineLyricsChange(!showInlineLyrics) },
                            onOpenQueue = { onShowInlineLyricsChange(false) },
                        )
                    } else {
                        ClassicBottomActions(
                            bluetoothDeviceName = bluetoothDeviceName,
                            nameToShow = nameToShow,
                            queueSheetState = queueSheetState,
                            showInlineLyrics = showInlineLyrics,
                            showInlineQueue = !showInlineLyrics,
                            useNewPlayerDesign = false,
                            textButtonColor = textButtonColor,
                            onShowInlineLyricsChange = onShowInlineLyricsChange,
                            onShowInlineQueueChange = { if (it) onShowInlineLyricsChange(false) },
                            onShowAudioDevices = onShowAudioDevices,
                        )
                    }
                }
            }
        }

        val lyricsAlpha by animateFloatAsState(
            targetValue = if (showInlineLyrics) 1f else 0f,
            label = "lyricsAlpha"
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 1f - lyricsAlpha }
                    .then(if (showInlineLyrics) Modifier.clearAndSetSemantics {} else Modifier)
                    .padding(start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .consumeWindowInsets(WindowInsets.systemBars)
            ) {
                Queue(
                    state = queueSheetState,
                    playerBottomSheetState = state,
                    navController = navController,
                    docked = true,
                    background = queueBackground,
                    onBackgroundColor = onBackgroundColor,
                    TextBackgroundColor = TextBackgroundColor,
                    textButtonColor = textButtonColor,
                    iconButtonColor = iconButtonColor,
                    pureBlack = pureBlack,
                    showInlineLyrics = showInlineLyrics,
                    playerBackground = playerBackground,
                    onToggleLyrics = { onShowInlineLyricsChange(!showInlineLyrics) },
                )
            }

            if (showInlineLyrics || lyricsAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent().changes.forEach { it.consume() }
                                }
                            }
                        }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = lyricsAlpha }
                ) {
                    InlineLyricsView(
                        mediaMetadata = mediaMetadata,
                        showLyrics = true,
                        positionProvider = { sliderPosition ?: effectivePosition() },
                        showMenuButton = true,
                    )
                }
            }
        }
    }
}
