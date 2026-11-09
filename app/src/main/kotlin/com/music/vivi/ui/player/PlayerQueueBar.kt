/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.navigation.NavController
import com.music.vivi.constants.PlayerBackgroundStyle
import com.music.vivi.constants.ShowCommentButtonKey
import com.music.vivi.extensions.toggleRepeatMode
import com.music.vivi.listentogether.RoomRole
import com.music.vivi.LocalListenTogetherManager
import com.music.vivi.LocalPlayerConnection
import com.music.vivi.R
import com.music.vivi.ui.component.ActionPromptDialog
import com.music.vivi.ui.component.BottomSheetState
import com.music.vivi.ui.component.LocalBottomSheetPageState
import com.music.vivi.ui.component.LocalMenuState
import com.music.vivi.ui.menu.PlayerMenu
import com.music.vivi.ui.player.fullscreen.rememberUseNewPlayerDesign
import com.music.vivi.ui.screens.CommentSheet
import com.music.vivi.ui.utils.ShowMediaInfo
import com.music.vivi.utils.makeTimeString
import com.music.vivi.utils.rememberPreference
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerQueueBar(
    navController: NavController,
    playerBottomSheetState: BottomSheetState,
    textColor: Color,
    textButtonColor: Color,
    iconButtonColor: Color,
    showInlineLyrics: Boolean,
    playerBackground: PlayerBackgroundStyle,
    onToggleLyrics: () -> Unit,
    onOpenQueue: () -> Unit,
) {
    val TextBackgroundColor = textColor
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current

    val listenTogetherManager = LocalListenTogetherManager.current
    val listenTogetherRoleState = listenTogetherManager?.role?.collectAsState(initial = RoomRole.NONE)
    val isListenTogetherGuest = listenTogetherRoleState?.value == RoomRole.GUEST

    val playerConnection = LocalPlayerConnection.current ?: return
    val repeatMode by playerConnection.repeatMode.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val useNewPlayerDesign = rememberUseNewPlayerDesign()
    val (showCommentButton) = rememberPreference(
        ShowCommentButtonKey,
        defaultValue = true
    )

    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var sleepTimerValue by remember { mutableFloatStateOf(30f) }
    val sleepTimerEnabled = remember(
        playerConnection.service.sleepTimer.triggerTime,
        playerConnection.service.sleepTimer.pauseWhenSongEnd
    ) {
        playerConnection.service.sleepTimer.isActive
    }
    var sleepTimerTimeLeft by remember { mutableLongStateOf(0L) }
    var showCommentSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(sleepTimerEnabled) {
        if (sleepTimerEnabled) {
            while (isActive) {
                sleepTimerTimeLeft = if (playerConnection.service.sleepTimer.pauseWhenSongEnd) {
                    playerConnection.player.duration - playerConnection.player.currentPosition
                } else {
                    playerConnection.service.sleepTimer.triggerTime - System.currentTimeMillis()
                }
                delay(1000L)
            }
        }
    }

    if (useNewPlayerDesign) {
        // New design
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

//                    Spacer(modifier = Modifier.weight(1f))

            PlayerQueueButton(
                icon = R.drawable.queue_music,
                onClick = { onOpenQueue() },
                isActive = false,
                shape = queueShape,
                modifier = Modifier.size(buttonSize),
                textButtonColor = textButtonColor,
                iconButtonColor = iconButtonColor,
                iconSize = iconSize,
                textBackgroundColor = TextBackgroundColor,
                playerBackground = playerBackground
            )

            PlayerQueueButton(
                icon = R.drawable.timer,
                onClick = {
                    if (sleepTimerEnabled) {
                        playerConnection.service.sleepTimer.clear()
                    } else {
                        showSleepTimerDialog = true
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
                textBackgroundColor = TextBackgroundColor,
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
                textBackgroundColor = TextBackgroundColor,
                playerBackground = playerBackground
            )

            if (showCommentButton) {
                PlayerQueueButton(
                    icon = R.drawable.chat_msg,
                    onClick = { showCommentSheet = true },
                    isActive = showCommentSheet,
                    shape = middleShape,
                    modifier = Modifier.size(buttonSize),
                    textButtonColor = textButtonColor,
                    iconButtonColor = iconButtonColor,
                    iconSize = iconSize,
                    textBackgroundColor = TextBackgroundColor,
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
                textBackgroundColor = TextBackgroundColor,
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
                textBackgroundColor = TextBackgroundColor,
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

    if (showSleepTimerDialog) {
        ActionPromptDialog(
            titleBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.sleep_timer),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            },
            onDismiss = { showSleepTimerDialog = false },
            onConfirm = {
                showSleepTimerDialog = false
                playerConnection.service.sleepTimer.start(sleepTimerValue.roundToInt())
            },
            onCancel = {
                showSleepTimerDialog = false
            },
            onReset = {
                sleepTimerValue = 30f // Default value
            },
            content = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.minute,
                            sleepTimerValue.roundToInt(),
                            sleepTimerValue.roundToInt()
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Spacer(Modifier.height(16.dp))

                    Slider(
                        value = sleepTimerValue,
                        onValueChange = { sleepTimerValue = it },
                        valueRange = 5f..120f,
                        steps = (120 - 5) / 5 - 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            showSleepTimerDialog = false
                            playerConnection.service.sleepTimer.start(-1)
                        }
                    ) {
                        Text(stringResource(R.string.end_of_song))
                    }
                }
            }
        )
    }

    if (showCommentSheet) {
        CommentSheet(
            videoId = mediaMetadata?.id ?: "",
            onDismiss = { showCommentSheet = false }
        )
    }
}
