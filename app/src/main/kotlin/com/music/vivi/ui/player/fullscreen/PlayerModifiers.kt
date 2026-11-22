/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier

internal fun smootherStep(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return x * x * x * (x * (x * 6f - 15f) + 10f)
}

internal fun Modifier.bottomControlsMask(revealFraction: () -> Float): Modifier =
    graphicsLayer {
        val fraction = revealFraction()
        compositingStrategy = if (fraction < 1.1f) CompositingStrategy.Offscreen else CompositingStrategy.Auto
    }.drawWithContent {
        drawContent()
        val fraction = revealFraction()
        if (fraction < 1.1f) {
            val fadeTop = (fraction - 0.12f).coerceIn(0f, 1f)
            val fadeBottom = fraction.coerceIn(fadeTop + 0.01f, 1f)
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Black,
                    fadeTop to Color.Black,
                    fadeBottom to Color.Transparent,
                    1f to Color.Transparent
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }

internal fun Modifier.bottomControlsMask(revealFraction: Float): Modifier =
    bottomControlsMask { revealFraction }


internal fun Modifier.compactPanelGestures(
    key1: Boolean,
    key2: Boolean,
    onScrollDown: () -> Unit,
    onScrollUp: () -> Unit,
    onTapInteraction: () -> Unit
): Modifier = pointerInput(key1, key2) {
    var downPos = Offset.Zero
    var lastPos = Offset.Zero
    var dragged = false
    var verticalDragDelta = 0f
    val touchSlop = viewConfiguration.touchSlop
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull() ?: continue
            if (change.changedToDown()) {
                downPos = change.position
                lastPos = change.position
                dragged = false
                verticalDragDelta = 0f
            } else if (change.pressed) {
                val deltaY = change.position.y - lastPos.y
                lastPos = change.position
                if (!dragged && (change.position - downPos).getDistance() > touchSlop) {
                    dragged = true
                }
                if (deltaY < 0f) {
                    if (verticalDragDelta > 0f) verticalDragDelta = 0f
                    verticalDragDelta += deltaY
                    if (verticalDragDelta < -touchSlop) {
                        verticalDragDelta = 0f
                        onScrollDown()
                    }
                } else if (deltaY > 0f) {
                    if (verticalDragDelta < 0f) verticalDragDelta = 0f
                    verticalDragDelta += deltaY
                    if (verticalDragDelta > touchSlop) {
                        verticalDragDelta = 0f
                        onScrollUp()
                    }
                }
            } else if (change.changedToUp()) {
                if (!dragged) {
                    onTapInteraction()
                }
            }
        }
    }

}

