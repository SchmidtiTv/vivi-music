/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 * 
 * Performance optimized MiniPlayer - prevents unnecessary recomposition
 */

package com.music.vivi.ui.player

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.music.vivi.LocalListenTogetherManager
import com.music.vivi.LocalPlayerConnection
import com.music.vivi.R
import com.music.vivi.constants.PlayerBackgroundStyle
import com.music.vivi.constants.SwipeSensitivityKey
import com.music.vivi.constants.SwipeThumbnailKey
import com.music.vivi.models.MediaMetadata
import com.music.vivi.ui.theme.PlayerColorExtractor
import com.music.vivi.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.music.vivi.ui.component.Icon as MIcon
import com.music.vivi.ui.player.mini.rememberMiniPlayerTheme

/**
 * Stable wrapper for progress state - reads values only during draw phase
 * This prevents recomposition when position/duration change
 */
@Stable
class ProgressState(
    private val positionState: MutableLongState,
    private val durationState: MutableLongState
) {
    val progress: Float
        get() {
            val duration = durationState.longValue
            return if (duration > 0) (positionState.longValue.toFloat() / duration).coerceIn(0f, 1f) else 0f
        }
}

@Composable
fun MiniPlayer(
    positionState: MutableLongState,
    durationState: MutableLongState,
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val theme = rememberMiniPlayerTheme().value

    // Create stable progress state - doesn't cause recomposition on position changes
    val progressState = remember { ProgressState(positionState, durationState) }

    theme.Content(
        progressState = progressState,
        modifier = modifier,
        vertical = vertical,
        onClick = onClick,
    )
}


/**
 * The user's chosen mini player design, stacked vertically for the bottom of the navigation rail.
 */
@Composable
fun RailMiniPlayer(
    positionState: MutableLongState,
    durationState: MutableLongState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    if (mediaMetadata == null) return

    // Same swipe-to-skip as the horizontal mini player, turned 90°: up = next, down = previous.
    val swipeSensitivity by rememberPreference(SwipeSensitivityKey, 0.73f)
    val swipeThumbnailPref by rememberPreference(SwipeThumbnailKey, true)
    val listenTogetherManager = LocalListenTogetherManager.current
    val isListenTogetherGuest = listenTogetherManager?.let { it.isInRoom && !it.isHost } ?: false
    val swipeThumbnail = swipeThumbnailPref && !isListenTogetherGuest

    val coroutineScope = rememberCoroutineScope()
    val offsetYAnimatable = remember { Animatable(0f) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var totalDragDistance by remember { mutableFloatStateOf(0f) }
    val animationSpec = remember {
        spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
    }
    val autoSwipeThreshold = remember(swipeSensitivity) {
        (600 / (1f + kotlin.math.exp(-(-11.44748 * swipeSensitivity + 9.04945)))).roundToInt()
    }

    val swipeModifier = if (!swipeThumbnail) Modifier else Modifier.pointerInput(Unit) {
        detectVerticalDragGestures(
            onDragStart = {
                dragStartTime = System.currentTimeMillis()
                totalDragDistance = 0f
            },
            onDragCancel = {
                coroutineScope.launch { offsetYAnimatable.animateTo(0f, animationSpec) }
            },
            onVerticalDrag = { _, dragAmount ->
                val canSkipPrevious = playerConnection.player.previousMediaItemIndex != -1
                val canSkipNext = playerConnection.player.nextMediaItemIndex != -1
                val tryingToSwipeDown = dragAmount > 0
                val tryingToSwipeUp = dragAmount < 0
                val allowUp = tryingToSwipeUp && canSkipNext
                val allowDown = tryingToSwipeDown && canSkipPrevious
                val canReturnToCenter =
                    (tryingToSwipeDown && !canSkipPrevious && offsetYAnimatable.value < 0) ||
                        (tryingToSwipeUp && !canSkipNext && offsetYAnimatable.value > 0)

                if (allowUp || allowDown || canReturnToCenter) {
                    totalDragDistance += kotlin.math.abs(dragAmount)
                    coroutineScope.launch { offsetYAnimatable.snapTo(offsetYAnimatable.value + dragAmount) }
                }
            },
            onDragEnd = {
                val dragDuration = System.currentTimeMillis() - dragStartTime
                val velocity = if (dragDuration > 0) totalDragDistance / dragDuration else 0f
                val currentOffset = offsetYAnimatable.value
                val minDistanceThreshold = 50f
                val velocityThreshold = (swipeSensitivity * -8.25f) + 8.5f

                val shouldChangeSong = (kotlin.math.abs(currentOffset) > minDistanceThreshold && velocity > velocityThreshold) ||
                    (kotlin.math.abs(currentOffset) > autoSwipeThreshold)

                if (shouldChangeSong) {
                    val canSkipPrevious = playerConnection.player.previousMediaItemIndex != -1
                    val canSkipNext = playerConnection.player.nextMediaItemIndex != -1
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
                coroutineScope.launch { offsetYAnimatable.animateTo(0f, animationSpec) }
            }
        )
    }

    MiniPlayer(
        positionState = positionState,
        durationState = durationState,
        modifier = modifier
            .then(swipeModifier)
            .offset { IntOffset(0, offsetYAnimatable.value.roundToInt()) },
        vertical = true,
        onClick = onClick,
    )
}

/**
 * Song info display - title and artist
 */
@Composable
internal fun MiniPlayerSongInfo(
    mediaMetadata: MediaMetadata?,
    onSurfaceColor: Color,
    errorColor: Color,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
) {
    val error by LocalPlayerConnection.current?.error?.collectAsState() ?: remember { mutableStateOf(null) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        mediaMetadata?.let { metadata ->
            Text(
                text = metadata.title,
                color = onSurfaceColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee(iterations = 1, initialDelayMillis = 3000, velocity = 30.dp),
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (metadata.explicit) MIcon.Explicit()
                if (metadata.artists.any { it.name.isNotBlank() }) {
                    Text(
                        text = metadata.artists.joinToString { it.name },
                        color = onSurfaceColor.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee(iterations = 1, initialDelayMillis = 3000, velocity = 30.dp),
                    )
                }
            }

            AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = stringResource(R.string.error_playing),
                    color = errorColor,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun MiniPlayerColorExtractor(
    mediaMetadata: MediaMetadata?,
    miniPlayerBackground: PlayerBackgroundStyle,
    onGradientColorsChange: (List<Color>) -> Unit
) {
    val context = LocalContext.current
    val fallbackColor = MaterialTheme.colorScheme.surfaceContainer.toArgb()

    LaunchedEffect(mediaMetadata?.id, miniPlayerBackground) {
        if (miniPlayerBackground == PlayerBackgroundStyle.GRADIENT || miniPlayerBackground == PlayerBackgroundStyle.GLOW_ANIMATED) {
            val currentMetadata = mediaMetadata
            if (currentMetadata?.thumbnailUrl != null) {
                withContext(Dispatchers.IO) {
                    val request = ImageRequest.Builder(context)
                        .data(currentMetadata.thumbnailUrl)
                        .size(100, 100)
                        .allowHardware(false)
                        .build()

                    val result = runCatching { context.imageLoader.execute(request) }.getOrNull()
                    if (result != null) {
                        val bitmap = result.image?.toBitmap()
                        if (bitmap != null) {
                            val palette = withContext(Dispatchers.Default) {
                                Palette.from(bitmap)
                                    .maximumColorCount(8)
                                    .resizeBitmapArea(100 * 100)
                                    .generate()
                            }
                            val extractedColors = if (miniPlayerBackground == PlayerBackgroundStyle.GLOW_ANIMATED) {
                                listOfNotNull(
                                    palette.getVibrantColor(fallbackColor).let { Color(it) },
                                    palette.getLightVibrantColor(fallbackColor).let { Color(it) },
                                    palette.getDarkVibrantColor(fallbackColor).let { Color(it) },
                                    palette.getMutedColor(fallbackColor).let { Color(it) },
                                    palette.getLightMutedColor(fallbackColor).let { Color(it) },
                                    palette.getDarkMutedColor(fallbackColor).let { Color(it) }
                                ).distinct()
                            } else {
                                PlayerColorExtractor.extractGradientColors(
                                    palette = palette,
                                    fallbackColor = fallbackColor
                                )
                            }
                            withContext(Dispatchers.Main) { onGradientColorsChange(extractedColors) }
                        }
                    }
                }
            }
        } else {
            onGradientColorsChange(emptyList())
        }
    }
}

@Composable
internal fun MiniPlayerBackgroundLayer(
    style: PlayerBackgroundStyle,
    mediaMetadata: MediaMetadata?,
    gradientColors: List<Color>
) {
    val context = LocalContext.current
    
    when (style) {
        PlayerBackgroundStyle.BLUR -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(mediaMetadata?.thumbnailUrl)
                        .size(128, 128)
                        .allowHardware(false)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(30.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                )
            }
        }
        PlayerBackgroundStyle.GRADIENT -> {
            if (gradientColors.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(gradientColors))
                        .background(Color.Black.copy(alpha = 0.2f))
                )
            }
        }
        PlayerBackgroundStyle.GLOW_ANIMATED -> {
            if (gradientColors.isNotEmpty()) {
                val infiniteTransition = rememberInfiniteTransition(label = "GlowAnimation")
                val progress = infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(20000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "glowProgress"
                )

                val colors = gradientColors
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val p = progress.value
                            val width = size.width
                            val height = size.height
                            
                            fun rotatedColorAt(index: Int): Color {
                                val size = colors.size
                                val idx = index.toFloat() + p * size
                                val a = kotlin.math.floor(idx).toInt() % size
                                val b = (a + 1) % size
                                val frac = idx - kotlin.math.floor(idx)
                                return lerp(colors[a], colors[b], frac)
                            }

                            fun oscillate(min: Float, max: Float, phase: Float): Float {
                                val v = kotlin.math.sin(2f * kotlin.math.PI.toFloat() * (p + phase))
                                return min + (max - min) * ((v + 1f) * 0.5f)
                            }

                            val c1 = rotatedColorAt(0)
                            val c2 = rotatedColorAt(1)

                            val o1x = oscillate(0.0f, 1.0f, 0.0f)
                            val o1y = oscillate(0.0f, 0.5f, 0.1f)
                            val o2x = oscillate(1.0f, 0.0f, 0.2f)
                            val o2y = oscillate(0.5f, 1.0f, 0.3f)

                            val b1 = Brush.radialGradient(
                                colors = listOf(c1.copy(alpha = 0.8f), Color.Transparent),
                                center = Offset(width * o1x, height * o1y),
                                radius = width * 1.2f
                            )
                            val b2 = Brush.radialGradient(
                                colors = listOf(c2.copy(alpha = 0.7f), Color.Transparent),
                                center = Offset(width * o2x, height * o2y),
                                radius = width * 1.0f
                            )
                            
                            drawRect(Color(0xFF050505))
                            drawRect(b1)
                            drawRect(b2)
                        }
                )
            }
        }
        PlayerBackgroundStyle.LIVE_MESH -> {
            val infiniteTransition = rememberInfiniteTransition(label = "liveMesh")
            val rotation = infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(60000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.5f
                        scaleY = 1.5f
                    }
            ) {
                val matrix = remember { ColorMatrix().apply { setToSaturation(1.6f) } }
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(mediaMetadata?.thumbnailUrl)
                        .size(128, 128)
                        .allowHardware(false)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(matrix),
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(40.dp)
                        .graphicsLayer { rotationZ = rotation.value }
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
            }
        }
        else -> {}
    }
}
