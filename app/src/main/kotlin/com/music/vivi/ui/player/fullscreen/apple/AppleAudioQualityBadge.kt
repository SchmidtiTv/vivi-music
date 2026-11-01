package com.music.vivi.ui.player.fullscreen.apple

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.vivi.R
import com.music.vivi.constants.AudioQuality
import com.music.vivi.models.MediaMetadata
import com.music.vivi.ui.component.LocalBottomSheetPageState
import com.music.vivi.ui.utils.ShowMediaInfo
import com.music.vivi.utils.makeTimeString


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppleAudioQualityBadge(
    mediaMetadata: MediaMetadata,
    sleepTimerEnabled: Boolean,
    sleepTimerTimeLeft: Long,
    audioQuality: AudioQuality,
    textColor: Color,
) {
    val bottomSheetPageState = LocalBottomSheetPageState.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(textColor.copy(alpha = 0.08f))
            .border(
                width = 0.5.dp,
                color = textColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(enabled = !sleepTimerEnabled) {
                mediaMetadata.id.let {
                    bottomSheetPageState.show {
                        ShowMediaInfo(it)
                    }
                }
            }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        AnimatedContent(
            targetState = sleepTimerEnabled,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "QualityTimerSwitcher"
        ) { isTimerActive ->
            if (isTimerActive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.sleep_timer),
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = makeTimeString(sleepTimerTimeLeft.coerceAtLeast(0)),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = textColor.copy(alpha = 0.8f),
                        maxLines = 1,
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val infiniteTransition =
                        rememberInfiniteTransition(label = "QualityIconTransition")
                    val animatedRotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "QualityIconRotation"
                    )

                    val iconBrush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            textColor.copy(alpha = 1.0f),
                            Color.Transparent
                        )
                    )

                    Icon(
                        painter = painterResource(R.drawable.stream_old_player),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(12.dp)
                            .graphicsLayer(alpha = 0.99f)
                            .drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    rotate(animatedRotation) {
                                        drawRect(iconBrush, blendMode = BlendMode.SrcIn)
                                    }
                                }
                            }
                    )
                    Text(
                        text = when (audioQuality) {
                            AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                            AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                            AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                        }.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = textColor.copy(alpha = 0.8f),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
