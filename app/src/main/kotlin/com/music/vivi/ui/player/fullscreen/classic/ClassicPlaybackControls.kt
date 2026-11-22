/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen.classic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import com.music.vivi.constants.PlayerHorizontalPadding
import com.music.vivi.db.entities.Song
import com.music.vivi.extensions.togglePlayPause
import com.music.vivi.extensions.toggleRepeatMode
import com.music.vivi.playback.CastConnectionHandler
import com.music.vivi.playback.PlayerConnection
import com.music.vivi.R
import com.music.vivi.ui.component.ResizableIconButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun ClassicPlaybackControls(
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
    currentSong: Song?,
    repeatMode: Int,
    TextBackgroundColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = PlayerHorizontalPadding),
    ) {
//                            Box(modifier = Modifier.weight(1f)) {
//                                ResizableIconButton(
//                                    icon = when (repeatMode) {
//                                        Player.REPEAT_MODE_OFF, Player.REPEAT_MODE_ALL -> R.drawable.repeat
//                                        Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
//                                        else -> throw IllegalStateException()
//                                    },
//                                    color = TextBackgroundColor,
//                                    modifier = Modifier
//                                        .size(32.dp)
//                                        .padding(4.dp)
//                                        .align(Alignment.Center)
//                                        .alpha(if (isListenTogetherGuest) 0.5f else 1f),
//                                    enabled = !isListenTogetherGuest,
//                                    onClick = {
//                                        playerConnection.player.toggleRepeatMode()
//                                    }
//                                )
//                            }

                    Box(modifier = Modifier.weight(1f)) {
                        ResizableIconButton(
                            icon = R.drawable.ic_player_previous,
                            enabled = canSkipPrevious && !isListenTogetherGuest,
                            color = TextBackgroundColor,
                            modifier =
                        Modifier
                            .size(width = 42.dp, height = 30.dp)
                            .align(Alignment.Center)
                            .alpha(if (isListenTogetherGuest) 0.5f else 1f),
                            onClick = playerConnection::seekToPrevious,
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
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
                                }
                            ),
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
                                    R.drawable.ic_player_pause
                                } else {
                                    R.drawable.ic_player_play
                                },
                            ),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(TextBackgroundColor),
                            modifier =
                            Modifier
                                .align(Alignment.Center)
                                .size(44.dp),
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        ResizableIconButton(
                            icon = R.drawable.ic_player_next,
                            enabled = canSkipNext && !isListenTogetherGuest,
                            color = TextBackgroundColor,
                            modifier =
                        Modifier
                            .size(width = 42.dp, height = 30.dp)
                            .align(Alignment.Center)
                            .alpha(if (isListenTogetherGuest) 0.5f else 1f),
                            onClick = playerConnection::seekToNext,
                        )
                    }

//                            Box(modifier = Modifier.weight(1f)) {
//                                ResizableIconButton(
//                                    icon = if (currentSong?.song?.liked == true) R.drawable.favorite else R.drawable.favorite_border,
//                                    color = if (currentSong?.song?.liked == true) MaterialTheme.colorScheme.error else TextBackgroundColor,
//                                    modifier =
//                                    Modifier
//                                        .size(32.dp)
//                                        .padding(4.dp)
//                                        .align(Alignment.Center),
//                                    onClick = playerConnection::toggleLike,
//                                )
//                            }
                }
}
