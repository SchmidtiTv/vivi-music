package com.music.vivi.ui.player.fullscreen.m3e

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player.STATE_ENDED
import com.music.vivi.R
import com.music.vivi.constants.PlayerHorizontalPadding
import com.music.vivi.playback.CastConnectionHandler
import com.music.vivi.playback.PlayerConnection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun M3ePlayerControls(
    playerConnection: PlayerConnection,
    castHandler: CastConnectionHandler?,
    isCasting: Boolean,
    castIsPlaying: Boolean,
    playbackState: Int,
    effectiveIsPlaying: Boolean,
    isMuted: Boolean,
    isListenTogetherGuest: Boolean,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    textButtonColor: Color,
    iconButtonColor: Color,
    sideButtonContainerColor: Color,
    sideButtonContentColor: Color,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PlayerHorizontalPadding)
    ) {
        val backInteractionSource = remember { MutableInteractionSource() }
        val nextInteractionSource = remember { MutableInteractionSource() }
        val playPauseInteractionSource = remember { MutableInteractionSource() }

        val isPlayPausePressed by playPauseInteractionSource.collectIsPressedAsState()
        val isBackPressed by backInteractionSource.collectIsPressedAsState()
        val isNextPressed by nextInteractionSource.collectIsPressedAsState()

        val playPauseWeight by animateFloatAsState(
            targetValue = if (isPlayPausePressed) 1.9f else if (isBackPressed || isNextPressed) 1.1f else 1.3f,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 500f
            ),
            label = "playPauseWeight"
        )

        val backButtonWeight by animateFloatAsState(
            targetValue = if (isBackPressed) 0.65f else if (isPlayPausePressed) 0.35f else 0.45f,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 500f
            ),
            label = "backButtonWeight"
        )

        val nextButtonWeight by animateFloatAsState(
            targetValue = if (isNextPressed) 0.65f else if (isPlayPausePressed) 0.35f else 0.45f,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 500f
            ),
            label = "nextButtonWeight"
        )

        FilledIconButton(
            onClick = playerConnection::seekToPrevious,
            enabled = canSkipPrevious && !isListenTogetherGuest,
            shape = RoundedCornerShape(50),
            interactionSource = backInteractionSource,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = sideButtonContainerColor,
                contentColor = sideButtonContentColor,
            ),
            modifier = Modifier
                .height(68.dp)
                .weight(backButtonWeight)
        ) {
            Icon(
                painter = painterResource(R.drawable.skip_previous),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        FilledIconButton(
            onClick = {
                if (isListenTogetherGuest) {
                    playerConnection.toggleMute()
                    return@FilledIconButton
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
                    playerConnection.togglePlayPause()
                }
            },
            shape = RoundedCornerShape(50),
            interactionSource = playPauseInteractionSource,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = textButtonColor,
                contentColor = iconButtonColor,
            ),
            modifier = Modifier
                .height(68.dp)
                .weight(playPauseWeight)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(
                        if (isListenTogetherGuest) {
                            if (isMuted) R.drawable.volume_off else R.drawable.volume_up
                        } else {
                            if (effectiveIsPlaying) R.drawable.pause else R.drawable.play
                        }
                    ),
                    contentDescription = if (isListenTogetherGuest) {
                        if (isMuted) stringResource(R.string.unmute) else stringResource(R.string.mute)
                    } else {
                        if (effectiveIsPlaying) stringResource(R.string.pause) else stringResource(R.string.play)
                    },
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isListenTogetherGuest) {
                        if (isMuted) stringResource(R.string.unmute) else stringResource(R.string.mute)
                    } else {
                        if (effectiveIsPlaying) stringResource(R.string.pause) else stringResource(R.string.play)
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        FilledIconButton(
            onClick = playerConnection::seekToNext,
            enabled = canSkipNext && !isListenTogetherGuest,
            shape = RoundedCornerShape(50),
            interactionSource = nextInteractionSource,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = sideButtonContainerColor,
                contentColor = sideButtonContentColor,
            ),
            modifier = Modifier
                .height(68.dp)
                .weight(nextButtonWeight
                )
        ) {
            Icon(
                painter = painterResource(R.drawable.skip_next),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}