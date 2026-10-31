/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen.m3e

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import com.music.vivi.LocalDatabase
import com.music.vivi.R
import com.music.vivi.constants.PlayerBackgroundStyle
import com.music.vivi.constants.PlayerHorizontalPadding
import com.music.vivi.db.entities.Song
import com.music.vivi.extensions.toggleRepeatMode
import com.music.vivi.models.MediaMetadata
import com.music.vivi.playback.CastConnectionHandler
import com.music.vivi.playback.ExoDownloadService
import com.music.vivi.playback.PlayerConnection
import com.music.vivi.ui.component.BottomSheetState
import com.music.vivi.ui.component.LocalBottomSheetPageState
import com.music.vivi.ui.component.LocalMenuState
import com.music.vivi.ui.menu.PlayerMenu
import com.music.vivi.ui.player.fullscreen.PlayerActionsArgs
import com.music.vivi.ui.player.fullscreen.PlayerControlsArgs
import com.music.vivi.ui.player.fullscreen.PlayerQueueBarArgs
import com.music.vivi.ui.player.fullscreen.PlayerTimeBadgeArgs
import com.music.vivi.ui.player.fullscreen.SlotPlayerTheme
import com.music.vivi.ui.utils.ShowMediaInfo
import com.music.vivi.ui.utils.ShowOffsetDialog
import com.music.vivi.utils.makeTimeString

object M3ePlayerTheme : SlotPlayerTheme {
    override val id = "m3e"
    override val nameRes = R.string.new_player_design
    override val controlsSpacing = 24.dp
    override val bottomSpacing = 30.dp
    override val solidSliderTrack = true
    override val growSliderWhileDragging = false
    override val hasTimeBadge = false

    @Composable
    override fun Actions(args: PlayerActionsArgs) {
        M3ePlayerActions(
            mediaMetadata = args.mediaMetadata,
            currentSong = args.currentSong,
            download = args.download,
            showInlineLyrics = args.showInlineLyrics,
            textButtonColor = args.textButtonColor,
            iconButtonColor = args.iconButtonColor,
            playerConnection = args.playerConnection,
            onToggleFullScreen = args.onToggleFullScreen,
        )
    }

    @Composable
    override fun TimeBadge(args: PlayerTimeBadgeArgs) {}

    @Composable
    override fun ColumnScope.Controls(args: PlayerControlsArgs) {
        M3ePlayerControls(
            playerConnection = args.playerConnection,
            castHandler = args.castHandler,
            isCasting = args.isCasting,
            castIsPlaying = args.castIsPlaying,
            playbackState = args.playbackState,
            effectiveIsPlaying = args.effectiveIsPlaying,
            isMuted = args.isMuted,
            isListenTogetherGuest = args.isListenTogetherGuest,
            canSkipPrevious = args.canSkipPrevious,
            canSkipNext = args.canSkipNext,
            textButtonColor = args.textButtonColor,
            iconButtonColor = args.iconButtonColor,
            sideButtonContainerColor = args.sideButtonContainerColor,
            sideButtonContentColor = args.sideButtonContentColor,
        )
    }

    @Composable
    override fun QueueBar(args: PlayerQueueBarArgs) {
        M3eQueueBar(
            mediaMetadata = args.mediaMetadata,
            playerConnection = args.playerConnection,
            navController = args.navController,
            playerBottomSheetState = args.playerBottomSheetState,
            repeatMode = args.repeatMode,
            sleepTimerEnabled = args.sleepTimerEnabled,
            sleepTimerTimeLeft = args.sleepTimerTimeLeft,
            isListenTogetherGuest = args.isListenTogetherGuest,
            showInlineLyrics = args.showInlineLyrics,
            showCommentButton = args.showCommentButton,
            showCommentSheet = args.showCommentSheet,
            playerBackground = args.playerBackground,
            textColor = args.textColor,
            textButtonColor = args.textButtonColor,
            iconButtonColor = args.iconButtonColor,
            onOpenQueue = args.onOpenQueue,
            onToggleLyrics = args.onToggleLyrics,
            onShowSleepTimerDialog = args.onShowSleepTimerDialog,
            onShowComments = args.onShowComments,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun M3ePlayerActions(
    mediaMetadata: MediaMetadata,
    currentSong: Song?,
    download: Download?,
    showInlineLyrics: Boolean,
    textButtonColor: Color,
    iconButtonColor: Color,
    playerConnection: PlayerConnection,
    onToggleFullScreen: () -> Unit,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current

    val shareShape = RoundedCornerShape(
        topStart = 50.dp, bottomStart = 50.dp,
        topEnd = 3.dp, bottomEnd = 3.dp
    )

    val favShape = RoundedCornerShape(
        topStart = 3.dp, bottomStart = 3.dp,
        topEnd = 50.dp, bottomEnd = 50.dp
    )

    val middleShape = RoundedCornerShape(3.dp)

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(targetState = showInlineLyrics, label = "DownloadButton") { showLyrics ->
            if (showLyrics) {
                FilledIconButton(
                    onClick = { onToggleFullScreen() },
                    shape = shareShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = textButtonColor,
                        contentColor = iconButtonColor,
                    ),
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.fullscreen),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                FilledIconButton(
                    onClick = {
                        mediaMetadata?.let { meta ->
                            when (download?.state) {
                                Download.STATE_COMPLETED, Download.STATE_QUEUED, Download.STATE_DOWNLOADING -> {
                                    DownloadService.sendRemoveDownload(
                                        context,
                                        ExoDownloadService::class.java,
                                        meta.id,
                                        false,
                                    )
                                }
                                else -> {
                                    database.transaction {
                                        insert(meta)
                                    }
                                    val downloadRequest =
                                        DownloadRequest
                                            .Builder(meta.id, meta.id.toUri())
                                            .setCustomCacheKey(meta.id)
                                            .setData(meta.title.toByteArray())
                                            .build()
                                    DownloadService.sendAddDownload(
                                        context,
                                        ExoDownloadService::class.java,
                                        downloadRequest,
                                        false,
                                    )
                                }
                            }
                        }
                    },
                    shape = shareShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = textButtonColor,
                        contentColor = iconButtonColor,
                    ),
                    modifier = Modifier.size(42.dp),
                ) {
                    when (download?.state) {
                        Download.STATE_COMPLETED -> {
                            Icon(
                                painter = painterResource(R.drawable.offline),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Download.STATE_QUEUED, Download.STATE_DOWNLOADING -> {
                            CircularWavyProgressIndicator(
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        else -> {
                            Icon(
                                painter = painterResource(R.drawable.download),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        AnimatedContent(targetState = showInlineLyrics, label = "LikeButton") { showLyrics ->
            if (showLyrics) {
                val currentLyrics by playerConnection.currentLyrics.collectAsState(initial = null)
                FilledIconButton(
                    onClick = {
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
                    shape = favShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = textButtonColor,
                        contentColor = iconButtonColor,
                    ),
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_horiz),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                FilledIconButton(
                    onClick = playerConnection::toggleLike,
                    shape = favShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = textButtonColor,
                        contentColor = iconButtonColor,
                    ),
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        painter = painterResource(
                            if (currentSong?.song?.liked == true)
                                R.drawable.favorite
                            else R.drawable.favorite_border
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun M3ePlayerControls(
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun M3eQueueBar(
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

@Composable
private fun PlayerQueueButton(
    icon: Int,
    onClick: () -> Unit,
    isActive: Boolean,
    enabled: Boolean = true,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    text: String? = null,
    textButtonColor: Color,
    iconButtonColor: Color,
    iconSize: androidx.compose.ui.unit.Dp,
    textBackgroundColor: Color,
    playerBackground: PlayerBackgroundStyle
) {
    val buttonModifier = Modifier
        .clip(shape)
        .clickable(enabled = enabled, onClick = onClick)

    val alphaFactor = if (enabled) 1f else 0.35f

    val appliedModifier = if (isActive) {
        modifier.then(buttonModifier.background(textButtonColor)).alpha(alphaFactor)
    } else {
        modifier.then(
            buttonModifier.border(
                width = 1.dp,
                color = textButtonColor.copy(alpha = 0.3f),
                shape = shape
            )
        ).alpha(alphaFactor)
    }

    Box(
        modifier = appliedModifier,
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                color = iconButtonColor.copy(alpha = if (enabled) 1f else 0.6f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee()
            )
        } else {
            val baseTint = if (isActive) {
                iconButtonColor
            } else {
                when (playerBackground) {
                    PlayerBackgroundStyle.BLUR, PlayerBackgroundStyle.GRADIENT, PlayerBackgroundStyle.GLOW_ANIMATED, PlayerBackgroundStyle.APPLE_MUSIC, PlayerBackgroundStyle.LIVE_MESH ->
                        Color.White
                    PlayerBackgroundStyle.DEFAULT ->
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                }
            }
            val finalTint = if (enabled) baseTint else baseTint.copy(alpha = 0.5f)
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = finalTint
            )
        }
    }
}
