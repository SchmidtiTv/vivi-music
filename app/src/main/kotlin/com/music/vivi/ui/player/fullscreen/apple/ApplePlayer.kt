/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen.apple

import android.media.AudioManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player.STATE_ENDED
import androidx.navigation.NavController
import com.music.vivi.R
import com.music.vivi.constants.AudioQuality
import com.music.vivi.constants.PlayerHorizontalPadding
import com.music.vivi.db.entities.Song
import com.music.vivi.extensions.togglePlayPause
import com.music.vivi.models.MediaMetadata
import com.music.vivi.playback.CastConnectionHandler
import com.music.vivi.playback.PlayerConnection
import com.music.vivi.ui.component.BottomSheetState
import com.music.vivi.ui.component.LocalBottomSheetPageState
import com.music.vivi.ui.component.LocalMenuState
import com.music.vivi.ui.component.PlayerSliderTrack
import com.music.vivi.ui.component.ResizableIconButton
import com.music.vivi.ui.menu.OldPlayerMenu
import com.music.vivi.ui.player.fullscreen.PlayerActionsArgs
import com.music.vivi.ui.player.fullscreen.PlayerControlsArgs
import com.music.vivi.ui.player.fullscreen.PlayerQueueBarArgs
import com.music.vivi.ui.player.fullscreen.PlayerTimeBadgeArgs
import com.music.vivi.ui.player.fullscreen.SlotPlayerTheme
import com.music.vivi.ui.utils.ShowMediaInfo
import com.music.vivi.ui.utils.ShowOffsetDialog
import com.music.vivi.utils.makeTimeString
import com.music.vivi.vivimusic.isBuds
import com.music.vivi.vivimusic.isSpeaker
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ApplePlayerTheme : SlotPlayerTheme {
    override val id = "apple"
    override val nameRes = R.string.classic_player
    override val controlsSpacing = 8.dp
    override val bottomSpacing = 8.dp
    override val solidSliderTrack = false
    override val growSliderWhileDragging = true
    override val hasTimeBadge = true

    @Composable
    override fun Actions(args: PlayerActionsArgs) {
        ApplePlayerActions(
            mediaMetadata = args.mediaMetadata,
            currentSong = args.currentSong,
            showInlineLyrics = args.showInlineLyrics,
            textButtonColor = args.textButtonColor,
            playerConnection = args.playerConnection,
            navController = args.navController,
            state = args.state,
            onToggleFullScreen = args.onToggleFullScreen,
        )
    }

    @Composable
    override fun TimeBadge(args: PlayerTimeBadgeArgs) {
        AppleAudioQualityBadge(
            mediaMetadata = args.mediaMetadata,
            sleepTimerEnabled = args.sleepTimerEnabled,
            sleepTimerTimeLeft = args.sleepTimerTimeLeft,
            audioQuality = args.audioQuality,
            textColor = args.textColor,
        )
    }

    @Composable
    override fun ColumnScope.Controls(args: PlayerControlsArgs) {
        ApplePlayerControls(
            playerConnection = args.playerConnection,
            castHandler = args.castHandler,
            isCasting = args.isCasting,
            castIsPlaying = args.castIsPlaying,
            castVolume = args.castVolume,
            playbackState = args.playbackState,
            isPlaying = args.isPlaying,
            effectiveIsPlaying = args.effectiveIsPlaying,
            isMuted = args.isMuted,
            isListenTogetherGuest = args.isListenTogetherGuest,
            canSkipPrevious = args.canSkipPrevious,
            canSkipNext = args.canSkipNext,
            audioManager = args.audioManager,
            systemVolume = args.systemVolume,
            maxSystemVolume = args.maxSystemVolume,
            bluetoothDeviceName = args.bluetoothDeviceName,
            textColor = args.textColor,
            textButtonColor = args.textButtonColor,
        )
    }

    @Composable
    override fun QueueBar(args: PlayerQueueBarArgs) {
        AppleQueueBar(
            playerConnection = args.playerConnection,
            sleepTimerEnabled = args.sleepTimerEnabled,
            sleepTimerTimeLeft = args.sleepTimerTimeLeft,
            isListenTogetherGuest = args.isListenTogetherGuest,
            isBluetoothConnected = args.isBluetoothConnected,
            textColor = args.textColor,
            onOpenQueue = args.onOpenQueue,
            onToggleLyrics = args.onToggleLyrics,
            onShowSleepTimerDialog = args.onShowSleepTimerDialog,
            onShowAudioDevices = args.onShowAudioDevices,
        )
    }

}
