package com.music.vivi.ui.player.fullscreen.apple

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.music.vivi.R
import com.music.vivi.playback.PlayerConnection
import com.music.vivi.utils.makeTimeString


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppleQueueBar(
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
            }
        }
    }
}
