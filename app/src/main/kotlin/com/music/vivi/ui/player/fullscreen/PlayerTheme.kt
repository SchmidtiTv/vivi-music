/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen

import android.media.AudioManager
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.music.vivi.constants.AudioQuality
import com.music.vivi.constants.PlayerBackgroundStyle
import com.music.vivi.db.entities.Song
import com.music.vivi.models.MediaMetadata
import com.music.vivi.playback.CastConnectionHandler
import com.music.vivi.playback.PlayerConnection
import com.music.vivi.ui.component.BottomSheetState

interface PlayerTheme {
    val id: String

    @get:StringRes
    val nameRes: Int
}

interface StandalonePlayerTheme : PlayerTheme {
    @Composable
    fun Content(state: BottomSheetState, navController: NavController, modifier: Modifier, slideFromStart: Boolean)
}

interface SlotPlayerTheme : PlayerTheme {
    val controlsSpacing: Dp
    val bottomSpacing: Dp
    val solidSliderTrack: Boolean
    val growSliderWhileDragging: Boolean

    val hasTimeBadge: Boolean

    @Composable
    fun Actions(args: PlayerActionsArgs)

    @Composable
    fun TimeBadge(args: PlayerTimeBadgeArgs)

    @Composable
    fun ColumnScope.Controls(args: PlayerControlsArgs)

    @Composable
    fun QueueBar(args: PlayerQueueBarArgs)

    @Composable
    fun Queue(args: PlayerQueueArgs)
}

class PlayerActionsArgs(
    val mediaMetadata: MediaMetadata,
    val currentSong: Song?,
    val download: Download?,
    val showInlineLyrics: Boolean,
    val textButtonColor: Color,
    val iconButtonColor: Color,
    val playerConnection: PlayerConnection,
    val navController: NavController,
    val state: BottomSheetState,
    val onToggleFullScreen: () -> Unit,
)

class PlayerTimeBadgeArgs(
    val mediaMetadata: MediaMetadata,
    val sleepTimerEnabled: Boolean,
    val sleepTimerTimeLeft: Long,
    val audioQuality: AudioQuality,
    val textColor: Color,
)

class PlayerControlsArgs(
    val playerConnection: PlayerConnection,
    val castHandler: CastConnectionHandler?,
    val isCasting: Boolean,
    val castIsPlaying: Boolean,
    val castVolume: Float,
    val playbackState: Int,
    val isPlaying: Boolean,
    val effectiveIsPlaying: Boolean,
    val isMuted: Boolean,
    val isListenTogetherGuest: Boolean,
    val canSkipPrevious: Boolean,
    val canSkipNext: Boolean,
    val audioManager: AudioManager,
    val systemVolume: Float,
    val maxSystemVolume: Float,
    val bluetoothDeviceName: String?,
    val textColor: Color,
    val textButtonColor: Color,
    val iconButtonColor: Color,
    val sideButtonContainerColor: Color,
    val sideButtonContentColor: Color,
)

class PlayerQueueArgs(
    val docked: Boolean,
    val queueSheetState: BottomSheetState,
    val playerBottomSheetState: BottomSheetState,
    val navController: NavController,
    val background: Color,
    val onBackgroundColor: Color,
    val textColor: Color,
    val textButtonColor: Color,
    val iconButtonColor: Color,
    val pureBlack: Boolean,
    val showInlineLyrics: Boolean,
    val playerBackground: PlayerBackgroundStyle,
    val onToggleLyrics: () -> Unit,
)

class PlayerQueueBarArgs(
    val mediaMetadata: MediaMetadata?,
    val playerConnection: PlayerConnection,
    val navController: NavController,
    val playerBottomSheetState: BottomSheetState,
    val repeatMode: Int,
    val sleepTimerEnabled: Boolean,
    val sleepTimerTimeLeft: Long,
    val isListenTogetherGuest: Boolean,
    val isBluetoothConnected: Boolean,
    val showInlineLyrics: Boolean,
    val showCommentButton: Boolean,
    val showCommentSheet: Boolean,
    val playerBackground: PlayerBackgroundStyle,
    val textColor: Color,
    val textButtonColor: Color,
    val iconButtonColor: Color,
    val onOpenQueue: () -> Unit,
    val onToggleLyrics: () -> Unit,
    val onShowSleepTimerDialog: () -> Unit,
    val onShowComments: () -> Unit,
    val onShowAudioDevices: () -> Unit,
)
