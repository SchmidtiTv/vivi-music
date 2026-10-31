package com.music.vivi.ui.player.fullscreen.m3e

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.navigation.NavController
import com.music.vivi.R
import com.music.vivi.constants.PlayerBackgroundStyle
import com.music.vivi.extensions.toggleRepeatMode
import com.music.vivi.models.MediaMetadata
import com.music.vivi.playback.PlayerConnection
import com.music.vivi.ui.component.BottomSheetState
import com.music.vivi.ui.component.LocalBottomSheetPageState
import com.music.vivi.ui.component.LocalMenuState
import com.music.vivi.ui.menu.PlayerMenu
import com.music.vivi.ui.utils.ShowMediaInfo
import com.music.vivi.utils.makeTimeString


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun M3eQueueBar(
    mediaMetadata: MediaMetadata?,
    playerConnection: PlayerConnection,
    navController: NavController,
    playerBottomSheetState: BottomSheetState,
    repeatMode: Int,
    sleepTimerEnabled: Boolean,
    sleepTimerTimeLeft: Long,
    isListenTogetherGuest: Boolean,
    showInlineLyrics: Boolean,
    showCommentButton: Boolean,
    showCommentSheet: Boolean,
    playerBackground: PlayerBackgroundStyle,
    textColor: Color,
    textButtonColor: Color,
    iconButtonColor: Color,
    onOpenQueue: () -> Unit,
    onToggleLyrics: () -> Unit,
    onShowSleepTimerDialog: () -> Unit,
    onShowComments: () -> Unit,
) {
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 12.dp)
            .windowInsetsPadding(
                WindowInsets.systemBars.only(
                    WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                ),
            ),
    ) {
        val buttonSize = 42.dp
        val iconSize = 24.dp
        val queueShape = RoundedCornerShape(
            topStart = 50.dp, bottomStart = 50.dp,
            topEnd = 3.dp, bottomEnd = 3.dp
        )
        val middleShape = RoundedCornerShape(3.dp)
        val repeatShape = RoundedCornerShape(
            topStart = 3.dp, bottomStart = 3.dp,
            topEnd = 50.dp, bottomEnd = 50.dp
        )

        //    Spacer(modifier = Modifier.weight(1f))

        PlayerQueueButton(
            icon = R.drawable.queue_music,
            onClick = { onOpenQueue() },
            isActive = false,
            shape = queueShape,
            modifier = Modifier.size(buttonSize),
            textButtonColor = textButtonColor,
            iconButtonColor = iconButtonColor,
            iconSize = iconSize,
            textBackgroundColor = textColor,
            playerBackground = playerBackground
        )

        PlayerQueueButton(
            icon = R.drawable.bedtime,
            onClick = {
                if (sleepTimerEnabled) {
                    playerConnection.service.sleepTimer.clear()
                } else {
                    onShowSleepTimerDialog()
                }
            },
            isActive = sleepTimerEnabled,
            enabled = !isListenTogetherGuest,
            shape = middleShape,
            modifier = Modifier.size(buttonSize),
            textButtonColor = textButtonColor,
            iconButtonColor = iconButtonColor,
            text = if (sleepTimerEnabled) makeTimeString(sleepTimerTimeLeft) else null,
            iconSize = iconSize,
            textBackgroundColor = textColor,
            playerBackground = playerBackground
        )

        PlayerQueueButton(
            icon = R.drawable.lyrics,
            onClick = { onToggleLyrics() },
            isActive = showInlineLyrics,
            shape = middleShape,
            modifier = Modifier.size(buttonSize),
            textButtonColor = textButtonColor,
            iconButtonColor = iconButtonColor,
            iconSize = iconSize,
            textBackgroundColor = textColor,
            playerBackground = playerBackground
        )

        if (showCommentButton) {
            PlayerQueueButton(
                icon = R.drawable.chat_msg,
                onClick = { onShowComments() },
                isActive = showCommentSheet,
                shape = middleShape,
                modifier = Modifier.size(buttonSize),
                textButtonColor = textButtonColor,
                iconButtonColor = iconButtonColor,
                iconSize = iconSize,
                textBackgroundColor = textColor,
                playerBackground = playerBackground
            )
        }

        val shuffleModeEnabledInside by playerConnection.shuffleModeEnabled.collectAsState()
        PlayerQueueButton(
            icon = R.drawable.shuffle,
            onClick = {
                playerConnection.player.shuffleModeEnabled = !shuffleModeEnabledInside
            },
            isActive = shuffleModeEnabledInside,
            enabled = !isListenTogetherGuest,
            shape = middleShape,
            modifier = Modifier.size(buttonSize),
            textButtonColor = textButtonColor,
            iconButtonColor = iconButtonColor,
            iconSize = iconSize,
            textBackgroundColor = textColor,
            playerBackground = playerBackground
        )


        PlayerQueueButton(
            icon = when (repeatMode) {
                Player.REPEAT_MODE_ALL -> R.drawable.repeat
                Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                else -> R.drawable.repeat
            },
            onClick = {
                playerConnection.player.toggleRepeatMode()
            },
            isActive = repeatMode != Player.REPEAT_MODE_OFF,
            enabled = !isListenTogetherGuest,
            shape = repeatShape,
            modifier = Modifier.size(buttonSize),
            textButtonColor = textButtonColor,
            iconButtonColor = iconButtonColor,
            iconSize = iconSize,
            textBackgroundColor = textColor,
            playerBackground = playerBackground
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(textButtonColor)
                .clickable {
                    menuState.show {
                        PlayerMenu(
                            mediaMetadata = mediaMetadata,
                            navController = navController,
                            playerBottomSheetState = playerBottomSheetState,
                            onShowDetailsDialog = {
                                mediaMetadata?.id?.let {
                                    bottomSheetPageState.show {
                                        ShowMediaInfo(it)
                                    }
                                }
                            },
                            onDismiss = menuState::dismiss
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.more_vert),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = iconButtonColor
            )
        }
    }
}