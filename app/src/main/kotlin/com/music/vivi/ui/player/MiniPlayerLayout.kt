package com.music.vivi.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.music.vivi.R

val RailMiniPlayerWidth = 80.dp

/**
 * Positions a mini player's parts for every design: in a row for the bottom bar, or stacked and
 * centered for the navigation rail. [actions] is emitted straight into that row/column, so separate
 * its items with `Spacer(Modifier.size(..))` to work in both orientations.
 */
@Composable
internal fun MiniPlayerLayout(
    vertical: Boolean,
    artwork: @Composable () -> Unit,
    info: @Composable (Modifier) -> Unit,
    actions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    railContentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
    artworkGap: Dp = 12.dp,
    actionsGap: Dp = 8.dp,
) {
    if (vertical) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(railContentPadding),
        ) {
            artwork()
            Spacer(Modifier.height(8.dp))
            info(Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            actions()
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxSize().padding(contentPadding),
        ) {
            artwork()
            Spacer(Modifier.width(artworkGap))
            info(Modifier.weight(1f))
            Spacer(Modifier.width(actionsGap))
            actions()
        }
    }
}

@Composable
internal fun Modifier.miniPlayerClick(onClick: (() -> Unit)?): Modifier =
    if (onClick == null) this
    else clickable(onClickLabel = stringResource(R.string.now_playing), onClick = onClick)
