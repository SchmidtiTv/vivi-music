/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.mini.m3e

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.music.vivi.LocalDatabase
import com.music.vivi.LocalListenTogetherManager
import com.music.vivi.LocalPlayerConnection
import com.music.vivi.R
import com.music.vivi.constants.DarkModeKey
import com.music.vivi.constants.MiniPlayerBackgroundStyleKey
import com.music.vivi.constants.MiniPlayerHeight
import com.music.vivi.constants.PlayerBackgroundStyle
import com.music.vivi.constants.PureBlackMiniPlayerKey
import com.music.vivi.constants.SwipeSensitivityKey
import com.music.vivi.constants.SwipeThumbnailKey
import com.music.vivi.listentogether.ListenTogetherManager
import com.music.vivi.models.MediaMetadata
import com.music.vivi.playback.CastConnectionHandler
import com.music.vivi.playback.PlayerConnection
import com.music.vivi.ui.player.MiniPlayerBackgroundLayer
import com.music.vivi.ui.player.MiniPlayerColorExtractor
import com.music.vivi.ui.player.MiniPlayerLayout
import com.music.vivi.ui.player.MiniPlayerSongInfo
import com.music.vivi.ui.player.ProgressState
import com.music.vivi.ui.player.RailMiniPlayerWidth
import com.music.vivi.ui.player.mini.MiniPlayerTheme
import com.music.vivi.ui.player.miniPlayerClick
import com.music.vivi.ui.screens.settings.DarkMode
import com.music.vivi.ui.utils.LocalAdaptiveLayout
import com.music.vivi.ui.utils.resize
import com.music.vivi.utils.rememberEnumPreference
import com.music.vivi.utils.rememberPreference
import com.music.vivi.vivimusic.AudioDeviceBottomSheet
import com.music.vivi.vivimusic.isBluetoothHeadphoneConnected
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import timber.log.Timber

object M3eMiniPlayerTheme : MiniPlayerTheme {
    override val id = "m3e"
    override val nameRes = R.string.new_mini_player_design
    override val floating = true
    override val transparentBottomInset = true

    @Composable
    override fun Content(
        progressState: ProgressState,
        modifier: Modifier,
        vertical: Boolean,
        onClick: (() -> Unit)?,
    ) {
        M3eMiniPlayer(
            progressState = progressState,
            modifier = modifier,
            vertical = vertical,
            onClick = onClick,
        )
    }
}

@Composable
private fun M3eMiniPlayer(
    progressState: ProgressState,
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    
    // Theme settings - these rarely change
    val pureBlack by rememberPreference(PureBlackMiniPlayerKey, defaultValue = false)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }
    
    val miniPlayerBackground by rememberEnumPreference(MiniPlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.DEFAULT)
    
    // Player states - only collect what's needed at this level
    val playbackState by playerConnection.playbackState.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    
    // Cast state - safely access castConnectionHandler to prevent crashes during service lifecycle changes
    val castHandler = remember(playerConnection) {
        try {
            playerConnection.service.castConnectionHandler
        } catch (e: Exception) {
            null
        }
    }
    val isCasting by castHandler?.isCasting?.collectAsState() ?: remember { mutableStateOf(false) }

    // Audio Output State
    val context = LocalContext.current
    val isBluetoothConnected = isBluetoothHeadphoneConnected(context)
    var showAudioDeviceBottomSheet by remember { mutableStateOf(false) }

    // Swipe settings
    val swipeSensitivity by rememberPreference(SwipeSensitivityKey, 0.73f)
    val swipeThumbnailPref by rememberPreference(SwipeThumbnailKey, true)
    
    // Disable swipe for Listen Together guests
    val listenTogetherManager = LocalListenTogetherManager.current
    val isListenTogetherGuest = listenTogetherManager?.let { it.isInRoom && !it.isHost } ?: false
    val swipeThumbnail = swipeThumbnailPref && !isListenTogetherGuest
    
    val layoutDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()
    
    val constrainWidth = LocalAdaptiveLayout.current.constrainMiniPlayer

    // Swipe animation state
    val offsetXAnimatable = remember { Animatable(0f) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var totalDragDistance by remember { mutableFloatStateOf(0f) }

    val animationSpec = remember {
        spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
    }

    val autoSwipeThreshold = remember(swipeSensitivity) {
        (600 / (1f + kotlin.math.exp(-(-11.44748 * swipeSensitivity + 9.04945)))).roundToInt()
    }

    val (gradientColors, onGradientColorsChange) = remember { mutableStateOf<List<Color>>(emptyList()) }

    MiniPlayerColorExtractor(
        mediaMetadata = mediaMetadata,
        miniPlayerBackground = miniPlayerBackground,
        onGradientColorsChange = onGradientColorsChange
    )
    
    // Memoize colors
    val backgroundColor = if (pureBlack && useDarkTheme) Color.Black else MaterialTheme.colorScheme.surfaceContainer
    val isDynamicBackground = miniPlayerBackground != PlayerBackgroundStyle.DEFAULT
    
    val primaryColor = if (isDynamicBackground) Color.White else MaterialTheme.colorScheme.primary
    val outlineColor = if (isDynamicBackground) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
    val onSurfaceColor = if (isDynamicBackground) Color.White else MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error

    val pill: @Composable (Modifier) -> Unit = { pillModifier ->
        Box(
            modifier = pillModifier
                .clip(RoundedCornerShape(50))
                .background(color = backgroundColor)
                .border(1.dp, outlineColor.copy(alpha = 0.3f), RoundedCornerShape(50))
                .miniPlayerClick(onClick)
        ) {
            // Background Layers
            Box(Modifier.matchParentSize()) {
                MiniPlayerBackgroundLayer(
                    style = miniPlayerBackground,
                    mediaMetadata = mediaMetadata,
                    gradientColors = gradientColors,
                    isPlaying = isPlaying
                )
            }

            MiniPlayerLayout(
                vertical = vertical,
                contentPadding = PaddingValues(8.dp),
                railContentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = (RailMiniPlayerWidth - 48.dp) / 2,
                    bottom = (RailMiniPlayerWidth - 40.dp) / 2,
                ),
                artworkGap = 16.dp,
                actionsGap = 12.dp,
                artwork = {
                    // Play button with progress - isolated composable
                    M3eMiniPlayerPlayButton(
                        progressState = progressState,
                        playbackState = playbackState,
                        isCasting = isCasting,
                        castHandler = castHandler,
                        playerConnection = playerConnection,
                        mediaMetadata = mediaMetadata,
                        primaryColor = primaryColor,
                        outlineColor = outlineColor,
                        listenTogetherManager = listenTogetherManager
                    )
                },
                info = { infoModifier ->
                    MiniPlayerSongInfo(
                        mediaMetadata = mediaMetadata,
                        onSurfaceColor = onSurfaceColor,
                        errorColor = errorColor,
                        centered = vertical,
                        modifier = infoModifier
                    )
                },
                actions = {
                    // Cast indicator
                    if (isCasting) {
                        Icon(
                            painter = painterResource(R.drawable.cast_connected),
                            contentDescription = "Casting",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                    }

                    // Audio Device Button (Replacing SubscribeButton)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color = primaryColor.copy(alpha = 0.1f))
                            .clickable { showAudioDeviceBottomSheet = true }
                    ) {
                        Icon(
                            painter = painterResource(if (isBluetoothConnected) R.drawable.bluetooth else R.drawable.speaker_apple),
                            contentDescription = stringResource(R.string.audio_devices),
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    // Favorite button - isolated composable
                    mediaMetadata?.let {
                        FavoriteButton(
                            songId = it.id,
                            onSurfaceColor = onSurfaceColor,
                            errorColor = errorColor,
                            outlineColor = outlineColor
                        )
                    }
                },
            )
        }
    }

    if (vertical) {
        pill(modifier.width(RailMiniPlayerWidth))
    } else Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .padding(horizontal = 12.dp)
            .let { baseModifier ->
                if (swipeThumbnail) {
                    baseModifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                dragStartTime = System.currentTimeMillis()
                                totalDragDistance = 0f
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetXAnimatable.animateTo(0f, animationSpec)
                                }
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
                                coroutineScope.launch {
                                    offsetXAnimatable.animateTo(0f, animationSpec)
                                }
                            }
                        )
                    }
                } else baseModifier
            }
    ) {
        pill(
            Modifier
                .then(if (constrainWidth) Modifier.width(500.dp).align(Alignment.Center) else Modifier.fillMaxWidth())
                .height(64.dp)
                .offset { IntOffset(offsetXAnimatable.value.roundToInt(), 0) }
        )
    }

    if (showAudioDeviceBottomSheet) {
        AudioDeviceBottomSheet(onDismiss = { showAudioDeviceBottomSheet = false })
    }
}

/**
 * Play button with circular progress indicator
 * Uses drawWithContent to update progress without recomposition
 */
@Composable
private fun M3eMiniPlayerPlayButton(
    progressState: ProgressState,
    playbackState: Int,
    isCasting: Boolean,
    castHandler: CastConnectionHandler?,
    playerConnection: PlayerConnection,
    mediaMetadata: MediaMetadata?,
    primaryColor: Color,
    outlineColor: Color,
    listenTogetherManager: ListenTogetherManager?
) {
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val castIsPlaying by castHandler?.castIsPlaying?.collectAsState() ?: remember { mutableStateOf(false) }
    val effectiveIsPlaying = if (isCasting) castIsPlaying else isPlaying
    val isListenTogetherGuest = listenTogetherManager?.let { it.isInRoom && !it.isHost } ?: false
    val isMuted by playerConnection.isMuted.collectAsState()

    
    val trackColor = outlineColor.copy(alpha = 0.2f)
    val strokeWidth = 3.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .drawWithContent {
                drawContent()
                // Draw progress arc - this reads progressState.progress during draw phase only
                val progress = progressState.progress
                val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                val startAngle = -90f
                val sweepAngle = 360f * progress
                val diameter = size.minDimension
                val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                
                // Draw track
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = stroke
                )
                // Draw progress
                drawArc(
                    color = primaryColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = stroke
                )
            }
    ) {
        // Thumbnail with play/pause overlay
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, outlineColor.copy(alpha = 0.3f), CircleShape)
                .clickable {
                    if (isListenTogetherGuest) {
                        playerConnection.toggleMute()
                        return@clickable
                    }
                    if (isCasting) {
                        if (castIsPlaying) castHandler?.pause() else castHandler?.play()
                    } else if (playbackState == Player.STATE_ENDED) {
                        playerConnection.player.seekTo(0, 0)
                        playerConnection.player.playWhenReady = true
                    } else {
                        playerConnection.togglePlayPause()
                    }
                }
        ) {
            mediaMetadata?.let { metadata ->
                AsyncImage(
                    model = metadata.thumbnailUrl?.resize(544, 544),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onSuccess = {
                        Timber.d("[Thumbnail] MiniPlayer play-button loaded: ${metadata.thumbnailUrl}")
                    },
                    onError = {
                        Timber.e("[Thumbnail] MiniPlayer play-button FAILED: url=${metadata.thumbnailUrl} | error=${it.result.throwable}")
                    },
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            }

            // Overlay for paused state or muted (guest)
            if (isListenTogetherGuest && isMuted || (!isListenTogetherGuest && (!effectiveIsPlaying || playbackState == Player.STATE_ENDED))) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                )
                Icon(
                    painter = painterResource(
                        if (isListenTogetherGuest) {
                            if (isMuted) R.drawable.volume_off else R.drawable.volume_up
                        } else if (playbackState == Player.STATE_ENDED) R.drawable.replay else R.drawable.play
                    ),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun FavoriteButton(
    songId: String,
    onSurfaceColor: Color,
    errorColor: Color,
    outlineColor: Color
) {
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val librarySong by database.song(songId).collectAsState(initial = null)
    val isLiked = librarySong?.song?.liked == true
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = if (isLiked) errorColor.copy(alpha = 0.5f) else outlineColor.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .background(
                color = if (isLiked) errorColor.copy(alpha = 0.1f) else Color.Transparent,
                shape = CircleShape
            )
            .clickable { playerConnection.service.toggleLike() }
    ) {
        Icon(
            painter = painterResource(if (isLiked) R.drawable.favorite else R.drawable.favorite_border),
            contentDescription = null,
            tint = if (isLiked) errorColor else onSurfaceColor.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
    }
}
