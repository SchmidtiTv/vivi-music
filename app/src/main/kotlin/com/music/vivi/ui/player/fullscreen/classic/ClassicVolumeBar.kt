/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen.classic

import android.media.AudioManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.music.vivi.constants.PlayerHorizontalPadding
import com.music.vivi.playback.CastConnectionHandler
import com.music.vivi.R
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun ClassicVolumeBar(
    audioManager: AudioManager,
    castHandler: CastConnectionHandler?,
    castVolume: Float,
    isCasting: Boolean,
    maxSystemVolume: Float,
    systemVolume: Float,
    TextBackgroundColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PlayerHorizontalPadding)
    ) {
                    var isDragging by remember { mutableStateOf(false) }
                    var lastAppliedStep by remember { mutableStateOf((systemVolume * maxSystemVolume).roundToInt()) }
                    val animatedVolume = remember { Animatable(systemVolume) }
                    val coroutineScope = rememberCoroutineScope()

                    LaunchedEffect(systemVolume) {
                        if (!isDragging) {
                            lastAppliedStep = (systemVolume * maxSystemVolume).roundToInt()
                            animatedVolume.animateTo(
                                targetValue = systemVolume,
                                animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing)
                            )
                        }
                    }

                    fun applyVolumeFraction(fraction: Float) {
                        val clamped = fraction.coerceIn(0f, 1f)
                        coroutineScope.launch { animatedVolume.snapTo(clamped) }
                        if (isCasting) {
                            castHandler?.setVolume(clamped)
                        } else {
                            val targetStep = (clamped * maxSystemVolume).roundToInt().coerceIn(0, maxSystemVolume.roundToInt())
                            if (targetStep != lastAppliedStep) {
                                lastAppliedStep = targetStep
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetStep, 0)
                            }
                        }
                    }

                    val currentBarHeight by animateDpAsState(
                        targetValue = if (isDragging) 10.dp else 6.dp,
                        animationSpec = tween(durationMillis = 150),
                        label = "volumeBarHeight"
                    )

                    val iconColor = TextBackgroundColor.copy(alpha = 0.65f)

                    Image(
                        painter = painterResource(R.drawable.apple_volume_down),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(iconColor),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .pointerInput(maxSystemVolume) {
                                detectTapGestures { offset ->
                                    if (size.width <= 0) return@detectTapGestures
                                    applyVolumeFraction(offset.x / size.width.toFloat())
                                }
                            }
                            .pointerInput(maxSystemVolume) {
                                var dragFraction = animatedVolume.value
                                detectHorizontalDragGestures(
                                    onDragStart = { offset ->
                                        if (size.width <= 0) return@detectHorizontalDragGestures
                                        isDragging = true
                                        dragFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                        applyVolumeFraction(dragFraction)
                                    },
                                    onHorizontalDrag = { _, delta ->
                                        if (size.width <= 0) return@detectHorizontalDragGestures
                                        dragFraction = (dragFraction + delta / size.width.toFloat()).coerceIn(0f, 1f)
                                        applyVolumeFraction(dragFraction)
                                    },
                                    onDragEnd = {
                                        isDragging = false
                                    },
                                    onDragCancel = {
                                        isDragging = false
                                    }
                                )
                            }
                    ) {
                        val trackHeightPx = currentBarHeight.toPx()
                        val topY = (size.height - trackHeightPx) / 2f
                        val cornerRadius = CornerRadius(trackHeightPx / 2f, trackHeightPx / 2f)
                        val fraction = (if (isCasting) castVolume else animatedVolume.value).coerceIn(0f, 1f)

                        drawRoundRect(
                            color = TextBackgroundColor.copy(alpha = 0.22f),
                            topLeft = Offset(0f, topY),
                            size = Size(size.width, trackHeightPx),
                            cornerRadius = cornerRadius
                        )

                        if (fraction > 0f) {
                            drawRoundRect(
                                color = TextBackgroundColor,
                                topLeft = Offset(0f, topY),
                                size = Size((size.width * fraction).coerceIn(0f, size.width), trackHeightPx),
                                cornerRadius = cornerRadius
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Image(
                        painter = painterResource(R.drawable.apple_volume_up),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(iconColor),
                        modifier = Modifier.size(20.dp)
                    )
                }
}
