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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ApplePlayerActions(
    mediaMetadata: MediaMetadata,
    currentSong: Song?,
    showInlineLyrics: Boolean,
    textButtonColor: Color,
    playerConnection: PlayerConnection,
    navController: NavController,
    state: BottomSheetState,
    onToggleFullScreen: () -> Unit,
) {
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current

    AnimatedContent(targetState = showInlineLyrics, label = "DownloadButton") { showLyrics ->
        if (showLyrics) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(textButtonColor.copy(alpha = 0.2f))
                    .clickable(onClick = onToggleFullScreen),
            ) {
                Icon(
                    painter = painterResource(R.drawable.fullscreen),
                    contentDescription = null,
                    tint = textButtonColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(textButtonColor.copy(alpha = 0.2f))
                    .clickable {
                        menuState.show {
                            OldPlayerMenu(
                                mediaMetadata = mediaMetadata,
                                navController = navController,
                                playerBottomSheetState = state,
                                onShowDetailsDialog = {
                                    mediaMetadata.id.let {
                                        bottomSheetPageState.show {
                                           ShowMediaInfo(it)
                                        }
                                    }
                                },
                                onDismiss = menuState::dismiss
                            )
                        }
                    },
            ) {
                Icon(
                    painter = painterResource(R.drawable.more_vert),
                    contentDescription = null,
                    tint = textButtonColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.size(12.dp))

    AnimatedContent(targetState = showInlineLyrics, label = "LikeButton") { showLyrics ->
        if (showLyrics) {
            val currentLyrics by playerConnection.currentLyrics.collectAsState(initial = null)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(textButtonColor.copy(alpha = 0.2f))
                    .clickable {
                        menuState.show {
                            com.music.vivi.ui.menu.LyricsMenu(
                                lyricsProvider = { currentLyrics },
                                songProvider = { currentSong?.song },
                                mediaMetadataProvider = { mediaMetadata },
                                onDismiss = menuState::dismiss,
                                onShowOffsetDialog = {
                                    bottomSheetPageState.show {
                                        ShowOffsetDialog(
                                            songProvider = { currentSong?.song }
                                        )
                                    }
                                }
                            )
                        }
                    },
            ) {
                Icon(
                    painter = painterResource(R.drawable.more_horiz),
                    contentDescription = null,
                    tint = textButtonColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(textButtonColor.copy(alpha = 0.2f))
                    .clickable(onClick = playerConnection::toggleLike),
            ) {
                Icon(
                    painter = painterResource(
                        if (currentSong?.song?.liked == true)
                            R.drawable.favorite
                        else R.drawable.favorite_border
                    ),
                    contentDescription = null,
                    tint = textButtonColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppleAudioQualityBadge(
    mediaMetadata: MediaMetadata,
    sleepTimerEnabled: Boolean,
    sleepTimerTimeLeft: Long,
    audioQuality: AudioQuality,
    textColor: Color,
) {
    val bottomSheetPageState = LocalBottomSheetPageState.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(textColor.copy(alpha = 0.08f))
            .border(
                width = 0.5.dp,
                color = textColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(enabled = !sleepTimerEnabled) {
                mediaMetadata.id.let {
                    bottomSheetPageState.show {
                        ShowMediaInfo(it)
                    }
                }
            }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        AnimatedContent(
            targetState = sleepTimerEnabled,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "QualityTimerSwitcher"
        ) { isTimerActive ->
            if (isTimerActive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.sleep_timer),
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = makeTimeString(sleepTimerTimeLeft.coerceAtLeast(0)),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = textColor.copy(alpha = 0.8f),
                        maxLines = 1,
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "QualityIconTransition")
                    val animatedRotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "QualityIconRotation"
                    )

                    val iconBrush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            textColor.copy(alpha = 1.0f),
                            Color.Transparent
                        )
                    )

                    Icon(
                        painter = painterResource(R.drawable.stream_old_player),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(12.dp)
                            .graphicsLayer(alpha = 0.99f)
                            .drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    rotate(animatedRotation) {
                                        drawRect(iconBrush, blendMode = BlendMode.SrcIn)
                                    }
                                }
                            }
                    )
                    Text(
                        text = when (audioQuality) {
                            AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                            AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                            AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                        }.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = textColor.copy(alpha = 0.8f),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColumnScope.ApplePlayerControls(
    playerConnection: PlayerConnection,
    castHandler: CastConnectionHandler?,
    isCasting: Boolean,
    castIsPlaying: Boolean,
    castVolume: Float,
    playbackState: Int,
    isPlaying: Boolean,
    effectiveIsPlaying: Boolean,
    isMuted: Boolean,
    isListenTogetherGuest: Boolean,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    audioManager: AudioManager,
    systemVolume: Float,
    maxSystemVolume: Float,
    bluetoothDeviceName: String?,
    textColor: Color,
    textButtonColor: Color,
) {
    val playPauseRoundness by animateDpAsState(
        targetValue = if (isPlaying) 24.dp else 36.dp,
        animationSpec = tween(durationMillis = 90, easing = LinearEasing),
        label = "playPauseRoundness",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = PlayerHorizontalPadding),
    ) {
    //    Box(modifier = Modifier.weight(1f)) {
    //        ResizableIconButton(
    //            icon = when (repeatMode) {
    //                Player.REPEAT_MODE_OFF, Player.REPEAT_MODE_ALL -> R.drawable.repeat
    //                Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
    //                else -> throw IllegalStateException()
    //            },
    //            color = textColor,
    //            modifier = Modifier
    //                .size(32.dp)
    //                .padding(4.dp)
    //                .align(Alignment.Center)
    //                .alpha(if (isListenTogetherGuest) 0.5f else 1f),
    //            enabled = !isListenTogetherGuest,
    //            onClick = {
    //                playerConnection.player.toggleRepeatMode()
    //            }
    //        )
    //    }

        Box(modifier = Modifier.weight(1f)) {
            ResizableIconButton(
                icon = R.drawable.apple_skip_previous,
                enabled = canSkipPrevious && !isListenTogetherGuest,
                color = textColor,
                modifier =
            Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .alpha(if (isListenTogetherGuest) 0.5f else 1f),
                onClick = playerConnection::seekToPrevious,
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier =
            Modifier
                .size(100.dp) //100
                .clip(RoundedCornerShape(playPauseRoundness))
                .clickable {
                    if (isListenTogetherGuest) {
                        playerConnection.toggleMute()
                        return@clickable
                    }
                    if (isCasting) {
                        if (castIsPlaying) {
                            castHandler?.pause()
                        } else {
                            castHandler?.play()
                        }
                    } else if (playbackState == STATE_ENDED) {
                        playerConnection.player.seekTo(0, 0)
                        playerConnection.player.playWhenReady = true
                    } else {
                        playerConnection.player.togglePlayPause()
                    }
                },
        ) {
            Image(
                painter =
                painterResource(
                    if (isListenTogetherGuest) {
                        if (isMuted) R.drawable.volume_off else R.drawable.volume_up
                    } else if (playbackState ==
                        STATE_ENDED
                    ) {
                        R.drawable.replay
                    } else if (effectiveIsPlaying) {
                        R.drawable.pause_applemusic
                    } else {
                        R.drawable.play_applemusic
                    },
                ),
                contentDescription = null,
                colorFilter = ColorFilter.tint(textColor),
                modifier =
                Modifier
                    .align(Alignment.Center)
                    .size(72.dp),
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            ResizableIconButton(
                icon = R.drawable.apple_skip_next,
                enabled = canSkipNext && !isListenTogetherGuest,
                color = textColor,
                modifier =
            Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .alpha(if (isListenTogetherGuest) 0.5f else 1f),
                onClick = playerConnection::seekToNext,
            )
        }

    //    Box(modifier = Modifier.weight(1f)) {
    //        ResizableIconButton(
    //            icon = if (currentSong?.song?.liked == true) R.drawable.favorite else R.drawable.favorite_border,
    //            color = if (currentSong?.song?.liked == true) MaterialTheme.colorScheme.error else textColor,
    //            modifier =
    //            Modifier
    //                .size(32.dp)
    //                .padding(4.dp)
    //                .align(Alignment.Center),
    //            onClick = playerConnection::toggleLike,
    //        )
    //    }
    }

    Spacer(modifier = Modifier.height(8.dp)) //space between play and audio

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PlayerHorizontalPadding)
    ) {
        val volumeInteractionSource = remember { MutableInteractionSource() }
        val isVolumeDragged by volumeInteractionSource.collectIsDraggedAsState()
        val isVolumePressed by volumeInteractionSource.collectIsPressedAsState()
        val isVolumeActive = isVolumeDragged || isVolumePressed

        // Internal state to track drag value and avoid system feedback lag
        var dragVolume by remember { mutableFloatStateOf(systemVolume) }

        // Use a coroutine to update system volume to avoid UI blocking on fast swipes
        val scope = rememberCoroutineScope()

        LaunchedEffect(systemVolume) {
            if (!isVolumeActive) dragVolume = systemVolume
        }

        // Smoothly animate the volume position when changed via buttons
        val animatedSystemVolume by animateFloatAsState(
            targetValue = systemVolume,
            animationSpec = tween(150, easing = LinearOutSlowInEasing),
            label = "animatedSystemVolume"
        )

        val volume = if (isCasting) castVolume else {
            if (isVolumeActive) dragVolume else animatedSystemVolume
        }

        val volumeTrackHeight by animateDpAsState(
            targetValue = if (isVolumeActive) 16.dp else 10.dp,
            animationSpec = spring(
                dampingRatio = 0.7f, // Slightly more stable damping
                stiffness = 600f // Balanced stiffness for high-speed stability
            ),
            label = "volumeTrackHeight"
        )

        val volumeIconScale by animateFloatAsState(
            targetValue = if (isVolumeActive) 1.15f else 1f,
            animationSpec = spring(
                dampingRatio = 0.7f,
                stiffness = 600f
            ),
            label = "volumeIconScale"
        )

        Icon(
            painter = painterResource(R.drawable.volume_mute),
            contentDescription = null,
            tint = textButtonColor,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer(scaleX = volumeIconScale, scaleY = volumeIconScale)
        )

        Spacer(Modifier.width(12.dp))

        Slider(
            value = volume,
            onValueChange = { newVolume ->
                dragVolume = newVolume
                if (isCasting) {
                    castHandler?.setVolume(newVolume)
                } else {
                    // Non-blocking update to prevent "fast swipe" lag
                    scope.launch(Dispatchers.Default) {
                        val newStep = (newVolume * maxSystemVolume).roundToInt()
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newStep, 0)
                    }
                }
            },
            modifier = Modifier.weight(1f),
            interactionSource = volumeInteractionSource,
            thumb = {},
            track = { sliderState ->
                PlayerSliderTrack(
                    sliderState = sliderState,
                    colors = SliderDefaults.colors(
                        activeTrackColor = textButtonColor.copy(alpha = 0.7f),
                        inactiveTrackColor = textButtonColor.copy(alpha = 0.15f)
                    ),
                    trackHeight = volumeTrackHeight
                )
            }
        )

        Spacer(Modifier.width(12.dp))

        Icon(
            painter = painterResource(R.drawable.volume_up),
            contentDescription = null,
            tint = textButtonColor,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer(scaleX = volumeIconScale, scaleY = volumeIconScale)
        )
    }

    // Use a persistent state to keep the name during exit animation
    var lastNonNullName by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(bluetoothDeviceName) {
        if (bluetoothDeviceName != null) lastNonNullName = bluetoothDeviceName
    }

    AnimatedVisibility(
        visible = bluetoothDeviceName != null,
        enter = fadeIn(tween(400)) + expandVertically(tween(400)),
        exit = fadeOut(tween(400)) + shrinkVertically(tween(400)),
        label = "BluetoothInfoVisibility"
    ) {
        val nameToShow = bluetoothDeviceName ?: lastNonNullName
        Column {
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(
                        when {
                            isSpeaker(nameToShow) -> R.drawable.speaker_applemusic
                            isBuds(nameToShow) -> R.drawable.apple_airpods
                            else -> R.drawable.apple_headset
                        }
                    ),
                    contentDescription = null,
                    tint = textButtonColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(
                        when {
                            isSpeaker(nameToShow) -> 18.dp
                            isBuds(nameToShow) -> 20.dp
                            else -> 16.dp
                        }
                    )
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = nameToShow ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = textButtonColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppleQueueBar(
    playerConnection: PlayerConnection,
    sleepTimerEnabled: Boolean,
    sleepTimerTimeLeft: Long,
    isListenTogetherGuest: Boolean,
    isBluetoothConnected: Boolean,
    textColor: Color,
    onOpenQueue: () -> Unit,
    onToggleLyrics: () -> Unit,
    onShowSleepTimerDialog: () -> Unit,
    onShowAudioDevices: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 12.dp)
            .windowInsetsPadding(
                WindowInsets.systemBars
                    .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
            ),
    ) {
        TextButton(
            onClick = { onOpenQueue() },
            modifier = Modifier.wrapContentWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.apple_queue),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = textColor
                )
    //            Spacer(modifier = Modifier.width(6.dp))
    //            Text(
    //                text = stringResource(id = R.string.queue),
    //                color = textColor,
    //                maxLines = 1,
    //                overflow = TextOverflow.Ellipsis,
    //                textAlign = TextAlign.Center,
    //                modifier = Modifier.basicMarquee()
    //            )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            modifier = Modifier.width(120.dp)
        ) {
            ToggleButton(
                checked = false,
                onCheckedChange = {
                    onShowAudioDevices()
                },
                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f),
                colors = ToggleButtonDefaults.colors(
                    containerColor = textColor.copy(alpha = 0.2f),
                    contentColor = textColor,
                    checkedContainerColor = textColor.copy(alpha = 0.4f),
                    checkedContentColor = textColor
                )
            ) {
                Icon(
                    painter = painterResource(
                        if (isBluetoothConnected) R.drawable.headset_applemusic else R.drawable.speaker_apple
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            }

            ToggleButton(
                checked = sleepTimerEnabled,
                onCheckedChange = {
                    if (!isListenTogetherGuest) {
                        if (sleepTimerEnabled) {
                            playerConnection.service.sleepTimer.clear()
                        } else {
                            onShowSleepTimerDialog()
                        }
                    }
                },
                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f),
                colors = ToggleButtonDefaults.colors(
                    containerColor = textColor.copy(alpha = 0.2f),
                    contentColor = textColor,
                    checkedContainerColor = textColor.copy(alpha = 0.4f),
                    checkedContentColor = textColor
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sleep_timer),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                    if (sleepTimerEnabled) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = makeTimeString(sleepTimerTimeLeft),
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        TextButton(
            onClick = {
                onToggleLyrics()
            },
            modifier = Modifier.wrapContentWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.apple_music_me),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = textColor
                )
    //            Spacer(modifier = Modifier.width(6.dp))
    //            Text(
    //                text = stringResource(R.string.lyrics),
    //                color = textColor,
    //                maxLines = 1,
    //                overflow = TextOverflow.Ellipsis,
    //                textAlign = TextAlign.Center,
    //                modifier = Modifier.basicMarquee()
    //            )
            }
        }
    }
}
