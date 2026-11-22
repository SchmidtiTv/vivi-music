/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen.classic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.vivi.constants.PlayerHorizontalPadding
import com.music.vivi.R
import com.music.vivi.ui.component.BottomSheetState
import com.music.vivi.ui.component.Lyrics
import com.music.vivi.vivimusic.isBuds
import com.music.vivi.vivimusic.isSpeaker
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun ClassicBottomActions(
    bluetoothDeviceName: String?,
    nameToShow: String?,
    queueSheetState: BottomSheetState,
    showInlineLyrics: Boolean,
    showInlineQueue: Boolean,
    useNewPlayerDesign: Boolean,
    textButtonColor: Color,
    onShowInlineLyricsChange: (Boolean) -> Unit,
    onShowInlineQueueChange: (Boolean) -> Unit,
    onShowAudioDevices: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PlayerHorizontalPadding)
            .padding(bottom = 12.dp)
    ) {
                            // Left: Lyrics
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            val next = !showInlineLyrics
                                            onShowInlineLyricsChange(next)
                                            if (next) onShowInlineQueueChange(false)
                                        }
                                    )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.lyrics_apple),
                                    contentDescription = stringResource(R.string.lyrics),
                                    tint = if (showInlineLyrics) textButtonColor else textButtonColor.copy(alpha = 0.5f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Center: Audio Device / Routing
                            val isConnected = bluetoothDeviceName != null
                            val tintColor = if (isConnected) textButtonColor else textButtonColor.copy(alpha = 0.5f)
                            val label = if (isConnected) {
                                nameToShow ?: "Bluetooth"
                            } else {
                                stringResource(R.string.player_audio_this_phone)
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .widthIn(max = 160.dp)
                                    .padding(horizontal = 4.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onShowAudioDevices() }
                                    )
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            if (isConnected) {
                                                when {
                                                    isSpeaker(nameToShow) -> R.drawable.speaker_applemusic
                                                    isBuds(nameToShow) -> R.drawable.headphones_apple
                                                    else -> R.drawable.headphones_apple
                                                }
                                            } else {
                                                R.drawable.airplay_audio
                                            }
                                        ),
                                        contentDescription = label,
                                        tint = tintColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                    color = tintColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Right: Queue
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            if (useNewPlayerDesign) {
                                                queueSheetState.expandSoft()
                                            } else {
                                                val next = !showInlineQueue
                                                onShowInlineQueueChange(next)
                                                if (next) onShowInlineLyricsChange(false)
                                            }
                                        }
                                    )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.apple_queue),
                                    contentDescription = stringResource(R.string.queue),
                                    tint = if (if (useNewPlayerDesign) queueSheetState.isExpanded else showInlineQueue) textButtonColor else textButtonColor.copy(alpha = 0.5f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
}
