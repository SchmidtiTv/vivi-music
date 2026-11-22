/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen.m3e

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player.STATE_ENDED
import com.music.vivi.constants.PlayerHorizontalPadding
import com.music.vivi.extensions.togglePlayPause
import com.music.vivi.playback.CastConnectionHandler
import com.music.vivi.playback.PlayerConnection
import com.music.vivi.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun M3ePlaybackControls(
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

        val playPauseWeight by animateFloatAsState(
            targetValue = if (isPlayPausePressed) 2.0f else 1.7f,
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = 400f
            ),
            label = "playPauseWeight"
        )

        val backButtonWeight by animateFloatAsState(
            targetValue = if (isPlayPausePressed) 0.45f else 0.6f,
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = 400f
            ),
            label = "backButtonWeight"
        )

        val nextButtonWeight by animateFloatAsState(
            targetValue = if (isPlayPausePressed) 0.45f else 0.6f,
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = 400f
            ),
            label = "nextButtonWeight"
        )

        val controlHeight = 68.dp
        val sideShape = remember { RoundedCornerShape(controlHeight / 2) }
        val playCornerRadius by animateDpAsState(
            targetValue = if (effectiveIsPlaying) 20.dp else controlHeight / 2,
            animationSpec = spring(
                dampingRatio = 0.55f,
                stiffness = 380f
            ),
            label = "playCornerRadius"
        )
        val playShape = RoundedCornerShape(playCornerRadius)

        val sideColors = ButtonDefaults.filledTonalButtonColors(
            containerColor = sideButtonContainerColor,
            contentColor = sideButtonContentColor,
        )
        val centerColors = ButtonDefaults.filledTonalButtonColors(
            containerColor = textButtonColor,
            contentColor = iconButtonColor,
        )

        FilledTonalButton(
            onClick = playerConnection::seekToPrevious,
            enabled = canSkipPrevious && !isListenTogetherGuest,
            shape = sideShape,
            interactionSource = backInteractionSource,
            colors = sideColors,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .height(controlHeight)
                .weight(backButtonWeight)
        ) {
            Icon(
                painter = painterResource(R.drawable.skip_previous),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        FilledTonalButton(
            onClick = {
                if (isListenTogetherGuest) {
                    playerConnection.toggleMute()
                    return@FilledTonalButton
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
            shape = playShape,
            interactionSource = playPauseInteractionSource,
            colors = centerColors,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .height(controlHeight)
                .weight(playPauseWeight)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val playIconRes = if (isListenTogetherGuest) {
                    if (isMuted) R.drawable.volume_off else R.drawable.volume_up
                } else {
                    if (effectiveIsPlaying) R.drawable.pause else R.drawable.play
                }
                AnimatedContent(
                    targetState = playIconRes,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.72f, animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f)))
                            .togetherWith(fadeOut(animationSpec = tween(120)) + scaleOut(targetScale = 0.72f, animationSpec = tween(120)))
                    },
                    contentAlignment = Alignment.Center,
                    label = "playIconAnimation"
                ) { iconRes ->
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = if (isListenTogetherGuest) {
                            if (isMuted) stringResource(R.string.unmute) else stringResource(R.string.mute)
                        } else {
                            if (effectiveIsPlaying) stringResource(R.string.pause) else stringResource(R.string.play)
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                val playPauseText = if (isListenTogetherGuest) {
                    if (isMuted) stringResource(R.string.unmute) else stringResource(R.string.mute)
                } else {
                    if (effectiveIsPlaying) stringResource(R.string.pause) else stringResource(R.string.play)
                }
                AnimatedContent(
                    targetState = playPauseText,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)))
                            .togetherWith(fadeOut(animationSpec = tween(120)) + scaleOut(targetScale = 0.85f, animationSpec = tween(120)))
                    },
                    contentAlignment = Alignment.Center,
                    label = "playPauseLabel"
                ) { labelText ->
                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        FilledTonalButton(
            onClick = playerConnection::seekToNext,
            enabled = canSkipNext && !isListenTogetherGuest,
            shape = sideShape,
            interactionSource = nextInteractionSource,
            colors = sideColors,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .height(controlHeight)
                .weight(nextButtonWeight)
        ) {
            Icon(
                painter = painterResource(R.drawable.skip_next),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
