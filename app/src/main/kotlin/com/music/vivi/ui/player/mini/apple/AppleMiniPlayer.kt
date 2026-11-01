/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.mini.apple

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.music.vivi.LocalListenTogetherManager
import com.music.vivi.LocalPlayerConnection
import com.music.vivi.R
import com.music.vivi.constants.CropAlbumArtKey
import com.music.vivi.constants.DarkModeKey
import com.music.vivi.constants.MiniPlayerBackgroundStyleKey
import com.music.vivi.constants.MiniPlayerHeight
import com.music.vivi.constants.PlayerBackgroundStyle
import com.music.vivi.constants.PureBlackMiniPlayerKey
import com.music.vivi.constants.SwipeSensitivityKey
import com.music.vivi.constants.SwipeThumbnailKey
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
import com.music.vivi.utils.rememberEnumPreference
import com.music.vivi.utils.rememberPreference
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

object AppleMiniPlayerTheme : MiniPlayerTheme {
    override val id = "apple"
    override val nameRes = R.string.apple_mini_player_design
    override val floating = true
    override val transparentBottomInset = false

    @Composable
    override fun Content(
        progressState: ProgressState,
        modifier: Modifier,
        vertical: Boolean,
        onClick: (() -> Unit)?,
    ) {
        AppleMiniPlayer(
            progressState = progressState,
            modifier = modifier,
            vertical = vertical,
            onClick = onClick,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppleMiniPlayer(
    progressState: ProgressState,
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    
    // Theme settings
    val pureBlack by rememberPreference(PureBlackMiniPlayerKey, defaultValue = false)
    val cropAlbumArt by rememberPreference(CropAlbumArtKey, defaultValue = false)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }
    
    val miniPlayerBackground by rememberEnumPreference(MiniPlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.DEFAULT)
    
    // Player states
    val playbackState by playerConnection.playbackState.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    
    // Cast state
    val castHandler = remember(playerConnection) {
        try {
            playerConnection.service.castConnectionHandler
        } catch (e: Exception) {
            null
        }
    }
    val isCasting by castHandler?.isCasting?.collectAsState() ?: remember { mutableStateOf(false) }

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
    val backgroundColor = if (pureBlack && useDarkTheme) Color.Black else MaterialTheme.colorScheme.surface
    val isDynamicBackground = miniPlayerBackground != PlayerBackgroundStyle.DEFAULT
    
    val primaryColor = if (isDynamicBackground) Color.White else MaterialTheme.colorScheme.primary
    val outlineColor = if (isDynamicBackground) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
    val onSurfaceColor = if (isDynamicBackground) Color.White else MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error


    val pill: @Composable (Modifier) -> Unit = { pillModifier ->
        Box(
            modifier = pillModifier
                .clip(RoundedCornerShape(8.dp)) // Apple rectangular shape with slightly rounded corners
                .background(color = backgroundColor)
                .miniPlayerClick(onClick)
        ) {
            // Background Layers
            Box(Modifier.matchParentSize()) {
                MiniPlayerBackgroundLayer(
                    style = miniPlayerBackground,
                    mediaMetadata = mediaMetadata,
                    gradientColors = gradientColors
                )
            }

            // Bottom Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter)
                    .drawWithContent {
                        val progress = progressState.progress
                        val trackColor = outlineColor.copy(alpha = 0.2f)
                        drawRect(trackColor)
                        drawRect(primaryColor, size = Size(size.width * progress, size.height))
                    }
            )

            MiniPlayerLayout(
                vertical = vertical,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp),
                artworkGap = 12.dp,
                actionsGap = 8.dp,
                artwork = {
                    // Cookie 4-Sided Thumbnail
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(MaterialShapes.Cookie4Sided.toShape())
                            .background(color = outlineColor.copy(alpha = 0.2f))
                    ) {
                        mediaMetadata?.let { metadata ->
                            val isYtThumbnail = remember(metadata.thumbnailUrl) {
                                metadata.thumbnailUrl?.let { it.contains("ytimg") || it.contains("/vi/") || it.contains("youtube.com") } == true
                            }
                            AsyncImage(
                                model = metadata.thumbnailUrl,
                                contentDescription = null,
                                contentScale = if (cropAlbumArt) ContentScale.Crop else ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        if (cropAlbumArt && isYtThumbnail) {
                                            scaleX = 4f / 3f
                                            scaleY = 4f / 3f
                                        }
                                    }
                            )
                        }
                    }
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
                        Spacer(modifier = Modifier.size(8.dp))
                    }

                    // Play/Pause Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (isListenTogetherGuest) {
                                    playerConnection.toggleMute()
                                    return@clickable
                                }
                                if (isCasting) {
                                    val castIsPlaying = castHandler?.castIsPlaying?.value ?: false
                                    if (castIsPlaying) castHandler?.pause() else castHandler?.play()
                                } else if (playbackState == Player.STATE_ENDED) {
                                    playerConnection.player.seekTo(0, 0)
                                    playerConnection.player.playWhenReady = true
                                } else {
                                    playerConnection.togglePlayPause()
                                }
                            }
                    ) {
                        val isPlaying by playerConnection.isPlaying.collectAsState()
                        val castIsPlaying by castHandler?.castIsPlaying?.collectAsState() ?: remember { mutableStateOf(false) }
                        val effectiveIsPlaying = if (isCasting) castIsPlaying else isPlaying
                        val isMuted by playerConnection.isMuted.collectAsState()

                        Icon(
                            painter = painterResource(
                                when {
                                    isListenTogetherGuest -> if (isMuted) R.drawable.volume_off else R.drawable.volume_up
                                    playbackState == Player.STATE_ENDED -> R.drawable.replay
                                    effectiveIsPlaying -> R.drawable.pause_applemusic
                                    else -> R.drawable.play_applemusic
                                }
                            ),
                            contentDescription = null,
                            tint = onSurfaceColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    // Next Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable(enabled = canSkipNext && !isListenTogetherGuest) {
                                playerConnection.seekToNext()
                            }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.apple_skip_next),
                            contentDescription = "Next",
                            tint = if (canSkipNext && !isListenTogetherGuest) onSurfaceColor else onSurfaceColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
            )
        }
    }

    if (vertical) {
        pill(modifier.width(RailMiniPlayerWidth))
        return
    }

    Box(
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
                                val canSkipPreviousLocal = playerConnection.player.previousMediaItemIndex != -1
                                val canSkipNextLocal = playerConnection.player.nextMediaItemIndex != -1
                                val tryingToSwipeRight = adjustedDragAmount > 0
                                val tryingToSwipeLeft = adjustedDragAmount < 0
                                val allowLeft = tryingToSwipeLeft && canSkipNextLocal
                                val allowRight = tryingToSwipeRight && canSkipPreviousLocal

                                val canReturnToCenter =
                                    (tryingToSwipeRight && !canSkipPreviousLocal && offsetXAnimatable.value < 0) ||
                                            (tryingToSwipeLeft && !canSkipNextLocal && offsetXAnimatable.value > 0)

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

                                val canSkipPreviousLocal = playerConnection.player.previousMediaItemIndex != -1
                                val canSkipNextLocal = playerConnection.player.nextMediaItemIndex != -1

                                val shouldChangeSong = (kotlin.math.abs(currentOffset) > minDistanceThreshold && velocity > velocityThreshold) ||
                                    (kotlin.math.abs(currentOffset) > autoSwipeThreshold)

                                if (shouldChangeSong) {
                                    // Same crossfade-aware path as Thumbnail/MiniPlayer; falls back to an
                                    // instant skip when crossfade-on-manual-skip is off or can't start.
                                    if (currentOffset > 0 && canSkipPreviousLocal) {
                                        if (!playerConnection.service.manualSkipToPreviousWithCrossfade()) {
                                            playerConnection.player.seekToPreviousMediaItem()
                                        }
                                    } else if (currentOffset <= 0 && canSkipNextLocal) {
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
}
