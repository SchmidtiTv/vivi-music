package com.music.vivi.ui.player.fullscreen.apple

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.music.vivi.R
import com.music.vivi.db.entities.Song
import com.music.vivi.models.MediaMetadata
import com.music.vivi.playback.PlayerConnection
import com.music.vivi.ui.component.BottomSheetState
import com.music.vivi.ui.component.LocalBottomSheetPageState
import com.music.vivi.ui.component.LocalMenuState
import com.music.vivi.ui.menu.OldPlayerMenu
import com.music.vivi.ui.utils.ShowMediaInfo
import com.music.vivi.ui.utils.ShowOffsetDialog


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ApplePlayerActions(
    mediaMetadata: MediaMetadata,
    currentSong: Song?,
    showInlineLyrics: Boolean,
    textButtonColor: Color,
    playerConnection: PlayerConnection,
    navController: NavController,
    state: BottomSheetState,
    onToggleFullScreen: () -> Unit,
) {
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current

    AnimatedContent(targetState = showInlineLyrics, label = "DownloadButton") { showLyrics ->
        if (showLyrics) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(textButtonColor.copy(alpha = 0.2f))
                    .clickable(onClick = onToggleFullScreen),
            ) {
                Icon(
                    painter = painterResource(R.drawable.fullscreen),
                    contentDescription = null,
                    tint = textButtonColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(textButtonColor.copy(alpha = 0.2f))
                    .clickable {
                        menuState.show {
                            OldPlayerMenu(
                                mediaMetadata = mediaMetadata,
                                navController = navController,
                                playerBottomSheetState = state,
                                onShowDetailsDialog = {
                                    mediaMetadata.id.let {
                                        bottomSheetPageState.show {
                                            ShowMediaInfo(it)
                                        }
                                    }
                                },
                                onDismiss = menuState::dismiss
                            )
                        }
                    },
            ) {
                Icon(
                    painter = painterResource(R.drawable.more_vert),
                    contentDescription = null,
                    tint = textButtonColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.size(12.dp))

    AnimatedContent(targetState = showInlineLyrics, label = "LikeButton") { showLyrics ->
        if (showLyrics) {
            val currentLyrics by playerConnection.currentLyrics.collectAsState(initial = null)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(textButtonColor.copy(alpha = 0.2f))
                    .clickable {
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
            ) {
                Icon(
                    painter = painterResource(R.drawable.more_horiz),
                    contentDescription = null,
                    tint = textButtonColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(textButtonColor.copy(alpha = 0.2f))
                    .clickable(onClick = playerConnection::toggleLike),
            ) {
                Icon(
                    painter = painterResource(
                        if (currentSong?.song?.liked == true)
                            R.drawable.favorite
                        else R.drawable.favorite_border
                    ),
                    contentDescription = null,
                    tint = textButtonColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                )
            }
        }
    }
}