/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen.m3e

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.music.vivi.R
import com.music.vivi.ui.player.fullscreen.PlayerActionsArgs
import com.music.vivi.ui.player.fullscreen.PlayerControlsArgs
import com.music.vivi.ui.player.fullscreen.PlayerQueue
import com.music.vivi.ui.player.fullscreen.PlayerQueueArgs
import com.music.vivi.ui.player.fullscreen.PlayerQueueBarArgs
import com.music.vivi.ui.player.fullscreen.PlayerTimeBadgeArgs
import com.music.vivi.ui.player.fullscreen.SlotPlayerTheme

object M3ePlayerTheme : SlotPlayerTheme {
    override val id = "m3e"
    override val nameRes = R.string.new_player_design
    override val controlsSpacing = 24.dp
    override val bottomSpacing = 30.dp
    override val solidSliderTrack = true
    override val growSliderWhileDragging = false
    override val hasTimeBadge = false

    @Composable
    override fun Actions(args: PlayerActionsArgs) {
        M3ePlayerActions(
            mediaMetadata = args.mediaMetadata,
            currentSong = args.currentSong,
            download = args.download,
            showInlineLyrics = args.showInlineLyrics,
            textButtonColor = args.textButtonColor,
            iconButtonColor = args.iconButtonColor,
            playerConnection = args.playerConnection,
            onToggleFullScreen = args.onToggleFullScreen,
        )
    }

    @Composable
    override fun TimeBadge(args: PlayerTimeBadgeArgs) {
    }

    @Composable
    override fun ColumnScope.Controls(args: PlayerControlsArgs) {
        M3ePlayerControls(
            playerConnection = args.playerConnection,
            castHandler = args.castHandler,
            isCasting = args.isCasting,
            castIsPlaying = args.castIsPlaying,
            playbackState = args.playbackState,
            effectiveIsPlaying = args.effectiveIsPlaying,
            isMuted = args.isMuted,
            isListenTogetherGuest = args.isListenTogetherGuest,
            canSkipPrevious = args.canSkipPrevious,
            canSkipNext = args.canSkipNext,
            textButtonColor = args.textButtonColor,
            iconButtonColor = args.iconButtonColor,
            sideButtonContainerColor = args.sideButtonContainerColor,
            sideButtonContentColor = args.sideButtonContentColor,
        )
    }

    @Composable
    override fun QueueBar(args: PlayerQueueBarArgs) {
        M3eQueueBar(
            mediaMetadata = args.mediaMetadata,
            playerConnection = args.playerConnection,
            navController = args.navController,
            playerBottomSheetState = args.playerBottomSheetState,
            repeatMode = args.repeatMode,
            sleepTimerEnabled = args.sleepTimerEnabled,
            sleepTimerTimeLeft = args.sleepTimerTimeLeft,
            isListenTogetherGuest = args.isListenTogetherGuest,
            showInlineLyrics = args.showInlineLyrics,
            showCommentButton = args.showCommentButton,
            showCommentSheet = args.showCommentSheet,
            playerBackground = args.playerBackground,
            textColor = args.textColor,
            textButtonColor = args.textButtonColor,
            iconButtonColor = args.iconButtonColor,
            onOpenQueue = args.onOpenQueue,
            onToggleLyrics = args.onToggleLyrics,
            onShowSleepTimerDialog = args.onShowSleepTimerDialog,
            onShowComments = args.onShowComments,
        )
    }

    @Composable
    override fun Queue(args: PlayerQueueArgs) {
        PlayerQueue(
            docked = args.docked,
            state = args.queueSheetState,
            playerBottomSheetState = args.playerBottomSheetState,
            navController = args.navController,
            theme = this,
            background = args.background,
            onBackgroundColor = args.onBackgroundColor,
            TextBackgroundColor = args.textColor,
            textButtonColor = args.textButtonColor,
            iconButtonColor = args.iconButtonColor,
            pureBlack = args.pureBlack,
            showInlineLyrics = args.showInlineLyrics,
            playerBackground = args.playerBackground,
            onToggleLyrics = args.onToggleLyrics,
        )
    }
}
