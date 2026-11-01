/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.mini.classic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.music.vivi.LocalListenTogetherManager
import com.music.vivi.LocalPlayerConnection
import com.music.vivi.R
import com.music.vivi.constants.CropAlbumArtKey
import com.music.vivi.constants.MiniPlayerHeight
import com.music.vivi.constants.PureBlackMiniPlayerKey
import com.music.vivi.constants.SwipeSensitivityKey
import com.music.vivi.constants.SwipeThumbnailKey
import com.music.vivi.constants.ThumbnailCornerRadius
import com.music.vivi.listentogether.ListenTogetherManager
import com.music.vivi.models.MediaMetadata
import com.music.vivi.playback.CastConnectionHandler
import com.music.vivi.playback.PlayerConnection
import com.music.vivi.ui.player.MiniPlayerLayout
import com.music.vivi.ui.player.ProgressState
import com.music.vivi.ui.player.RailMiniPlayerWidth
import com.music.vivi.ui.player.mini.MiniPlayerTheme
import com.music.vivi.ui.player.miniPlayerClick
import com.music.vivi.ui.utils.LocalAdaptiveLayout
import com.music.vivi.ui.utils.resize
import com.music.vivi.utils.rememberPreference
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import timber.log.Timber

object ClassicMiniPlayerTheme : MiniPlayerTheme {
    override val id = "classic"
    override val nameRes = R.string.classic_mini_player
    override val floating = false
    override val transparentBottomInset = false

    @Composable
    override fun Content(
        progressState: ProgressState,
        modifier: Modifier,
        vertical: Boolean,
        onClick: (() -> Unit)?,
    ) {
        if (vertical) {
            ClassicMiniPlayer(
                progressState = progressState,
                modifier = modifier,
                vertical = true,
                onClick = onClick,
            )
        } else {
            Box(modifier = modifier.fillMaxWidth()) {
                ClassicMiniPlayer(
                    progressState = progressState,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun ClassicMiniPlayer(
    progressState: ProgressState,
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val pureBlack by rememberPreference(PureBlackMiniPlayerKey, defaultValue = false)
    
    val playbackState by playerConnection.playbackState.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    
    val castHandler = remember(playerConnection) {
        try {
            playerConnection.service.castConnectionHandler
        } catch (e: Exception) {
            null
        }
    }
    val isCasting by castHandler?.isCasting?.collectAsState() ?: remember { mutableStateOf(false) }

    val swipeSensitivity by rememberPreference(SwipeSensitivityKey, 0.73f)
    val swipeThumbnailPref by rememberPreference(SwipeThumbnailKey, true)
    
    // Disable swipe for Listen Together guests
    val listenTogetherManager = LocalListenTogetherManager.current
    val isListenTogetherGuest = listenTogetherManager?.let { it.isInRoom && !it.isHost } ?: false
    val swipeThumbnail = swipeThumbnailPref && !isListenTogetherGuest

    val layoutDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()
    
    val constrainWidth = LocalAdaptiveLayout.current.constrainMiniPlayer

    val offsetXAnimatable = remember { Animatable(0f) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var totalDragDistance by remember { mutableFloatStateOf(0f) }

    val animationSpec = remember {
        spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
    }

    val autoSwipeThreshold = remember(swipeSensitivity) {
        (600 / (1f + kotlin.math.exp(-(-11.44748 * swipeSensitivity + 9.04945)))).roundToInt()
    }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val backgroundColor = if (pureBlack && isSystemInDarkTheme()) Color.Black else MaterialTheme.colorScheme.surfaceContainer

    val progressBar: @Composable BoxScope.() -> Unit = {
        // Progress bar - uses drawWithContent to avoid recomposition
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter)
                .drawWithContent {
                    val progress = progressState.progress
                    drawRect(trackColor)
                    drawRect(primaryColor, size = Size(size.width * progress, size.height))
                }
        )
    }

    val layout: @Composable (Modifier) -> Unit = { layoutModifier ->
        MiniPlayerLayout(
            vertical = vertical,
            modifier = layoutModifier,
            contentPadding = PaddingValues(start = 6.dp, end = 12.dp),
            artworkGap = 0.dp,
            actionsGap = 6.dp,
            artwork = {
                mediaMetadata?.let { ClassicMiniArtwork(mediaMetadata = it, pureBlack = pureBlack) }
            },
            info = { infoModifier ->
                mediaMetadata?.let { ClassicMiniText(mediaMetadata = it, centered = vertical, modifier = infoModifier) }
            },
            actions = {
                ClassicPlayPauseButton(
                    playbackState = playbackState,
                    isCasting = isCasting,
                    castHandler = castHandler,
                    playerConnection = playerConnection,
                    listenTogetherManager = listenTogetherManager
                )

                IconButton(
                    enabled = canSkipNext && !isListenTogetherGuest,
                    onClick = if (isListenTogetherGuest) ({}) else ({ playerConnection.seekToNext() }),
                ) {
                    Icon(painter = painterResource(R.drawable.skip_next), contentDescription = null)
                }
            },
        )
    }

    if (vertical) {
        Box(
            modifier = modifier
                .width(RailMiniPlayerWidth)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .miniPlayerClick(onClick)
        ) {
            progressBar()
            layout(Modifier)
        }
        return
    }

    Box(
        modifier = modifier
            .then(if (constrainWidth) Modifier.width(500.dp) else Modifier.fillMaxWidth())
            .height(MiniPlayerHeight)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(backgroundColor)
            .let { baseModifier ->
                if (swipeThumbnail) {
                    baseModifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                dragStartTime = System.currentTimeMillis()
                                totalDragDistance = 0f
                            },
                            onDragCancel = {
                                coroutineScope.launch { offsetXAnimatable.animateTo(0f, animationSpec) }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val adjustedDragAmount =
                                    if (layoutDirection == LayoutDirection.Rtl) -dragAmount else dragAmount
                                val canSkipPrevious = playerConnection.player.previousMediaItemIndex != -1
                                val canSkipNext = playerConnection.player.nextMediaItemIndex != -1
                                val tryingToSwipeRight = adjustedDragAmount > 0
                                val tryingToSwipeLeft = adjustedDragAmount < 0
                                val allowLeft = tryingToSwipeLeft && canSkipNext
                                val allowRight = tryingToSwipeRight && canSkipPrevious

                                val canReturnToCenter =
                                    (tryingToSwipeRight && !canSkipPrevious && offsetXAnimatable.value < 0) ||
                                            (tryingToSwipeLeft && !canSkipNext && offsetXAnimatable.value > 0)

                                if (allowLeft || allowRight || canReturnToCenter) {
                                    totalDragDistance += kotlin.math.abs(adjustedDragAmount)
                                    coroutineScope.launch {
                                        offsetXAnimatable.snapTo(offsetXAnimatable.value + adjustedDragAmount)
                                    }
                                }
                            },
                            onDragEnd = {
                                val dragDuration = System.currentTimeMillis() - dragStartTime
                                val velocity = if (dragDuration > 0) totalDragDistance / dragDuration else 0f
                                val currentOffset = offsetXAnimatable.value
                                val minDistanceThreshold = 50f
                                val velocityThreshold = (swipeSensitivity * -8.25f) + 8.5f

                                val shouldChangeSong = (kotlin.math.abs(currentOffset) > minDistanceThreshold && velocity > velocityThreshold) ||
                                    (kotlin.math.abs(currentOffset) > autoSwipeThreshold)

                                if (shouldChangeSong) {
                                    if (currentOffset > 0 && canSkipPrevious) {
                                        if (!playerConnection.service.manualSkipToPreviousWithCrossfade()) {
                                            playerConnection.player.seekToPreviousMediaItem()
                                        }
                                    } else if (currentOffset <= 0 && canSkipNext) {
                                        if (!playerConnection.service.manualSkipToNextWithCrossfade()) {
                                            playerConnection.player.seekToNext()
                                        }
                                    }
                                }
                                coroutineScope.launch { offsetXAnimatable.animateTo(0f, animationSpec) }
                            }
                        )
                    }
                } else baseModifier
            }
    ) {
        progressBar()
        layout(Modifier.offset { IntOffset(offsetXAnimatable.value.roundToInt(), 0) })

        // Swipe indicator
        if (offsetXAnimatable.value.absoluteValue > 50f) {
            Box(
                modifier = Modifier
                    .align(if (offsetXAnimatable.value > 0) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (offsetXAnimatable.value > 0) R.drawable.skip_previous else R.drawable.skip_next
                    ),
                    contentDescription = null,
                    tint = primaryColor.copy(
                        alpha = (offsetXAnimatable.value.absoluteValue / autoSwipeThreshold).coerceIn(0f, 1f)
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ClassicPlayPauseButton(
    playbackState: Int,
    isCasting: Boolean,
    castHandler: CastConnectionHandler?,
    playerConnection: PlayerConnection,
    listenTogetherManager: ListenTogetherManager?
) {
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val castIsPlaying by castHandler?.castIsPlaying?.collectAsState() ?: remember { mutableStateOf(false) }
    val effectiveIsPlaying = if (isCasting) castIsPlaying else isPlaying
    val isListenTogetherGuest = listenTogetherManager?.let { it.isInRoom && !it.isHost } ?: false
    val isMuted by playerConnection.isMuted.collectAsState()


    IconButton(
        onClick = {
            if (isListenTogetherGuest) {
                playerConnection.toggleMute()
                return@IconButton
            }
            if (isCasting) {
                if (castIsPlaying) castHandler?.pause() else castHandler?.play()
            } else if (playbackState == Player.STATE_ENDED) {
                playerConnection.player.seekTo(0, 0)
                playerConnection.player.playWhenReady = true
            } else {
                playerConnection.togglePlayPause()
            }
        },
    ) {
        Icon(
            painter = painterResource(
                when {
                    isListenTogetherGuest -> if (isMuted) R.drawable.volume_off else R.drawable.volume_up
                    playbackState == Player.STATE_ENDED -> R.drawable.replay
                    effectiveIsPlaying -> R.drawable.pause
                    else -> R.drawable.play
                }
            ),
            contentDescription = null,
        )
    }
}

@Composable
private fun ClassicMiniArtwork(
    mediaMetadata: MediaMetadata,
    pureBlack: Boolean,
) {
    val error by LocalPlayerConnection.current?.error?.collectAsState() ?: remember { mutableStateOf(null) }
    val cropAlbumArt by rememberPreference(CropAlbumArtKey, false)

    Box(
        modifier = Modifier
            .padding(6.dp)
            .size(48.dp)
            .clip(RoundedCornerShape(ThumbnailCornerRadius))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        val isYtThumbnail = remember(mediaMetadata.thumbnailUrl) {
            mediaMetadata.thumbnailUrl?.let { it.contains("ytimg") || it.contains("/vi/") || it.contains("youtube.com") } == true
        }

        AsyncImage(
            model = mediaMetadata.thumbnailUrl?.resize(544, 544),
            contentDescription = null,
            contentScale = if (cropAlbumArt) ContentScale.Crop else ContentScale.Fit,
            onSuccess = {
                Timber.d("[Thumbnail] ClassicMiniPlayer loaded: ${mediaMetadata.thumbnailUrl}")
            },
            onError = {
                Timber.e("[Thumbnail] ClassicMiniPlayer FAILED: url=${mediaMetadata.thumbnailUrl} | error=${it.result.throwable}")
            },
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(ThumbnailCornerRadius))
                .graphicsLayer {
                    if (cropAlbumArt && isYtThumbnail) {
                        scaleX = 4f / 3f
                        scaleY = 4f / 3f
                    }
                },
        )

        androidx.compose.animation.AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        color = if (pureBlack) Color.Black else Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(ThumbnailCornerRadius),
                    ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.info),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun ClassicMiniText(
    mediaMetadata: MediaMetadata,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
) {
    Column(
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        modifier = modifier.padding(horizontal = 6.dp),
    ) {
        Text(
            text = mediaMetadata.title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.basicMarquee(),
        )

        if (mediaMetadata.artists.any { it.name.isNotBlank() }) {
            Text(
                text = mediaMetadata.artists.joinToString { it.name },
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
