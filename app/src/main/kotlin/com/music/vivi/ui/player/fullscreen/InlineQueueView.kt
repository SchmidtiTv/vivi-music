/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player.fullscreen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.source.ShuffleOrder.DefaultShuffleOrder
import androidx.navigation.NavController
import com.music.vivi.constants.PlayerHorizontalPadding
import com.music.vivi.constants.QueueEditLockKey
import com.music.vivi.extensions.metadata
import com.music.vivi.extensions.move
import com.music.vivi.extensions.removeWindow
import com.music.vivi.extensions.toggleRepeatMode
import com.music.vivi.listentogether.RoomRole
import com.music.vivi.LocalListenTogetherManager
import com.music.vivi.playback.PlayerConnection
import com.music.vivi.R
import com.music.vivi.ui.component.ActionPromptDialog
import com.music.vivi.ui.component.BottomSheetState
import com.music.vivi.ui.component.LocalBottomSheetPageState
import com.music.vivi.ui.component.LocalMenuState
import com.music.vivi.ui.component.MediaMetadataListItem
import com.music.vivi.ui.menu.QueueMenu
import com.music.vivi.ui.menu.SelectionMediaMetadataMenu
import com.music.vivi.ui.utils.ShowMediaInfo
import com.music.vivi.utils.makeTimeString
import com.music.vivi.utils.rememberPreference
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import sh.calvin.reorderable.rememberReorderableLazyListState
import sh.calvin.reorderable.ReorderableItem
import timber.log.Timber
import com.music.vivi.ui.player.smoothScrollToTrack
import com.music.vivi.ui.player.Queue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InlineQueueView(
    playerConnection: PlayerConnection,
    navController: NavController,
    playerBottomSheetState: BottomSheetState,
    primaryColor: Color = Color.White,
    onDraggingChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current

    val queueWindows by playerConnection.queueWindows.collectAsState()
    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()
    val shuffleModeEnabled by playerConnection.shuffleModeEnabled.collectAsState()
    val automix by playerConnection.service.automixItems.collectAsState()

    val listenTogetherManager = LocalListenTogetherManager.current
    val listenTogetherRoleState = listenTogetherManager?.role?.collectAsState(initial = RoomRole.NONE)
    val isListenTogetherGuest = listenTogetherRoleState?.value == RoomRole.GUEST

    val castHandler = remember(playerConnection) {
        try {
            playerConnection.service.castConnectionHandler
        } catch (e: Exception) {
            null
        }
    }

    val isCasting by castHandler?.isCasting?.collectAsState() ?: remember { mutableStateOf(false) }
    val castIsPlaying by castHandler?.castIsPlaying?.collectAsState() ?: remember { mutableStateOf(false) }

    val currentPlayingUid = remember(currentWindowIndex, queueWindows) {
        if (currentWindowIndex in queueWindows.indices) {
            queueWindows[currentWindowIndex].uid
        } else null
    }

    var isQueueLocked by rememberPreference(QueueEditLockKey, defaultValue = true)

    var inSelectMode by rememberSaveable { mutableStateOf(false) }
    val selection = rememberSaveable(
        saver = listSaver<MutableList<String>, String>(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf() }
    val onExitSelectionMode = {
        inSelectMode = false
        selection.clear()
    }
    if (inSelectMode) {
        BackHandler(onBack = onExitSelectionMode)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var dismissJob: Job? by remember { mutableStateOf(null) }

    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var sleepTimerValue by remember { mutableFloatStateOf(30f) }
    val sleepTimerEnabled = remember(
        playerConnection.service.sleepTimer.triggerTime,
        playerConnection.service.sleepTimer.pauseWhenSongEnd
    ) {
        playerConnection.service.sleepTimer.isActive
    }
    var sleepTimerTimeLeft by remember { mutableLongStateOf(0L) }

    LaunchedEffect(sleepTimerEnabled) {
        if (sleepTimerEnabled) {
            while (isActive) {
                sleepTimerTimeLeft = if (playerConnection.service.sleepTimer.pauseWhenSongEnd) {
                    playerConnection.player.duration - playerConnection.player.currentPosition
                } else {
                    playerConnection.service.sleepTimer.triggerTime - System.currentTimeMillis()
                }
                delay(1000L)
            }
        }
    }


    val mutableQueueWindows = remember {
        mutableStateListOf<Timeline.Window>().apply { addAll(queueWindows) }
    }
    var dragInfo by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = currentWindowIndex.coerceAtLeast(0))

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        scrollThresholdPadding = WindowInsets.systemBars.asPaddingValues()
    ) { from, to ->
        val currentDragInfo = dragInfo
        dragInfo = if (currentDragInfo == null) {
            from.index to to.index
        } else {
            currentDragInfo.first to to.index
        }
        val safeFrom = from.index.coerceIn(0, mutableQueueWindows.lastIndex)
        val safeTo = to.index.coerceIn(0, mutableQueueWindows.lastIndex)
        mutableQueueWindows.move(safeFrom, safeTo)
    }

    LaunchedEffect(reorderableState.isAnyItemDragging) {
        onDraggingChange(reorderableState.isAnyItemDragging)
        if (!reorderableState.isAnyItemDragging) {
            dragInfo?.let { (from, to) ->
                val safeFrom = from.coerceIn(0, queueWindows.lastIndex)
                val safeTo = to.coerceIn(0, queueWindows.lastIndex)
                if (!playerConnection.player.shuffleModeEnabled) {
                    playerConnection.player.moveMediaItem(safeFrom, safeTo)
                } else {
                    playerConnection.player.setShuffleOrder(
                        DefaultShuffleOrder(
                            queueWindows.map { it.firstPeriodIndex }
                                .toMutableList()
                                .move(safeFrom, safeTo)
                                .toIntArray(),
                            System.currentTimeMillis()
                        )
                    )
                }
                dragInfo = null
            }
        }
    }


    LaunchedEffect(queueWindows) {
        val sameItems = queueWindows.size == mutableQueueWindows.size &&
            queueWindows.indices.all { i -> queueWindows[i].mediaItem.mediaId == mutableQueueWindows[i].mediaItem.mediaId }
        if (!sameItems) {
            mutableQueueWindows.clear()
            mutableQueueWindows.addAll(queueWindows)
        }
    }

    var previousWindowIndex by remember { mutableIntStateOf(currentWindowIndex) }

    LaunchedEffect(currentWindowIndex) {
        if (currentWindowIndex != -1 && currentWindowIndex != previousWindowIndex) {
            previousWindowIndex = currentWindowIndex
            if (currentWindowIndex in mutableQueueWindows.indices && !reorderableState.isAnyItemDragging) {
                val targetUid = if (currentWindowIndex in queueWindows.indices) queueWindows[currentWindowIndex].uid else null
                lazyListState.smoothScrollToTrack(
                    index = currentWindowIndex,
                    key = targetUid?.hashCode()
                )
            }
        }
    }


    val activePrimaryColor = primaryColor
    val activeContainerColor = activePrimaryColor.copy(alpha = 0.82f)
    val inactiveContainerColor = activePrimaryColor.copy(alpha = 0.16f)
    val activeIconColor = Color(0xFF1A1A1A)
    val inactiveIconColor = activePrimaryColor.copy(alpha = 0.85f)
    val activeSecondaryColor = activePrimaryColor.copy(alpha = 0.65f)

    data class QueueBarAction(
        val iconRes: Int,
        val active: Boolean,
        val description: String,
        val enabled: Boolean = true,
        val text: String? = null,
        val onClick: () -> Unit
    )

    val actions = listOf(
        // 1. Shuffle
        QueueBarAction(
            iconRes = R.drawable.shuffle,
            active = shuffleModeEnabled,
            description = "Shuffle",
            enabled = !isListenTogetherGuest,
            onClick = {
                val next = !shuffleModeEnabled
                playerConnection.player.shuffleModeEnabled = next
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(0)
                }
            }
        ),
        // 2. Repeat
        QueueBarAction(
            iconRes = if (repeatMode == Player.REPEAT_MODE_ONE) R.drawable.repeat_one else R.drawable.repeat,
            active = repeatMode != Player.REPEAT_MODE_OFF,
            description = "Repeat",
            enabled = !isListenTogetherGuest,
            onClick = { playerConnection.player.toggleRepeatMode() }
        ),
        // 3. Sleep Timer
        QueueBarAction(
            iconRes = R.drawable.timer,
            active = sleepTimerEnabled,
            description = if (sleepTimerEnabled) "Timer active: ${makeTimeString(sleepTimerTimeLeft)}" else "Sleep Timer",
            enabled = !isListenTogetherGuest,
            text = if (sleepTimerEnabled) makeTimeString(sleepTimerTimeLeft) else null,
            onClick = {
                if (sleepTimerEnabled) {
                    playerConnection.service.sleepTimer.clear()
                } else {
                    showSleepTimerDialog = true
                }
            }
        ),
        // 4. Lock / Reorder
        QueueBarAction(
            iconRes = if (isQueueLocked) R.drawable.lock else R.drawable.lock_open,
            active = !isQueueLocked,
            description = stringResource(if (isQueueLocked) R.string.unlock_queue else R.string.lock_queue),
            enabled = !isListenTogetherGuest,
            onClick = { isQueueLocked = !isQueueLocked }
        )
    )

    CompositionLocalProvider(LocalContentColor provides activePrimaryColor) {
        Column(modifier = modifier.fillMaxSize()) {
            // Header: Multi-selection bar OR Top 4 Action pill buttons
            if (inSelectMode) {
                val selectedSongs = remember(selection.toList(), mutableQueueWindows) {
                    mutableQueueWindows.filter { it.mediaItem.mediaId in selection }
                        .mapNotNull { it.mediaItem.metadata }
                }
                val selectedItems = remember(selection.toList(), mutableQueueWindows) {
                    mutableQueueWindows.filter { it.mediaItem.mediaId in selection }
                }
                val count = selection.size
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = PlayerHorizontalPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onExitSelectionMode) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = null,
                            tint = activePrimaryColor
                        )
                    }
                    Text(
                        text = pluralStringResource(R.plurals.n_selected, count, count),
                        color = activePrimaryColor,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(
                        checked = count == mutableQueueWindows.size && count > 0,
                        onCheckedChange = {
                            if (count == mutableQueueWindows.size) {
                                selection.clear()
                            } else {
                                selection.clear()
                                mutableQueueWindows.forEach {
                                    selection.add(it.mediaItem.mediaId)
                                }
                            }
                        }
                    )
                    IconButton(
                        enabled = count > 0,
                        onClick = {
                            menuState.show {
                                SelectionMediaMetadataMenu(
                                    songSelection = selectedSongs,
                                    onDismiss = menuState::dismiss,
                                    clearAction = onExitSelectionMode,
                                    currentItems = selectedItems
                                )
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = null,
                            tint = if (count > 0) activePrimaryColor else activePrimaryColor.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PlayerHorizontalPadding, vertical = 4.dp)
                ) {
                    actions.forEach { action ->
                        val targetContainerColor = if (action.active) activeContainerColor else inactiveContainerColor
                        val targetIconColor = if (action.active) activeIconColor else inactiveIconColor

                        val containerColor by animateColorAsState(
                            targetValue = targetContainerColor,
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                            label = "queueActionContainer"
                        )
                        val iconColor by animateColorAsState(
                            targetValue = targetIconColor,
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                            label = "queueActionIcon"
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(CircleShape)
                                .background(containerColor)
                                .alpha(if (action.enabled) 1f else 0.4f)
                                .clickable(
                                    enabled = action.enabled,
                                    onClick = action.onClick
                                )
                        ) {
                            if (action.text != null) {
                                Text(
                                    text = action.text,
                                    color = iconColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Icon(
                                    painter = painterResource(action.iconRes),
                                    contentDescription = action.description,
                                    tint = iconColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Queued songs list & Undo Snackbar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        items = mutableQueueWindows,
                        key = { _, window -> window.uid.hashCode() }
                    ) { index, window ->
                        ReorderableItem(
                            state = reorderableState,
                            key = window.uid.hashCode()
                        ) {
                            val currentItem by rememberUpdatedState(window)
                            val isActive = window.uid == currentPlayingUid
                            val itemAlpha by animateFloatAsState(
                                targetValue = if (currentWindowIndex >= 0 && index < currentWindowIndex) 0.55f else 1f,
                                animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
                                label = "queueItemAlpha"
                            )
                            val dismissBoxState = rememberSwipeToDismissBoxState(
                                positionalThreshold = { totalDistance -> totalDistance }
                            )

                            var processedDismiss by remember { mutableStateOf(false) }
                            LaunchedEffect(dismissBoxState.currentValue) {
                                val dv = dismissBoxState.currentValue
                                if (!processedDismiss && !isListenTogetherGuest && (
                                    dv == SwipeToDismissBoxValue.StartToEnd || dv == SwipeToDismissBoxValue.EndToStart
                                )) {
                                    processedDismiss = true
                                    playerConnection.player.removeWindow(currentItem)
                                    dismissJob?.cancel()
                                    dismissJob = coroutineScope.launch {
                                        val snackbarResult = snackbarHostState.showSnackbar(
                                            message = context.getString(
                                                R.string.removed_song_from_playlist,
                                                currentItem.mediaItem.metadata?.title
                                            ),
                                            actionLabel = context.getString(R.string.undo),
                                            duration = SnackbarDuration.Short
                                        )
                                        if (snackbarResult == SnackbarResult.ActionPerformed) {
                                            playerConnection.player.addMediaItem(currentItem.mediaItem)
                                            val targetIndex = currentItem.firstPeriodIndex.coerceIn(0, playerConnection.player.mediaItemCount)
                                            Timber.tag("Queue").d("undo: restored '${currentItem.mediaItem.metadata?.title}' at index $targetIndex")
                                            playerConnection.player.moveMediaItem(
                                                mutableQueueWindows.size.coerceIn(0, playerConnection.player.mediaItemCount - 1),
                                                targetIndex
                                            )
                                        }
                                    }
                                }
                                if (dv == SwipeToDismissBoxValue.Settled) {
                                    processedDismiss = false
                                }
                            }

                            val onCheckedChange: (Boolean) -> Unit = { checked ->
                                if (checked) {
                                    selection.add(window.mediaItem.mediaId)
                                } else {
                                    selection.remove(window.mediaItem.mediaId)
                                }
                            }

                            val itemContent: @Composable () -> Unit = {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.graphicsLayer { alpha = itemAlpha }
                                ) {
                                    MediaMetadataListItem(
                                        mediaMetadata = window.mediaItem.metadata!!,
                                        isSelected = inSelectMode && window.mediaItem.mediaId in selection,
                                        isActive = isActive,
                                        isPlaying = isPlaying && isActive,
                                        backgroundColor = Color.Transparent,
                                        subtitleColor = activeSecondaryColor,
                                        trailingContent = {
                                            if (inSelectMode) {
                                                Checkbox(
                                                    checked = window.mediaItem.mediaId in selection,
                                                    onCheckedChange = onCheckedChange
                                                )
                                            } else {
                                                if (!isListenTogetherGuest) {
                                                    IconButton(
                                                        onClick = {
                                                            menuState.show {
                                                                QueueMenu(
                                                                    mediaMetadata = window.mediaItem.metadata!!,
                                                                    navController = navController,
                                                                    playerBottomSheetState = playerBottomSheetState,
                                                                    onShowDetailsDialog = {
                                                                        window.mediaItem.mediaId.let {
                                                                            bottomSheetPageState.show {
                                                                                ShowMediaInfo(it)
                                                                            }
                                                                        }
                                                                    },
                                                                    onDismiss = menuState::dismiss
                                                                )
                                                            }
                                                        }
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(R.drawable.more_vert),
                                                            contentDescription = null,
                                                            tint = activeSecondaryColor
                                                        )
                                                    }
                                                }
                                                AnimatedVisibility(
                                                    visible = !isQueueLocked && !isListenTogetherGuest,
                                                    enter = fadeIn(tween(180)) + expandHorizontally(tween(180)),
                                                    exit = fadeOut(tween(180)) + shrinkHorizontally(tween(180))
                                                ) {
                                                    IconButton(
                                                        onClick = {},
                                                        modifier = Modifier.draggableHandle()
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(R.drawable.drag_handle),
                                                            contentDescription = null,
                                                            tint = activeSecondaryColor
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {
                                                    if (inSelectMode) {
                                                        onCheckedChange(window.mediaItem.mediaId !in selection)
                                                    } else if (!isListenTogetherGuest) {
                                                        if (isActive) {
                                                            if (isCasting) {
                                                                if (castIsPlaying) castHandler?.pause() else castHandler?.play()
                                                            } else {
                                                                if (isPlaying) {
                                                                    playerConnection.player.pause()
                                                                } else {
                                                                    playerConnection.player.play()
                                                                }
                                                            }
                                                        } else {
                                                            coroutineScope.launch {
                                                                lazyListState.smoothScrollToTrack(window.firstPeriodIndex, window.uid.hashCode())
                                                            }
                                                            if (isCasting) {
                                                                val mediaId = window.mediaItem.mediaId
                                                                val navigated = castHandler?.navigateToMediaIfInQueue(mediaId) ?: false
                                                                if (!navigated) {
                                                                    playerConnection.player.seekToDefaultPosition(window.firstPeriodIndex)
                                                                    playerConnection.player.playWhenReady = true
                                                                }
                                                            } else {
                                                                playerConnection.player.playWhenReady = true
                                                                playerConnection.player.seekToDefaultPosition(window.firstPeriodIndex)
                                                            }
                                                        }
                                                    }
                                                },
                                                onLongClick = {
                                                    if (!inSelectMode && !isListenTogetherGuest) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        inSelectMode = true
                                                        onCheckedChange(true)
                                                    }
                                                }
                                            )
                                    )
                                }
                            }

                            if (inSelectMode || isQueueLocked) {
                                itemContent()
                            } else {
                                SwipeToDismissBox(
                                    state = dismissBoxState,
                                    backgroundContent = {}
                                ) {
                                    itemContent()
                                }
                            }
                        }
                    }

                    if (automix.isNotEmpty()) {
                        item(key = "automix_divider") {
                            Text(
                                text = stringResource(R.string.similar_content),
                                color = activeSecondaryColor,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }

                        itemsIndexed(
                            items = automix,
                            key = { _, it -> it.mediaId }
                        ) { index, item ->
                            Row(
                                horizontalArrangement = Arrangement.Center
                            ) {
                                MediaMetadataListItem(
                                    mediaMetadata = item.metadata!!,
                                    backgroundColor = Color.Transparent,
                                    subtitleColor = activeSecondaryColor,
                                    trailingContent = {
                                        if (!isListenTogetherGuest) {
                                            IconButton(
                                                onClick = {
                                                    playerConnection.service.playNextAutomix(item, index)
                                                }
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.playlist_play),
                                                    contentDescription = null,
                                                    tint = activeSecondaryColor
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    playerConnection.service.addToQueueAutomix(item, index)
                                                }
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.queue_music),
                                                    contentDescription = null,
                                                    tint = activeSecondaryColor
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {},
                                            onLongClick = {
                                                menuState.show {
                                                    QueueMenu(
                                                        mediaMetadata = item.metadata!!,
                                                        navController = navController,
                                                        playerBottomSheetState = playerBottomSheetState,
                                                        onShowDetailsDialog = {
                                                            item.mediaId.let {
                                                                bottomSheetPageState.show {
                                                                    ShowMediaInfo(it)
                                                                }
                                                            }
                                                        },
                                                        onDismiss = menuState::dismiss
                                                    )
                                                }
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                )
            }
        }
    }


    if (showSleepTimerDialog) {
        ActionPromptDialog(
            titleBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.sleep_timer),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            },
            onDismiss = { showSleepTimerDialog = false },
            onConfirm = {
                showSleepTimerDialog = false
                playerConnection.service.sleepTimer.start(sleepTimerValue.roundToInt())
            },
            onCancel = {
                showSleepTimerDialog = false
            },
            onReset = {
                sleepTimerValue = 30f
            },
            content = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.minute,
                            sleepTimerValue.roundToInt(),
                            sleepTimerValue.roundToInt()
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Spacer(Modifier.height(16.dp))

                    Slider(
                        value = sleepTimerValue,
                        onValueChange = { sleepTimerValue = it },
                        valueRange = 5f..120f,
                        steps = (120 - 5) / 5 - 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            showSleepTimerDialog = false
                            playerConnection.service.sleepTimer.start(-1)
                        }
                    ) {
                        Text(stringResource(R.string.end_of_song))
                    }
                }
            }
        )
    }
}

