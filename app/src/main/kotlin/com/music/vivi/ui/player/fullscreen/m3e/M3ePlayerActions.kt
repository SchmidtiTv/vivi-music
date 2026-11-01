package com.music.vivi.ui.player.fullscreen.m3e

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.music.vivi.LocalDatabase
import com.music.vivi.R
import com.music.vivi.db.entities.Song
import com.music.vivi.models.MediaMetadata
import com.music.vivi.playback.ExoDownloadService
import com.music.vivi.playback.PlayerConnection
import com.music.vivi.ui.component.LocalBottomSheetPageState
import com.music.vivi.ui.component.LocalMenuState
import com.music.vivi.ui.utils.ShowOffsetDialog


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun M3ePlayerActions(
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