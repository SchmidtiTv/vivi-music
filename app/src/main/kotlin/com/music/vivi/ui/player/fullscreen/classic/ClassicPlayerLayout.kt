/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen.classic

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp as dpLerp
import androidx.compose.ui.zIndex
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.crossfade
import coil3.request.ImageRequest
import coil3.size.Size as CoilSize
import com.music.vivi.constants.PlayerBackgroundStyle
import com.music.vivi.constants.PlayerHorizontalPadding
import com.music.vivi.db.entities.Song
import com.music.vivi.models.MediaMetadata
import com.music.vivi.Motionartwork.Motionartwork
import com.music.vivi.playback.PlayerConnection
import com.music.vivi.R
import com.music.vivi.ui.component.BottomSheetState
import com.music.vivi.ui.component.Icon as MIcon
import com.music.vivi.ui.component.LocalBottomSheetPageState
import com.music.vivi.ui.component.LocalMenuState
import com.music.vivi.ui.component.Lyrics
import com.music.vivi.ui.menu.LyricsMenu
import com.music.vivi.ui.menu.OldPlayerMenu
import com.music.vivi.ui.player.fullscreen.BackgroundVideoView
import com.music.vivi.ui.player.fullscreen.bottomControlsMask
import com.music.vivi.ui.player.fullscreen.compactPanelGestures
import com.music.vivi.ui.player.fullscreen.InlineLyricsView
import com.music.vivi.ui.player.fullscreen.InlineQueueView
import com.music.vivi.ui.player.fullscreen.smootherStep
import com.music.vivi.ui.player.MotionartworkPlayer
import com.music.vivi.ui.player.fullscreen.BackgroundVideoView
import com.music.vivi.ui.player.fullscreen.InlineLyricsView
import com.music.vivi.ui.player.fullscreen.InlineQueueView
import com.music.vivi.ui.player.fullscreen.bottomControlsMask
import com.music.vivi.ui.player.fullscreen.compactPanelGestures
import com.music.vivi.ui.player.fullscreen.smootherStep
import com.music.vivi.ui.utils.ShowMediaInfo
import com.music.vivi.ui.utils.ShowOffsetDialog
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun ClassicPlayerLayout(
    state: BottomSheetState,
    sheetNestedScroll: Modifier,
    navController: NavController,
    bottomPadding: Dp,
    mediaMetadata: MediaMetadata?,
    currentSong: Song?,
    playerConnection: PlayerConnection,
    playerBackground: PlayerBackgroundStyle,
    showInlineLyrics: Boolean,
    showInlineQueue: Boolean,
    onShowInlineLyricsChange: (Boolean) -> Unit,
    onShowInlineQueueChange: (Boolean) -> Unit,
    isFullScreen: Boolean,
    isPlaying: Boolean,
    effectiveIsPlaying: Boolean,
    isListenTogetherGuest: Boolean,
    canSkipNext: Boolean,
    canSkipPrevious: Boolean,
    swipeThumbnailPref: Boolean,
    thumbnailCornerRadius: Float,
    cropAlbumArt: Boolean,
    enableCanvas: Boolean,
    canvasArtwork: Motionartwork?,
    trackInfoHeightDp: Dp,
    trackInfoSpacerHeight: Dp,
    positionProvider: () -> Long,
    controlsContent: @Composable ColumnScope.(MediaMetadata) -> Unit,
    TextBackgroundColor: Color,
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .padding(bottom = bottomPadding)
    ) {
        val isCompactHeader = showInlineLyrics || showInlineQueue
        val lyricsTransition = updateTransition(
            targetState = isCompactHeader,
            label = "playerCompactTransition"
        )
        val lyricsProgress by lyricsTransition.animateFloat(
            transitionSpec = { tween(durationMillis = 380, easing = FastOutSlowInEasing) },
            label = "lyricsProgress"
        ) { showing -> if (showing) 1f else 0f }

        val normalTopPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 24.dp
        val topSectionHeight = (maxHeight - normalTopPadding) * (1.25f / 2.25f)

        val expandedArtworkSize = minOf(maxWidth - (PlayerHorizontalPadding * 2), topSectionHeight - 16.dp).coerceAtLeast(120.dp)
        val expandedX = (maxWidth - expandedArtworkSize) / 2f
        val expandedY = normalTopPadding + (topSectionHeight - expandedArtworkSize) / 2f
        val controlsTop = (expandedY + expandedArtworkSize + 34.dp).coerceAtMost(normalTopPadding + topSectionHeight)
        val bottomControlsHeight = (maxHeight - controlsTop).coerceAtLeast(120.dp)

        val collapsedArtworkSize = 56.dp
        val collapsedX = PlayerHorizontalPadding
        val collapsedY = normalTopPadding

        val isAppleMusic = playerBackground == PlayerBackgroundStyle.APPLE_MUSIC

        val bannerHeight = (maxHeight * 0.65f).coerceAtMost(controlsTop + 16.dp)
        val targetExpandedWidth = if (isAppleMusic) maxWidth else expandedArtworkSize
        val targetExpandedHeight = if (isAppleMusic) bannerHeight else expandedArtworkSize
        val targetExpandedX = if (isAppleMusic) 0.dp else expandedX
        val targetExpandedY = if (isAppleMusic) 0.dp else expandedY

        val artworkWidth = dpLerp(targetExpandedWidth, collapsedArtworkSize, lyricsProgress)
        val artworkHeight = dpLerp(targetExpandedHeight, collapsedArtworkSize, lyricsProgress)
        val artworkX = dpLerp(targetExpandedX, collapsedX, lyricsProgress)
        val artworkY = dpLerp(targetExpandedY, collapsedY, lyricsProgress)

        var showCompactControls by remember(showInlineLyrics, showInlineQueue) {
            mutableStateOf(true)
        }

        var lastCompactInteraction by remember(showInlineLyrics, showInlineQueue) {
            mutableIntStateOf(0)
        }

        var isDraggingQueue by remember(showInlineQueue) {
            mutableStateOf(false)
        }

        LaunchedEffect(showInlineLyrics, showInlineQueue, effectiveIsPlaying) {
            if ((showInlineLyrics || showInlineQueue) && !effectiveIsPlaying) {
                showCompactControls = true
            }
        }

        LaunchedEffect(
            showInlineLyrics,
            showInlineQueue,
            effectiveIsPlaying,
            showCompactControls,
            lastCompactInteraction
        ) {
            if ((showInlineLyrics || showInlineQueue) && effectiveIsPlaying && showCompactControls) {
                delay(3500)
                showCompactControls = false
            }
        }

        val lyricsTop = collapsedY + collapsedArtworkSize + 16.dp
        val lyricsHeight = (maxHeight - lyricsTop - 16.dp).coerceAtLeast(120.dp)
        val compactControlsOffset = if (isCompactHeader) trackInfoHeightDp + trackInfoSpacerHeight else 0.dp
        val effectiveControlsTop = controlsTop + compactControlsOffset
        val effectiveControlsHeight = (maxHeight - effectiveControlsTop).coerceAtLeast(120.dp)
        val controlsMaskThresholdY = effectiveControlsTop

        val lyricsClipEnd =
            ((controlsMaskThresholdY - lyricsTop) / lyricsHeight).coerceIn(0.35f, 0.85f)
        val lyricsRevealFraction by animateFloatAsState(
            targetValue = if (showCompactControls) lyricsClipEnd else 1.15f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "lyricsRevealFraction"
        )

        val queueTop = collapsedY + collapsedArtworkSize + 12.dp
        val queueHeight = (maxHeight - queueTop - 8.dp).coerceAtLeast(120.dp)
        val queueClipEnd =
            ((controlsMaskThresholdY - queueTop) / queueHeight).coerceIn(0.35f, 0.85f)
        val queueRevealFraction by animateFloatAsState(
            targetValue = if (showCompactControls) queueClipEnd else 1.15f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "queueRevealFraction"
        )

        // Apple Music Top Drag Handle / Grabber Pill
        AnimatedVisibility(
            visible = !isFullScreen,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 4.dp)
                .zIndex(2f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 64.dp, height = 20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { state.collapseSoft() }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 5.dp)
                        .clip(CircleShape)
                        .background(TextBackgroundColor.copy(alpha = 0.45f))
                )
            }
        }

        // 1a. Synchronized Lyrics View
        AnimatedVisibility(
            visible = showInlineLyrics,
            enter = fadeIn(tween(durationMillis = 280, delayMillis = 100)),
            exit = fadeOut(tween(durationMillis = 150)),
            modifier = Modifier
                .offset(y = lyricsTop)
                .fillMaxWidth()
                .height(lyricsHeight)
                .bottomControlsMask { lyricsRevealFraction }
                .compactPanelGestures(
                    key1 = showInlineLyrics,
                    key2 = effectiveIsPlaying,
                    onScrollDown = {
                        if (effectiveIsPlaying && showCompactControls) {
                            showCompactControls = false
                        }
                    },
                    onScrollUp = {
                        if (effectiveIsPlaying && showCompactControls) {
                            showCompactControls = false
                        }
                    },
                    onTapInteraction = {
                        if (!showCompactControls) {
                            showCompactControls = true
                        }
                        lastCompactInteraction++
                    }
                )
        ) {
            val controlsOverlap = if (showCompactControls) {
                (lyricsTop + lyricsHeight - effectiveControlsTop).coerceAtLeast(0.dp)
            } else {
                0.dp
            }
            InlineLyricsView(
                mediaMetadata = mediaMetadata,
                showLyrics = showInlineLyrics,
                positionProvider = positionProvider,
                controlsOverlap = controlsOverlap
            )
        }

        // 1b. In-Screen Queue View
        AnimatedVisibility(
            visible = showInlineQueue,
            enter = fadeIn(tween(durationMillis = 280, delayMillis = 100)),
            exit = fadeOut(tween(durationMillis = 150)),
            modifier = Modifier
                .offset(y = queueTop)
                .fillMaxWidth()
                .height(queueHeight)
                .bottomControlsMask { queueRevealFraction }
                .compactPanelGestures(
                    key1 = showInlineQueue,
                    key2 = effectiveIsPlaying,
                    onScrollDown = {
                        if (!isDraggingQueue && showCompactControls) {
                            showCompactControls = false
                        }
                    },
                    onScrollUp = {
                        if (!isDraggingQueue && !showCompactControls) {
                            showCompactControls = true
                        }
                        lastCompactInteraction++
                    },
                    onTapInteraction = {
                        if (!showCompactControls) {
                            showCompactControls = true
                        }
                        lastCompactInteraction++
                    }
                )
        ) {
            InlineQueueView(
                playerConnection = playerConnection,
                navController = navController,
                playerBottomSheetState = state,
                primaryColor = TextBackgroundColor,
                onDraggingChange = { isDraggingQueue = it },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Playback Controls (auto-hides on delay during lyrics playback, reveals on tap)
        AnimatedVisibility(
            visible = !isCompactHeader || showCompactControls,
            enter = slideInVertically(
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                initialOffsetY = { it }
            ) + fadeIn(tween(durationMillis = 220)),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                targetOffsetY = { it }
            ) + fadeOut(tween(durationMillis = 220)),
            modifier = Modifier
                .offset(y = effectiveControlsTop)
                .fillMaxWidth()
                .height(effectiveControlsHeight)
                .let {
                    if (isCompactHeader) {
                        it.pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    if (event.changes.any { change -> change.changedToDown() || change.changedToUp() }) {
                                        lastCompactInteraction++
                                    }
                                }
                            }
                        }
                    } else {
                        it
                    }
                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                mediaMetadata?.let {
                    controlsContent(it)
                }
            }
        }

        // 3. Compact Header: Song Title, Artist, Favorite Star, 3-dot Lyrics Menu
        AnimatedVisibility(
            visible = isCompactHeader,
            enter = fadeIn(tween(durationMillis = 280, delayMillis = 100)),
            exit = fadeOut(tween(durationMillis = 150)),
            modifier = Modifier
                .offset(y = collapsedY)
                .fillMaxWidth()
                .height(collapsedArtworkSize)
                .padding(
                    start = collapsedX + collapsedArtworkSize + 12.dp,
                    end = PlayerHorizontalPadding
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = Triple(
                        mediaMetadata?.id,
                        mediaMetadata?.title.orEmpty(),
                        mediaMetadata?.artists?.joinToString(", ") { it.name }.orEmpty()
                    ),
                    transitionSpec = {
                        fadeIn(tween(durationMillis = 300)) togetherWith fadeOut(tween(durationMillis = 220))
                    },
                    label = "playerCompactTrackInfoTransition",
                    modifier = Modifier.weight(1f)
                ) { (_, title, artists) ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = TextBackgroundColor,
                            modifier = Modifier.basicMarquee()
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (mediaMetadata?.explicit == true) {
                                MIcon.Explicit(tint = TextBackgroundColor.copy(alpha = 0.65f))
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            Text(
                                text = artists,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextBackgroundColor.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.basicMarquee()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isLiked = currentSong?.song?.liked == true
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(TextBackgroundColor.copy(alpha = 0.14f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = playerConnection::toggleLike
                            )
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isLiked) R.drawable.star_apple else R.drawable.star_outline_apple
                            ),
                            contentDescription = if (isLiked) "Unlike" else "Like",
                            tint = TextBackgroundColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    val currentLyrics by playerConnection.currentLyrics.collectAsState(initial = null)
                    val metaForLyrics = mediaMetadata
                    if (metaForLyrics != null) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(TextBackgroundColor.copy(alpha = 0.14f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        if (showInlineQueue) {
                                            menuState.show {
                                                OldPlayerMenu(
                                                    mediaMetadata = metaForLyrics,
                                                    navController = navController,
                                                    playerBottomSheetState = state,
                                                    onShowDetailsDialog = {
                                                        metaForLyrics.id.let {
                                                            bottomSheetPageState.show {
                                                                ShowMediaInfo(it)
                                                            }
                                                        }
                                                    },
                                                    onDismiss = menuState::dismiss
                                                )
                                            }
                                        } else {
                                            menuState.show {
                                                LyricsMenu(
                                                    lyricsProvider = { currentLyrics },
                                                    songProvider = { currentSong?.song },
                                                    mediaMetadataProvider = { metaForLyrics },
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
                                        }
                                    }
                                )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ellipsis_horizontal),
                                contentDescription = if (showInlineQueue) "Queue Options" else "Lyrics Options",
                                tint = TextBackgroundColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Artwork: Unified single morphing component matching ViviLite architecture
        val artworkShape = if (isAppleMusic) {
            RoundedCornerShape(dpLerp(0.dp, 8.dp, lyricsProgress))
        } else {
            RoundedCornerShape(dpLerp(thumbnailCornerRadius.dp, 0.dp, lyricsProgress))
        }
        val maskAlpha = if (isAppleMusic) (1f - lyricsProgress).coerceIn(0f, 1f) else 0f
        val showDissolveMask = isAppleMusic && maskAlpha > 0.01f

        Box(
            modifier = Modifier
                .offset(x = artworkX, y = artworkY)
                .width(artworkWidth)
                .height(artworkHeight)
                .zIndex(1f)
                .then(sheetNestedScroll)
                .graphicsLayer {
                    shape = artworkShape
                    clip = true
                    compositingStrategy = if (showDissolveMask) {
                        CompositingStrategy.Offscreen
                    } else {
                        CompositingStrategy.Auto
                    }
                }
                .drawWithContent {
                    drawContent()
                    if (showDissolveMask) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colorStops = Array(17) { i ->
                                    val t = i / 16f
                                    t to Color.Black.copy(alpha = 1f - maskAlpha * smootherStep(t))
                                },
                                startY = size.height * (1f - 0.42f),
                                endY = size.height,
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    }
                }
                .background(if (isAppleMusic) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Crossfade(
                targetState = mediaMetadata?.id to (mediaMetadata?.thumbnailUrl ?: currentSong?.song?.thumbnailUrl),
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                label = "artworkCrossfade"
            ) { (_, thumbnailUrl) ->
                var currentModel by remember(thumbnailUrl) {
                    mutableStateOf<Any?>(thumbnailUrl)
                }
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(currentModel)
                        .size(CoilSize.ORIGINAL)
                        .crossfade(true)
                        .build(),
                    onError = {
                        val url = currentModel?.toString()
                        currentModel = when {
                            url == null -> null
                            url.contains("maxresdefault.jpg") -> url.replace("maxresdefault.jpg", "sddefault.jpg")
                            url.contains("sddefault.jpg") -> url.replace("sddefault.jpg", "hqdefault.jpg")
                            url.contains("hqdefault.jpg") -> url.replace("hqdefault.jpg", "mqdefault.jpg")
                            thumbnailUrl != null && url != thumbnailUrl -> thumbnailUrl
                            else -> null
                        }
                    },
                    contentDescription = null,
                    contentScale = if (isAppleMusic || cropAlbumArt) ContentScale.Crop else ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            val motionVideoUrl = if (isAppleMusic) {
                canvasArtwork?.animatedTall ?: canvasArtwork?.animated ?: canvasArtwork?.videoUrl
            } else {
                canvasArtwork?.animated ?: canvasArtwork?.videoUrl
            }
            if (enableCanvas && !isCompactHeader && motionVideoUrl != null) {
                if (isAppleMusic) {
                    BackgroundVideoView(
                        videoUrl = motionVideoUrl,
                        isPlaying = isPlaying,
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    MotionartworkPlayer(
                        primaryUrl = motionVideoUrl,
                        fallbackUrl = canvasArtwork?.videoUrl,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isCompactHeader, swipeThumbnailPref, isListenTogetherGuest) {
                        if (isCompactHeader) {
                            detectTapGestures(
                                onTap = {
                                    onShowInlineLyricsChange(false)
                                    onShowInlineQueueChange(false)
                                }
                            )
                        } else {
                            detectHorizontalDragGestures { _, dragAmount ->
                                if (swipeThumbnailPref && !isListenTogetherGuest) {
                                    if (dragAmount < -20f && canSkipNext) {
                                        playerConnection.player.seekToNext()
                                    } else if (dragAmount > 20f && canSkipPrevious) {
                                        playerConnection.player.seekToPreviousMediaItem()
                                    }
                                }
                            }
                        }
                    }
            )
        }
    }
}
