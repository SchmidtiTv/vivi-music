package com.music.vivi.ui.player.fullscreen.apple

import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player.STATE_ENDED
import com.music.vivi.R
import com.music.vivi.constants.PlayerHorizontalPadding
import com.music.vivi.extensions.togglePlayPause
import com.music.vivi.playback.CastConnectionHandler
import com.music.vivi.playback.PlayerConnection
import com.music.vivi.ui.component.PlayerSliderTrack
import com.music.vivi.ui.component.ResizableIconButton
import com.music.vivi.vivimusic.isBuds
import com.music.vivi.vivimusic.isSpeaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColumnScope.ApplePlayerControls(
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
