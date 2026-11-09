/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.vivi.ui.player

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.WindowManager
import android.widget.Toast
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.produceState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import coil3.size.Size as CoilSize
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import androidx.compose.ui.graphics.lerp as colorLerp
import com.music.vivi.ui.menu.LyricsMenu
import kotlinx.coroutines.flow.MutableStateFlow
import com.music.vivi.LocalDatabase
import com.music.vivi.LocalPlayerConnection
import com.music.vivi.LocalDownloadUtil
import com.music.vivi.LocalListenTogetherManager
import com.music.vivi.R
import com.music.vivi.constants.CropAlbumArtKey
import com.music.vivi.constants.DarkModeKey
import com.music.vivi.constants.HidePlayerThumbnailKey
import com.music.vivi.constants.EnableLyricsThumbnailPlayPauseKey
import com.music.vivi.constants.KeepScreenOn
import com.music.vivi.constants.PlayerBackgroundStyle
import com.music.vivi.constants.PlayerBackgroundStyleKey
import com.music.vivi.constants.PlayerButtonsStyle
import com.music.vivi.constants.PlayerButtonsStyleKey
import com.music.vivi.constants.PlayerHorizontalPadding
import com.music.vivi.constants.QueuePeekHeight
import com.music.vivi.constants.SliderStyle
import com.music.vivi.constants.SliderStyleKey
import com.music.vivi.constants.SquigglySliderKey
import com.music.vivi.constants.SwipeLyricsKey
import com.music.vivi.constants.ThumbnailCornerRadius
import com.music.vivi.constants.ThumbnailCornerRadiusKey
import com.music.vivi.constants.SwipeThumbnailKey
import com.music.vivi.ui.player.fullscreen.rememberUseNewPlayerDesign
import com.music.vivi.extensions.SwipeGesture
import com.music.vivi.extensions.togglePlayPause
import com.music.vivi.listentogether.RoomRole
import com.music.vivi.models.MediaMetadata
import com.music.vivi.playback.ExoDownloadService
import com.music.vivi.vivimusic.getConnectedBluetoothDeviceName
import com.music.vivi.vivimusic.isBuds
import com.music.vivi.vivimusic.isSpeaker
import com.music.vivi.vivimusic.AudioDeviceBottomSheet
import com.music.vivi.ui.component.BottomSheet
import com.music.vivi.ui.component.BottomSheetState
import com.music.vivi.ui.player.backdrop.PlayerBlurBackdrop
import com.music.vivi.ui.player.backdrop.rememberArtworkBlurBitmap
import com.music.vivi.ui.component.LocalBottomSheetPageState
import com.music.vivi.ui.component.LocalMenuState
import com.music.vivi.ui.component.Lyrics
import com.music.vivi.ui.component.PlayerSliderTrack
import com.music.vivi.ui.component.SquigglySlider
import com.music.vivi.ui.component.ThinSlider
import com.music.vivi.ui.component.WavySlider
import com.music.vivi.ui.component.rememberBottomSheetState
import com.music.vivi.ui.menu.OldPlayerMenu
import com.music.vivi.ui.screens.settings.DarkMode
import com.music.vivi.ui.theme.PlayerColorExtractor
import com.music.vivi.ui.theme.PlayerSliderColors
import com.music.vivi.ui.utils.LocalAdaptiveLayout
import com.music.vivi.ui.utils.ShowMediaInfo
import com.music.vivi.ui.utils.ShowOffsetDialog
import com.music.vivi.utils.makeTimeString
import com.music.vivi.utils.rememberEnumPreference
import com.music.vivi.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.ermohdamaan.justforpixel.justforpixelexpressivelab.core.components.sliders.ExpressiveWavySlider
import kotlinx.coroutines.withContext
import kotlin.math.max
import com.music.vivi.ui.component.Icon as MIcon
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.AspectRatioFrameLayout
import coil3.request.crossfade
import com.music.vivi.Motionartwork.apple.AppleMusicMotionartworkProvider
import com.music.vivi.Motionartwork.Motionartwork
import com.music.vivi.Motionartwork.tidal.TidalMotionartworkProvider
import com.music.vivi.Motionartwork.vivimusic.ViviMusicMotionartworkProvider
import com.music.vivi.constants.MotionartworkSource
import com.music.vivi.constants.MotionartworkSourceKey
import com.music.vivi.constants.CanvasThumbnailAnimationKey
import com.music.vivi.constants.SpotifyCanvasEnabledKey
import com.music.spotify.Spotify
import com.music.spotify.SpotifyCanvasProvider
import com.music.vivi.constants.CanvasLoadOnlyWifiKey
import timber.log.Timber
import com.music.vivi.ui.player.MotionartworkPlaybackCache
import com.music.vivi.utils.isWifiConnected
import com.music.vivi.utils.SpotifySessionManager
import java.util.Locale
import com.music.vivi.ui.player.fullscreen.BackgroundVideoView
import com.music.vivi.ui.player.fullscreen.DockedQueuePlayerLayout
import com.music.vivi.ui.player.fullscreen.InlineLyricsView
import com.music.vivi.ui.player.fullscreen.InlineQueueView
import com.music.vivi.ui.player.fullscreen.smootherStep
import com.music.vivi.ui.player.fullscreen.classic.ClassicBottomActions
import com.music.vivi.ui.player.fullscreen.classic.ClassicPlayerLayout
import com.music.vivi.ui.player.fullscreen.classic.ClassicPlaybackControls
import com.music.vivi.ui.player.fullscreen.classic.ClassicVolumeBar
import com.music.vivi.ui.player.fullscreen.m3e.M3ePlaybackControls
import com.music.vivi.ui.player.fullscreen.m3e.M3ePlayerLayout
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
    pureBlack: Boolean,
    positionState: MutableLongState = remember { mutableLongStateOf(0L) },
    durationState: MutableLongState = remember { mutableLongStateOf(0L) },
    slideFromStart: Boolean = false,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.isCollapsed, state.isDismissed) {
        if (!state.isCollapsed && !state.isDismissed) {
            keyboardController?.hide()
            focusManager.clearFocus()
            (context as? android.app.Activity)?.let { activity ->
                WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                    .hide(WindowInsetsCompat.Type.ime())
            }
        }
    }


    val useNewPlayerDesign = rememberUseNewPlayerDesign()
    val (hidePlayerThumbnail, onHidePlayerThumbnailChange) = rememberPreference(HidePlayerThumbnailKey, false)
    val cropAlbumArt by rememberPreference(CropAlbumArtKey, false)
    val swipeThumbnailPref by rememberPreference(SwipeThumbnailKey, true)
    val thumbnailCornerRadius by rememberPreference(ThumbnailCornerRadiusKey, defaultValue = 3f)
    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.LIVE_MESH
    )
    val playerButtonsStyle by rememberEnumPreference(
        key = PlayerButtonsStyleKey,
        defaultValue = PlayerButtonsStyle.DEFAULT
    )

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val enableCanvas by rememberPreference(CanvasThumbnailAnimationKey, true)
    val (motionartworkSource) = rememberEnumPreference(MotionartworkSourceKey, defaultValue = MotionartworkSource.AUTO)
    val canvasLoadOnlyWifi by rememberPreference(CanvasLoadOnlyWifiKey, defaultValue = false)
    val spotifyCanvasEnabled by rememberPreference(SpotifyCanvasEnabledKey, false)

    val shouldUseDarkButtonColors = remember(playerBackground, useDarkTheme) {
        when (playerBackground) {
            PlayerBackgroundStyle.BLUR, PlayerBackgroundStyle.GRADIENT, PlayerBackgroundStyle.GLOW_ANIMATED, PlayerBackgroundStyle.APPLE_MUSIC, PlayerBackgroundStyle.LIVE_MESH -> true
            PlayerBackgroundStyle.DEFAULT -> useDarkTheme
        }
    }


    val isPlaying by playerConnection.isPlaying.collectAsState()
    val swipeLyrics by rememberPreference(SwipeLyricsKey, false)
    val enableLyricsThumbnailPlayPause by rememberPreference(EnableLyricsThumbnailPlayPauseKey, false)
    val isKeepScreenOn by rememberPreference(KeepScreenOn, false)
    val keepScreenOn = isPlaying && isKeepScreenOn

    DisposableEffect(playerBackground, state.isExpanded, useDarkTheme, keepScreenOn) {
        val window = (context as? android.app.Activity)?.window
        if (window != null && state.isExpanded) {
            WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)

            
            when (playerBackground) {
                PlayerBackgroundStyle.BLUR, PlayerBackgroundStyle.GRADIENT, PlayerBackgroundStyle.GLOW_ANIMATED, PlayerBackgroundStyle.APPLE_MUSIC, PlayerBackgroundStyle.LIVE_MESH -> {
                    insetsController.isAppearanceLightStatusBars = false
                }
                PlayerBackgroundStyle.DEFAULT -> {
                    insetsController.isAppearanceLightStatusBars = !useDarkTheme
                }
            }

            if (keepScreenOn && state.isExpanded)
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            else
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        
        onDispose {
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)

                insetsController.isAppearanceLightStatusBars = !useDarkTheme
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    val onBackgroundColor = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val useBlackBackground =
        remember(isSystemInDarkTheme, darkTheme, pureBlack) {
            val useDarkTheme =
                if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
            useDarkTheme && pureBlack
        }

    val playbackState by playerConnection.playbackState.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val nextMediaMetadata by playerConnection.nextMediaMetadata.collectAsState()
    val crossfadeIncomingItem by playerConnection.crossfadeIncomingItem.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val automix by playerConnection.service.automixItems.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val isMuted by playerConnection.isMuted.collectAsState()
    val playerVolume by playerConnection.service.playerVolume.collectAsState()

    val sliderStyle by rememberEnumPreference(SliderStyleKey, SliderStyle.SLIM)
    val squigglySlider by rememberPreference(SquigglySliderKey, defaultValue = false)
    
    // Listen Together state (reactive)
    val listenTogetherManager = LocalListenTogetherManager.current
    val listenTogetherRoleState = listenTogetherManager?.role?.collectAsState(initial = RoomRole.NONE)
    val isListenTogetherGuest = listenTogetherRoleState?.value == RoomRole.GUEST
    
    // Cast state - safely access castConnectionHandler to prevent crashes during service lifecycle changes
    val castHandler = remember(playerConnection) {
        try {
            playerConnection.service.castConnectionHandler
        } catch (e: Exception) {
            null
        }
    }

    val isCasting by castHandler?.isCasting?.collectAsState() ?: remember { mutableStateOf(false) }
    val castPosition by castHandler?.castPosition?.collectAsState() ?: remember { mutableLongStateOf(0L) }
    val castDuration by castHandler?.castDuration?.collectAsState() ?: remember { mutableLongStateOf(0L) }
    val castIsPlaying by castHandler?.castIsPlaying?.collectAsState() ?: remember { mutableStateOf(false) }
    val castVolume by castHandler?.castVolume?.collectAsState() ?: remember { mutableFloatStateOf(1f) }
    
    // Use Cast state when casting, otherwise local player
    val effectiveIsPlaying = if (isCasting) castIsPlaying else isPlaying

    // Use State objects for position/duration to pass to MiniPlayer without causing recomposition
    // These states persist across playback state changes to ensure continuous progress updates
    // Convenience accessors for local use
    var position by positionState
    var duration by durationState
    
    val effectivePosition by remember {
        derivedStateOf {
            if (isCasting) {
                castPosition
            } else {
                position
            }
        }
    }

    
    var sliderPosition by remember {
        mutableStateOf<Long?>(null)
    }
    // Track when we last manually set position to avoid Cast overwriting it
    var lastManualSeekTime by remember { mutableLongStateOf(0L) }
    
    var gradientColors by remember {
        mutableStateOf<List<Color>>(emptyList())
    }
    val gradientColorsCache = remember { mutableMapOf<String, List<Color>>() }

    if (!canSkipNext && automix.isNotEmpty()) {
        playerConnection.service.addToQueueAutomix(automix[0], 0)
    }

    val bluetoothDeviceName by produceState<String?>(initialValue = getConnectedBluetoothDeviceName(context)) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                value = getConnectedBluetoothDeviceName(context)
            }
        }

        val callback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            object : android.media.AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out android.media.AudioDeviceInfo>?) {
                    value = getConnectedBluetoothDeviceName(context)
                }
                override fun onAudioDevicesRemoved(removedDevices: Array<out android.media.AudioDeviceInfo>?) {
                    value = getConnectedBluetoothDeviceName(context)
                }
            }
        } else null

        val filter = IntentFilter().apply {
            addAction(AudioManager.ACTION_HEADSET_PLUG)
            addAction("android.bluetooth.adapter.action.STATE_CHANGED")
            addAction("android.bluetooth.device.action.ACL_CONNECTED")
            addAction("android.bluetooth.device.action.ACL_DISCONNECTED")
            addAction("android.media.AUDIO_BECOMING_NOISY")
        }
        
        context.registerReceiver(receiver, filter)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && callback != null) {
            audioManager.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
        }
        
        awaitDispose {
            context.unregisterReceiver(receiver)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && callback != null) {
                audioManager.unregisterAudioDeviceCallback(callback)
            }
        }
    }

    var showAudioDeviceBottomSheet by remember { mutableStateOf(false) }
    var lastNonNullName by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(bluetoothDeviceName) {
        if (bluetoothDeviceName != null) lastNonNullName = bluetoothDeviceName
    }
    val nameToShow = bluetoothDeviceName ?: lastNonNullName


    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxSystemVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat() }
    val systemVolume by produceState(initialValue = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxSystemVolume) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
                    value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxSystemVolume
                }
            }
        }
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        context.registerReceiver(receiver, filter)
        awaitDispose {
            context.unregisterReceiver(receiver)
        }
    }


    val defaultGradientColors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)
    val fallbackColor = MaterialTheme.colorScheme.surface.toArgb()

    LaunchedEffect(mediaMetadata?.id, playerBackground) {
        if (playerBackground == PlayerBackgroundStyle.GRADIENT || playerBackground == PlayerBackgroundStyle.GLOW_ANIMATED) {
            val currentMetadata = mediaMetadata
            if (currentMetadata != null && currentMetadata.thumbnailUrl != null) {
                val cachedColors = gradientColorsCache[currentMetadata.id]
                if (cachedColors != null) {
                    gradientColors = cachedColors
                    return@LaunchedEffect
                }
                withContext(Dispatchers.IO) {
                    val request = ImageRequest.Builder(context)
                        .data(currentMetadata.thumbnailUrl)
                        .size(100, 100)
                        .allowHardware(false)
                        .memoryCacheKey("gradient_${currentMetadata.id}")
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
                            val extractedColors = if (playerBackground == PlayerBackgroundStyle.GLOW_ANIMATED) {
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
                            gradientColorsCache[currentMetadata.id] = extractedColors
                            withContext(Dispatchers.Main) { gradientColors = extractedColors }
                        }
                    }
                }
            }
        } else {
            gradientColors = emptyList()
        }
    }


    // Preload incoming background thumbnail and gradient colors so the transition into next song has ZERO splash
    val incomingBackgroundItem = crossfadeIncomingItem ?: nextMediaMetadata
    LaunchedEffect(incomingBackgroundItem?.id, playerBackground) {
        val nextThumbnail = incomingBackgroundItem?.thumbnailUrl ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val req100 = ImageRequest.Builder(context)
                .data(nextThumbnail)
                .size(100, 100)
                .allowHardware(false)
                .build()
            context.imageLoader.execute(req100)

            val req128 = ImageRequest.Builder(context)
                .data(nextThumbnail)
                .size(128, 128)
                .allowHardware(false)
                .build()
            context.imageLoader.execute(req128)

            if ((playerBackground == PlayerBackgroundStyle.GRADIENT || playerBackground == PlayerBackgroundStyle.GLOW_ANIMATED) &&
                !gradientColorsCache.containsKey(incomingBackgroundItem.id)
            ) {
                val reqGradient = ImageRequest.Builder(context)
                    .data(nextThumbnail)
                    .size(100, 100)
                    .allowHardware(false)
                    .memoryCacheKey("gradient_${incomingBackgroundItem.id}")
                    .build()
                val result = runCatching { context.imageLoader.execute(reqGradient) }.getOrNull()
                val bitmap = result?.image?.toBitmap()
                if (bitmap != null) {
                    val palette = withContext(Dispatchers.Default) {
                        Palette.from(bitmap)
                            .maximumColorCount(8)
                            .resizeBitmapArea(100 * 100)
                            .generate()
                    }
                    val extractedColors = if (playerBackground == PlayerBackgroundStyle.GLOW_ANIMATED) {
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
                    gradientColorsCache[incomingBackgroundItem.id] = extractedColors
                }
            }
        }
    }


    val TextBackgroundColor by animateColorAsState(
        targetValue = when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onBackground
            PlayerBackgroundStyle.BLUR -> Color.White
            PlayerBackgroundStyle.GRADIENT -> Color.White
            PlayerBackgroundStyle.GLOW_ANIMATED -> Color.White
            PlayerBackgroundStyle.APPLE_MUSIC -> Color.White
            PlayerBackgroundStyle.LIVE_MESH -> Color.White
        },
        label = "TextBackgroundColor"
    )

    val icBackgroundColor by animateColorAsState(
        targetValue = when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.surface
            PlayerBackgroundStyle.BLUR -> Color.Black
            PlayerBackgroundStyle.GRADIENT -> Color.Black
            PlayerBackgroundStyle.GLOW_ANIMATED -> Color.Black
            PlayerBackgroundStyle.APPLE_MUSIC -> Color.Black
            PlayerBackgroundStyle.LIVE_MESH -> Color.Black
        },
        label = "icBackgroundColor"
    )

    val albumTitle = mediaMetadata?.album?.title
    var canvasArtwork by remember(mediaMetadata?.id, albumTitle) { mutableStateOf<Motionartwork?>(null) }
    var canvasFetchInFlight by remember(mediaMetadata?.id, albumTitle) { mutableStateOf(false) }
    var spotifyCanvasUrl by remember(mediaMetadata?.id, albumTitle) { mutableStateOf<String?>(null) }
    
    val isNetworkConnected by (LocalPlayerConnection.current?.service?.connectivityObserver?.networkStatus ?: MutableStateFlow(false)).collectAsState(initial = false)

    LaunchedEffect(mediaMetadata?.id, albumTitle, playerBackground, motionartworkSource, spotifyCanvasEnabled, Spotify.accessToken, isNetworkConnected, enableCanvas, useNewPlayerDesign) {
        val item = mediaMetadata ?: return@LaunchedEffect
        
        if (!spotifyCanvasEnabled || !useNewPlayerDesign) {
            spotifyCanvasUrl = null
        } else {
            val prefs = context.getSharedPreferences("spotify_canvas_url_map", Context.MODE_PRIVATE)
            val cachedUrl = prefs.getString(item.id, null)
            if (cachedUrl != null) {
                spotifyCanvasUrl = cachedUrl
                timber.log.Timber.d("SpotifyCanvas: Loaded url from local SharedPreferences cache for offline bypass - ID: ${item.id}")
            } else {
                val accessToken = SpotifySessionManager.getValidAccessToken(context)
                if (accessToken != null && (!canvasLoadOnlyWifi || isWifiConnected(context))) {
                    timber.log.Timber.d("SpotifyCanvas: Triggering Canvas fetch for track: ${item.title}")
                    withContext(Dispatchers.IO) {
                        // Try REST API first as it's more reliable
                        val trackId = Spotify.searchTrackRest("${item.title} ${item.artists.joinToString { it.name }}") 
                            ?: Spotify.searchTrack("${item.title} ${item.artists.joinToString { it.name }}")?.id

                        timber.log.Timber.d("SpotifyCanvas: Resolved Track ID from search: $trackId")

                        if (trackId != null) {
                            val canvasRes = SpotifyCanvasProvider.getCanvasUrl(trackId, accessToken)
                            timber.log.Timber.d("SpotifyCanvas: Canvas URL extraction result: $canvasRes")
                            val fetchedUrl = canvasRes.getOrNull()
                            if (fetchedUrl != null) {
                                prefs.edit().putString(item.id, fetchedUrl).apply()
                            }
                            withContext(Dispatchers.Main) {
                                spotifyCanvasUrl = fetchedUrl
                            }
                        }
                    }
                } else {
                    timber.log.Timber.d("SpotifyCanvas: Conditions not met to fetch. canvasEnabled=$spotifyCanvasEnabled, tokenReady=${Spotify.accessToken != null}, itemReady=${item != null}")
                }
            }
        }

        if (!enableCanvas) {
            canvasArtwork = null
            return@LaunchedEffect
        }
        if (canvasLoadOnlyWifi && !isWifiConnected(context)) {
            canvasArtwork = null
            return@LaunchedEffect
        }
        
        val cacheKey = "${item.id}:${motionartworkSource.name}"
        // Use cached artwork if available
        MotionartworkPlaybackCache.get(cacheKey)?.let { cached ->
            canvasArtwork = cached
            spotifyCanvasUrl = null // Prioritize Motionartwork, disable Spotify Canvas
            return@LaunchedEffect
        }

        if (canvasFetchInFlight) return@LaunchedEffect
        canvasFetchInFlight = true
        
        withContext(Dispatchers.IO) {
            val storefront = Locale.getDefault().country.lowercase(Locale.ROOT).takeIf { it.length == 2 } ?: "us"
            val requestedTitle = item.title
            val requestedArtist = item.artists.joinToString { it.name }
            val requestedAlbum = item.album?.title ?: ""

            val s = requestedTitle
            val a = requestedArtist

            android.util.Log.d("CanvasDebug", "=== Canvas Fetch START ===")
            android.util.Log.d("CanvasDebug", "Source     : $motionartworkSource")
            android.util.Log.d("CanvasDebug", "Storefront : $storefront")
            android.util.Log.d("CanvasDebug", "Title      : '$requestedTitle'")
            android.util.Log.d("CanvasDebug", "Artist     : '$requestedArtist'")
            android.util.Log.d("CanvasDebug", "Album      : '$requestedAlbum' (blank=${requestedAlbum.isBlank()})")

            val fetched = when (motionartworkSource) {
                MotionartworkSource.AUTO -> {
                    AppleMusicMotionartworkProvider.getBySongArtist(s, a, requestedAlbum, storefront)
                        ?.takeIf { !it.preferredAnimationUrl.isNullOrBlank() && validateMotionartworkMatch(it, s, a, requestedAlbum) }
                        ?: TidalMotionartworkProvider.getBySongArtist(s, a, requestedAlbum)
                        ?.takeIf { !it.preferredAnimationUrl.isNullOrBlank() && validateMotionartworkMatch(it, s, a, requestedAlbum) }
                        ?: ViviMusicMotionartworkProvider.getBySongArtist(s, a, requestedAlbum)
                        ?.takeIf { !it.preferredAnimationUrl.isNullOrBlank() && validateMotionartworkMatch(it, s, a, requestedAlbum) }
                }
                MotionartworkSource.APPLE_MUSIC -> {
                    AppleMusicMotionartworkProvider.getBySongArtist(s, a, requestedAlbum, storefront)
                        ?.takeIf { !it.preferredAnimationUrl.isNullOrBlank() }
                }
                MotionartworkSource.VIVIMUSIC -> {
                    ViviMusicMotionartworkProvider.getBySongArtist(s, a, requestedAlbum)
                        ?.takeIf { !it.preferredAnimationUrl.isNullOrBlank() }
                }
                MotionartworkSource.TIDAL -> {
                    TidalMotionartworkProvider.getBySongArtist(s, a, requestedAlbum)
                        ?.takeIf { !it.preferredAnimationUrl.isNullOrBlank() }
                }
            }

            android.util.Log.d("CanvasDebug", "Fetched artwork: ${if (fetched == null) "NULL" else "non-null"}")
            if (fetched != null) {
                android.util.Log.d("CanvasDebug", "  artwork.name      : '${fetched.name}'")
                android.util.Log.d("CanvasDebug", "  artwork.artist    : '${fetched.artist}'")
                android.util.Log.d("CanvasDebug", "  artwork.albumName : '${fetched.albumName}'")
                android.util.Log.d("CanvasDebug", "  artwork.animUrl   : '${fetched.preferredAnimationUrl}'")
            }

            val validated = fetched?.let { artwork ->
                // For AUTO mode validation is already done per-provider inside takeIf.
                // For single-source modes this block acts as the safety net.
                val passes = validateMotionartworkMatch(artwork, requestedTitle, requestedArtist, requestedAlbum)

                android.util.Log.d("CanvasDebug", "Validation:")
                android.util.Log.d("CanvasDebug", "  artistMatches  : ${artwork.artist.orEmpty().trim().equals(requestedArtist.trim(), ignoreCase = true)}  ('${artwork.artist?.trim()}' vs '${requestedArtist.trim()}')")
                android.util.Log.d("CanvasDebug", "  songMatches    : ${artwork.name.orEmpty().trim().equals(requestedTitle.trim(), ignoreCase = true)}  ('${artwork.name?.trim()}' vs '${requestedTitle.trim()}')")
                android.util.Log.d("CanvasDebug", "  albumMatches   : ${artwork.albumName.orEmpty().trim().equals(requestedAlbum.trim(), ignoreCase = true)}  ('${artwork.albumName?.trim()}' vs '${requestedAlbum.trim()}')")
                android.util.Log.d("CanvasDebug", "  RESULT         : $passes")

                if (passes) artwork else null
            }

            android.util.Log.d("CanvasDebug", "Final validated: ${if (validated == null) "NULL (canvas will NOT show)" else "OK (canvas will show)"}")
            android.util.Log.d("CanvasDebug", "=== Canvas Fetch END ===")

            withContext(Dispatchers.Main) {
                canvasArtwork = validated
                if (validated != null) {
                    MotionartworkPlaybackCache.put(cacheKey, validated)
                    spotifyCanvasUrl = null // Prioritize Motionartwork, disable Spotify Canvas
                }
                canvasFetchInFlight = false
            }
        }
    }


    val (textButtonColor, iconButtonColor) = when {
        playerBackground == PlayerBackgroundStyle.BLUR || 
        playerBackground == PlayerBackgroundStyle.GRADIENT ||
        playerBackground == PlayerBackgroundStyle.GLOW_ANIMATED ||
        playerBackground == PlayerBackgroundStyle.APPLE_MUSIC ||
        playerBackground == PlayerBackgroundStyle.LIVE_MESH -> {
            when (playerButtonsStyle) {
                PlayerButtonsStyle.DEFAULT -> Pair(Color.White, Color.Black)
                PlayerButtonsStyle.PRIMARY -> Pair(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.onPrimary
                )
                PlayerButtonsStyle.TERTIARY -> Pair(
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.onTertiary
                )
            }
        }
        else -> {
            when (playerButtonsStyle) {
                PlayerButtonsStyle.DEFAULT ->
                    if (useDarkTheme) Pair(Color.White, Color.Black)
                    else Pair(Color.Black, Color.White)
                PlayerButtonsStyle.PRIMARY -> Pair(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.onPrimary
                )
                PlayerButtonsStyle.TERTIARY -> Pair(
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.onTertiary
                )
            }
        }
    }


    // Separate colors for Previous/Next buttons in PRIMARY/TERTIARY modes
    val (sideButtonContainerColor, sideButtonContentColor) = when {
        playerBackground == PlayerBackgroundStyle.BLUR || 
        playerBackground == PlayerBackgroundStyle.GRADIENT -> {
            when (playerButtonsStyle) {
                PlayerButtonsStyle.DEFAULT -> Pair(
                    Color.White.copy(alpha = 0.2f), 
                    Color.White
                )
                PlayerButtonsStyle.PRIMARY -> Pair(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
                PlayerButtonsStyle.TERTIARY -> Pair(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        playerBackground == PlayerBackgroundStyle.GLOW_ANIMATED -> {
            when (playerButtonsStyle) {
                PlayerButtonsStyle.DEFAULT -> Pair(
                    Color.White.copy(alpha = 0.2f), 
                    Color.White
                )
                PlayerButtonsStyle.PRIMARY -> Pair(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
                PlayerButtonsStyle.TERTIARY -> Pair(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        else -> {
            when (playerButtonsStyle) {
                PlayerButtonsStyle.DEFAULT -> Pair(
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    MaterialTheme.colorScheme.onSurface
                )
                PlayerButtonsStyle.PRIMARY -> Pair(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
                PlayerButtonsStyle.TERTIARY -> Pair(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }


    val download by LocalDownloadUtil.current.getDownload(mediaMetadata?.id ?: "")
        .collectAsState(initial = null)

    val sleepTimerEnabled =
        remember(
            playerConnection.service.sleepTimer.triggerTime,
            playerConnection.service.sleepTimer.pauseWhenSongEnd
        ) {
            playerConnection.service.sleepTimer.isActive
        }

    var sleepTimerTimeLeft by remember {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(sleepTimerEnabled) {
        if (sleepTimerEnabled) {
            while (isActive) {
                sleepTimerTimeLeft =
                    if (playerConnection.service.sleepTimer.pauseWhenSongEnd) {
                        playerConnection.player.duration - playerConnection.player.currentPosition
                    } else {
                        playerConnection.service.sleepTimer.triggerTime - System.currentTimeMillis()
                    }
                delay(1000L)
            }
        }
    }


    var showChoosePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showInlineLyrics by rememberSaveable {
        mutableStateOf(false)
    }

    var showInlineQueue by rememberSaveable {
        mutableStateOf(false)
    }

    BackHandler(enabled = (showInlineLyrics || showInlineQueue) && !useNewPlayerDesign) {
        showInlineLyrics = false
        showInlineQueue = false
    }

    val queueBeside = LocalAdaptiveLayout.current.playerQueueBeside
    LaunchedEffect(queueBeside) {
        if (queueBeside) showInlineQueue = false
    }

    var isFullScreen by rememberSaveable {
        mutableStateOf(false)
    }

    // Position update - only for local playback
    // When casting, we use castPosition directly to avoid sync issues
    // Use isPlaying instead of playbackState to ensure continuous updates during playback
    LaunchedEffect(isPlaying, isCasting) {
        if (!isCasting && isPlaying) {
            while (isActive) {
                delay(100) // Update more frequently for smoother progress bar
                if (sliderPosition == null) { // Only update if user isn't dragging
                    position = playerConnection.player.currentPosition
                    duration = playerConnection.player.duration
                }
            }
        }
    }

    
    // Also update position when playback state changes (e.g., song change, seek)
    LaunchedEffect(playbackState, mediaMetadata?.id) {
        if (!isCasting) {
            position = playerConnection.player.currentPosition
            duration = playerConnection.player.duration
        }
    }

    
    // When casting, use Cast position/duration directly
    // But wait a bit after manual seeks to let Cast catch up
    LaunchedEffect(isCasting, castPosition, castDuration) {
        if (isCasting && sliderPosition == null) {
            val timeSinceManualSeek = System.currentTimeMillis() - lastManualSeekTime
            if (timeSinceManualSeek > 1500) {
                // Only update from Cast if we haven't manually seeked recently
                position = castPosition
                if (castDuration > 0) duration = castDuration
            }
        }
    }


    val dismissedBound = QueuePeekHeight + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

    val queueSheetState = rememberBottomSheetState(
        dismissedBound = if (useNewPlayerDesign) dismissedBound else 0.dp,
        expandedBound = state.expandedBound,
        collapsedBound = if (useNewPlayerDesign) dismissedBound + 1.dp else 0.dp,
        initialAnchor = if (useNewPlayerDesign) 1 else 0
    )

    val bottomSheetBackgroundColor = when {
        useNewPlayerDesign && spotifyCanvasUrl != null -> Color.Black
        playerBackground == PlayerBackgroundStyle.BLUR ||
        playerBackground == PlayerBackgroundStyle.GRADIENT ||
        playerBackground == PlayerBackgroundStyle.GLOW_ANIMATED ||
        playerBackground == PlayerBackgroundStyle.APPLE_MUSIC -> Color.Black
        playerBackground == PlayerBackgroundStyle.LIVE_MESH ->
            if (useDarkTheme) Color(0xFF191919) else Color(0xFFE6E6E6)
        else ->
            if (useBlackBackground) Color.Black
            else MaterialTheme.colorScheme.surfaceContainer
    }

    val backgroundAlpha = state.progress.coerceIn(0f, 1f)

    // Pull-down-to-collapse is vertical only, so it's off when the sheet slides in from the side.
    val sheetNestedScroll = if (slideFromStart) {
        Modifier
    } else {
        Modifier.nestedScroll(state.preUpPostDownNestedScrollConnection)
    }

    BottomSheet(
        state = state,
        modifier = modifier,
        slideFromStart = slideFromStart,
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bottomSheetBackgroundColor)
            ) {
                if (useNewPlayerDesign && spotifyCanvasUrl != null) {
                    val lyricsBlurAlpha by animateFloatAsState(
                        targetValue = if (showInlineLyrics || showInlineQueue) 1f else 0f,
                        animationSpec = tween(400),
                        label = "canvasLyricsBlur"
                    )
                    AnimatedContent(
                        targetState = spotifyCanvasUrl,
                        transitionSpec = { fadeIn(tween(800)) togetherWith fadeOut(tween(800)) },
                        label = "spotifyCanvasBackground"
                    ) { url ->
                        if (url != null) {
                            val canvasOnStartHalf = LocalAdaptiveLayout.current.playerQueueBeside
                            Box(modifier = Modifier.fillMaxSize().alpha(backgroundAlpha)) {
                                if (canvasOnStartHalf) {
                                    PlayerBlurBackdrop(
                                        artworkUri = mediaMetadata?.thumbnailUrl?.toUri(),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                if (backgroundAlpha > 0.01f) {
                                    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                                    BackgroundVideoView(
                                        videoUrl = url,
                                        isPlaying = isPlaying,
                                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                                        modifier = if (canvasOnStartHalf) {
                                            Modifier
                                                .fillMaxWidth(0.5f)
                                                .fillMaxHeight()
                                                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                                                .drawWithContent {
                                                    drawContent()
                                                    val stops = arrayOf(
                                                        0.00f to Color.Black,
                                                        0.75f to Color.Black,
                                                        0.92f to Color.Black.copy(alpha = 0.4f),
                                                        1.00f to Color.Transparent,
                                                    )
                                                    drawRect(
                                                        brush = Brush.horizontalGradient(
                                                            colorStops = if (isRtl) {
                                                                stops.reversedArray().map { (f, c) -> 1f - f to c }.toTypedArray()
                                                            } else {
                                                                stops
                                                            }
                                                        ),
                                                        blendMode = BlendMode.DstIn
                                                    )
                                                }
                                        } else {
                                            Modifier.fillMaxSize()
                                        }
                                    )
                                }
                                if (lyricsBlurAlpha > 0.01f && !canvasOnStartHalf) {
                                    PlayerBlurBackdrop(
                                        artworkUri = mediaMetadata?.thumbnailUrl?.toUri(),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .alpha(lyricsBlurAlpha)
                                    )
                                }
                            }
                        }
                    }
                } else when (playerBackground) {
                    PlayerBackgroundStyle.BLUR -> {
                        PlayerBlurBackdrop(
                            artworkUri = mediaMetadata?.thumbnailUrl?.toUri(),
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(backgroundAlpha)
                        )
                    }
                    PlayerBackgroundStyle.GRADIENT -> {
                        AnimatedContent(
                            targetState = gradientColors,
                            transitionSpec = {
                                fadeIn(tween(800)).togetherWith(fadeOut(tween(800)))
                            },
                            label = "gradientBackground"
                        ) { colors ->
                            if (colors.isNotEmpty()) {
                                val gradientColorStops = if (colors.size >= 3) {
                                    arrayOf(
                                        0.0f to colors[0],
                                        0.5f to colors[1],
                                        1.0f to colors[2]
                                    )
                                } else {
                                    arrayOf(
                                        0.0f to colors[0],
                                        0.6f to colors[0].copy(alpha = 0.7f),
                                        1.0f to Color.Black
                                    )
                                }
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .alpha(backgroundAlpha)
                                        .background(Brush.verticalGradient(colorStops = gradientColorStops))
                                        .background(Color.Black.copy(alpha = 0.2f))
                                )
                            }
                        }
                    }
                    PlayerBackgroundStyle.GLOW_ANIMATED -> {
                        AnimatedContent(
                            targetState = gradientColors,
                            transitionSpec = {
                                fadeIn(tween(1200)) togetherWith fadeOut(tween(1200))
                            },
                            label = "GlowAnimatedContent"
                        ) { colors ->
                            if (colors.isNotEmpty()) {
                                val infiniteTransition =
                                    rememberInfiniteTransition(label = "GlowAnimation")

                                val progress by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(20000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                    ),
                                    label = "glowProgress"
                                )

                                fun rotatedColorAt(index: Int): Color {
                                    val size = colors.size
                                    val idx = index.toFloat() + progress * size
                                    val a = kotlin.math.floor(idx).toInt() % size
                                    val b = (a + 1) % size
                                    val frac = idx - kotlin.math.floor(idx)
                                    return colorLerp(
                                        colors.getOrElse(a) { Color.DarkGray },
                                        colors.getOrElse(b) { Color.DarkGray },
                                        frac
                                    )
                                }

                                fun oscillate(
                                    min: Float,
                                    max: Float,
                                    phase: Float,
                                    speed: Float = 1f
                                ): Float {
                                    val v = kotlin.math.sin(
                                        2f * kotlin.math.PI.toFloat() * (progress * speed + phase)
                                    )
                                    return min + (max - min) * ((v + 1f) * 0.5f)
                                }

                                val color1 = rotatedColorAt(0)
                                val color2 = rotatedColorAt(1)
                                val color3 = rotatedColorAt(2)
                                val color4 = rotatedColorAt(3)
                                val color5 = rotatedColorAt(4)
                                val color6 = rotatedColorAt(5)

                                val o1x = oscillate(0.0f, 1.0f, 0.00f, 1.0f)
                                val o1y = oscillate(0.0f, 0.5f, 0.07f, 1.0f)
                                val r1 = oscillate(0.8f, 1.6f, 0.12f, 1.0f)

                                val o2x = oscillate(1.0f, 0.0f, 0.2f, 1.0f)
                                val o2y = oscillate(0.5f, 1.0f, 0.25f, 1.0f)
                                val r2 = oscillate(0.7f, 1.5f, 0.18f, 1.0f)

                                val o3x = oscillate(0.2f, 0.8f, 0.33f, 1.0f)
                                val o3y = oscillate(0.8f, 0.2f, 0.36f, 1.0f)
                                val r3 = oscillate(0.6f, 1.4f, 0.29f, 1.0f)

                                val o4x = oscillate(0.3f, 0.7f, 0.44f, 1.0f)
                                val o4y = oscillate(0.2f, 0.8f, 0.41f, 1.0f)
                                val r4 = oscillate(0.9f, 1.7f, 0.47f, 1.0f)

                                val o5x = oscillate(0.4f, 0.6f, 0.55f, 1.0f)
                                val o5y = oscillate(0.0f, 1.0f, 0.51f, 1.0f)
                                val r5 = oscillate(0.7f, 1.5f, 0.58f, 1.0f)

                                val o6x = oscillate(0.0f, 1.0f, 0.66f, 1.0f)
                                val o6y = oscillate(0.5f, 0.7f, 0.62f, 1.0f)
                                val r6 = oscillate(0.8f, 1.8f, 0.69f, 1.0f)

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(backgroundAlpha)
                                        .drawWithCache {
                                            val width = size.width
                                            val height = size.height
                                            val baseColor = Color(0xFF050505)

                                            val brush1 = Brush.radialGradient(
                                                colors = listOf(
                                                    color1.copy(alpha = 0.85f),
                                                    color1.copy(alpha = 0.5f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(width * o1x, height * o1y),
                                                radius = width * r1
                                            )
                                            val brush2 = Brush.radialGradient(
                                                colors = listOf(
                                                    color2.copy(alpha = 0.8f),
                                                    color2.copy(alpha = 0.45f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(width * o2x, height * o2y),
                                                radius = width * r2
                                            )
                                            val brush3 = Brush.radialGradient(
                                                colors = listOf(
                                                    color3.copy(alpha = 0.75f),
                                                    color3.copy(alpha = 0.4f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(width * o3x, height * o3y),
                                                radius = width * r3
                                            )
                                            val brush4 = Brush.radialGradient(
                                                colors = listOf(
                                                    color4.copy(alpha = 0.7f),
                                                    color4.copy(alpha = 0.35f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(width * o4x, height * o4y),
                                                radius = width * r4
                                            )
                                            val brush5 = Brush.radialGradient(
                                                colors = listOf(
                                                    color5.copy(alpha = 0.65f),
                                                    color5.copy(alpha = 0.3f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(width * o5x, height * o5y),
                                                radius = width * r5
                                            )
                                            val brush6 = Brush.radialGradient(
                                                colors = listOf(
                                                    color6.copy(alpha = 0.6f),
                                                    color6.copy(alpha = 0.25f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(width * o6x, height * o6y),
                                                radius = width * r6
                                            )

                                            onDrawBehind {
                                                drawRect(color = baseColor)
                                                drawRect(brush = brush1)
                                                drawRect(brush = brush2)
                                                drawRect(brush = brush3)
                                                drawRect(brush = brush4)
                                                drawRect(brush = brush5)
                                                drawRect(brush = brush6)
                                            }
                                        }
                                )
                            }
                        }
                    }
                    PlayerBackgroundStyle.APPLE_MUSIC -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(backgroundAlpha)
                        ) {
                            // Layer 1: Full-Screen Blurred Backdrop (Cached & persistent across song changes - zero flicker)
                            val blurBitmap = rememberArtworkBlurBitmap(mediaMetadata?.thumbnailUrl?.toUri())
                            Crossfade(
                                targetState = blurBitmap,
                                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                                label = "appleMusicBlurCrossfade"
                            ) { bitmap ->
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            // Layer 1.5: Artwork Depth Scrim
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            0.00f to Color.Black.copy(alpha = 0.34f),
                                            0.55f to Color.Black.copy(alpha = 0.48f),
                                            1.00f to Color.Black.copy(alpha = 0.64f)
                                        )
                                    )
                            )

                            // Layer 2: Clear Artwork (Dissolves along smootherStep into backdrop)
                            val artworkOnLeftHalf = LocalAdaptiveLayout.current.playerQueueBeside
                            if (useNewPlayerDesign || artworkOnLeftHalf) {
                                val clearArtworkAlpha by animateFloatAsState(
                                    targetValue = if ((showInlineLyrics || showInlineQueue) && !artworkOnLeftHalf) 0f else 1f,
                                    animationSpec = tween(500),
                                    label = "clearArtworkAlpha"
                                )
                                Crossfade(
                                    targetState = mediaMetadata?.thumbnailUrl,
                                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                                    label = "appleMusicClearArtworkCrossfade"
                                ) { thumbnailUrl ->
                                    if (thumbnailUrl != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(if (artworkOnLeftHalf) 0.5f else 1f)
                                                .fillMaxHeight(0.65f) // Occupies top 65%
                                                .alpha(clearArtworkAlpha)
                                                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                                                .drawWithContent {
                                                    drawContent()
                                                    // Along smootherstep, so neither end of the dissolve shows as a line
                                                    drawRect(
                                                        brush = Brush.verticalGradient(
                                                            colorStops = Array(17) { i ->
                                                                val t = i / 16f
                                                                t to Color.Black.copy(alpha = 1f - smootherStep(t))
                                                            },
                                                            startY = size.height * (1f - 0.42f),
                                                            endY = size.height,
                                                        ),
                                                        blendMode = BlendMode.DstIn
                                                    )
                                                    if (artworkOnLeftHalf) {
                                                        drawRect(
                                                            brush = Brush.horizontalGradient(
                                                                colorStops = arrayOf(
                                                                    0.00f to Color.Black,
                                                                    0.75f to Color.Black,
                                                                    0.92f to Color.Black.copy(alpha = 0.4f),
                                                                    1.00f to Color.Transparent,
                                                                )
                                                            ),
                                                            blendMode = BlendMode.DstIn
                                                        )
                                                    }
                                                }
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(thumbnailUrl)
                                                    .size(CoilSize.ORIGINAL)
                                                    .crossfade(600)
                                                    .build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )

                                            if (enableCanvas && canvasArtwork != null && backgroundAlpha > 0.01f) {
                                                BackgroundVideoView(
                                                    videoUrl = canvasArtwork?.animatedTall ?: canvasArtwork?.animated ?: canvasArtwork?.videoUrl ?: "",
                                                    isPlaying = isPlaying,
                                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    PlayerBackgroundStyle.LIVE_MESH -> {
                        if (backgroundAlpha > 0.01f) {
                            val thumbnailUrl = mediaMetadata?.thumbnailUrl
                            AndroidView<BlendView>(factory = { ctx -> BlendView(ctx) }, update = { blendView -> if (isPlaying) blendView.startRotationAnimation() else blendView.stopRotationAnimation(); blendView.setImageUrl(thumbnailUrl) }, modifier = Modifier.fillMaxSize())
                        }
                    }
                    PlayerBackgroundStyle.DEFAULT -> {
                        // Nothing
                    }
                }
            }
        },
        onDismiss = {
            playerConnection.service.clearAutomix()
            playerConnection.player.stop()
            playerConnection.player.clearMediaItems()
        },
        collapsedContent = {
            MiniPlayer(
                positionState = positionState,
                durationState = durationState
            )
        },
    ) {
        val density = LocalDensity.current
        var trackInfoHeightDp by remember { mutableStateOf(56.dp) }
        val trackInfoSpacerHeight = 20.dp

        val controlsContent: @Composable ColumnScope.(MediaMetadata) -> Unit = { mediaMetadata ->
            val trackInfoContent: @Composable () -> Unit = {
                val controlsHeaderAlpha by animateFloatAsState(
                    targetValue = if (!useNewPlayerDesign && !LocalAdaptiveLayout.current.playerQueueBeside && (showInlineLyrics || showInlineQueue)) 0f else 1f,
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                    label = "controlsHeaderAlpha",
                )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding)
                    .onSizeChanged { size ->
                        if (size.height > 0) {
                            trackInfoHeightDp = with(density) { size.height.toDp() }
                        }
                    }
                    .graphicsLayer { alpha = controlsHeaderAlpha },
            ) {
                AnimatedContent(
                    targetState = useNewPlayerDesign && ((showInlineLyrics && !LocalAdaptiveLayout.current.playerQueueBeside) || showInlineQueue || spotifyCanvasUrl != null),
                    label = "ThumbnailAnimation"
                ) { showSmallThumbnail ->
                    if (showSmallThumbnail) {
                        Row {
                            if (hidePlayerThumbnail) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.vivi_music_small_icon),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp),
                                        tint = textButtonColor.copy(alpha = 0.7f)
                                    )
                                }
                            } else {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                        .clickable(enabled = isFullScreen && enableLyricsThumbnailPlayPause) {
                                            playerConnection.togglePlayPause()
                                        }
                                ) {
                                    val isYtThumbnail = remember(mediaMetadata.thumbnailUrl) {
                                        mediaMetadata.thumbnailUrl?.let { it.contains("ytimg") || it.contains("/vi/") || it.contains("youtube.com") } == true
                                    }
                                    AsyncImage(
                                        model = mediaMetadata.thumbnailUrl,
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

                                    if (isFullScreen && enableLyricsThumbnailPlayPause) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = if (isPlaying) 0f else 0.4f))
                                        )

                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = !isPlaying,
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            Icon(
                                                painter = painterResource(
                                                    if (playbackState == Player.STATE_ENDED) R.drawable.replay
                                                    else R.drawable.play
                                                ),
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(0.dp))
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .SwipeGesture(
                            enabled = isFullScreen && swipeLyrics,
                            onSwipeRight = { playerConnection.seekToPrevious() },
                            onSwipeLeft = { playerConnection.seekToNext() }
                        )
                ) {
                    AnimatedContent(
                        targetState = mediaMetadata.title,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "",
                    ) { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = TextBackgroundColor,
                            modifier =
                            Modifier
                                .basicMarquee(iterations = 1, initialDelayMillis = 3000, velocity = 30.dp)
                                .combinedClickable(
                                    enabled = controlsHeaderAlpha > 0.5f,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        if (mediaMetadata.album != null) {
                                            navController.navigate("album/${mediaMetadata.album.id}")
                                            state.collapseSoft()
                                        }
                                    },
                                    onLongClick = {
                                        val clip = ClipData.newPlainText(context.getString(R.string.copied_title), title)
                                        clipboardManager.setPrimaryClip(clip)
                                        Toast
                                            .makeText(context, context.getString(R.string.copied_title), Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                )
                            ,
                        )
                    }

                    Spacer(Modifier.height(if (useNewPlayerDesign) 4.dp else 2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (mediaMetadata.explicit) {
                            MIcon.Explicit(
                                tint = if (useNewPlayerDesign) TextBackgroundColor else TextBackgroundColor.copy(alpha = 0.65f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        if (mediaMetadata.artists.any { it.name.isNotBlank() }) {
                            val artistColor = if (useNewPlayerDesign) TextBackgroundColor else TextBackgroundColor.copy(alpha = 0.65f)
                            val artistFontWeight = if (useNewPlayerDesign) FontWeight.Normal else FontWeight.SemiBold
                            val annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(color = artistColor, fontSize = 16.sp, fontWeight = artistFontWeight)) {
                                    mediaMetadata.artists.forEachIndexed { index, artist ->
                                        val tag = "artist_${artist.id.orEmpty()}"
                                        pushStringAnnotation(tag = tag, annotation = artist.id.orEmpty())
                                        append(artist.name)
                                        pop()
                                        if (index != mediaMetadata.artists.lastIndex) append(", ")
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .basicMarquee(iterations = 1, initialDelayMillis = 3000, velocity = 30.dp)
                            ) {
                                var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                                var clickOffset by remember { mutableStateOf<Offset?>(null) }
                                Text(
                                    text = annotatedString,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = artistColor,
                                        fontSize = 16.sp,
                                        fontWeight = artistFontWeight
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    onTextLayout = { layoutResult = it },
                                    modifier = Modifier
                                        .pointerInput(Unit) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    val tapPosition = event.changes.firstOrNull()?.position
                                                    if (tapPosition != null) {
                                                        clickOffset = tapPosition
                                                    }
                                                }
                                            }
                                        }
                                        .combinedClickable(
                                            enabled = controlsHeaderAlpha > 0.5f,
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = {
                                                val tapPosition = clickOffset
                                                val layout = layoutResult
                                                if (tapPosition != null && layout != null) {
                                                    val offset = layout.getOffsetForPosition(tapPosition)
                                                    annotatedString
                                                        .getStringAnnotations(offset, offset)
                                                        .firstOrNull()
                                                        ?.let { ann ->
                                                            val artistId = ann.item
                                                            if (artistId.isNotBlank()) {
                                                                navController.navigate("artist/$artistId")
                                                                state.collapseSoft()
                                                            }
                                                        }
                                                }
                                            },
                                            onLongClick = {
                                                val clip =
                                                    ClipData.newPlainText(
                                                        context.getString(R.string.copied_artist),
                                                        annotatedString
                                                    )
                                                clipboardManager.setPrimaryClip(clip)
                                                Toast
                                                    .makeText(
                                                        context,
                                                        context.getString(R.string.copied_artist),
                                                        Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (useNewPlayerDesign) {
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
                                    onClick = { isFullScreen = !isFullScreen },
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
                                            LyricsMenu(
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
                } else {
                    val isLiked = currentSong?.song?.liked == true
                    // Matches controlsHeaderAlpha: the header only hides over lyrics/queue when they aren't docked beside it.
                    val headerActionsEnabled = LocalAdaptiveLayout.current.playerQueueBeside ||
                        (!showInlineLyrics && !showInlineQueue)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(TextBackgroundColor.copy(alpha = 0.14f))
                                .clickable(
                                    enabled = headerActionsEnabled,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = playerConnection::toggleLike,
                                ),
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (isLiked)
                                        R.drawable.star_apple
                                    else
                                        R.drawable.star_outline_apple
                                ),
                                contentDescription = if (isLiked) "Unlike" else "Like",
                                tint = TextBackgroundColor,
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(TextBackgroundColor.copy(alpha = 0.14f))
                                .clickable(
                                    enabled = headerActionsEnabled,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
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
                                ),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ellipsis_horizontal),
                                contentDescription = "Options",
                                tint = TextBackgroundColor,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }

        val sliderContent: @Composable () -> Unit = {
            val effectiveSliderStyle = if (!useNewPlayerDesign) SliderStyle.THIN else sliderStyle
            when (effectiveSliderStyle) {
                SliderStyle.DEFAULT -> {
                    Slider(
                        value = (sliderPosition ?: effectivePosition).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = {
                            if (!isListenTogetherGuest) {
                                sliderPosition = it.toLong()
                            }
                        },
                        onValueChangeFinished = {
                            if (!isListenTogetherGuest) {
                                sliderPosition?.let {
                                    // Ignore seeks while duration is unknown (track still
                                    // loading): the seek would be lost and the optimistic
                                    // position update would show a wrong slider position.
                                    if (duration != C.TIME_UNSET && duration > 0) {
                                        if (isCasting) {
                                            castHandler?.seekTo(it)
                                            lastManualSeekTime = System.currentTimeMillis()
                                        } else {
                                            playerConnection.player.seekTo(it)
                                        }
                                        position = it
                                    }
                                }
                                sliderPosition = null
                            }
                        },
                        enabled = !isListenTogetherGuest,
                        colors = PlayerSliderColors.getSliderColors(
                            activeColor = textButtonColor,
                            playerBackground = playerBackground,
                            useDarkTheme = useDarkTheme
                        ),
                        modifier = Modifier.padding(horizontal = PlayerHorizontalPadding),
                    )
                }

                SliderStyle.WAVY -> {
                    if (squigglySlider) {
                        SquigglySlider(
                            value = (sliderPosition ?: effectivePosition).toFloat(),
                            valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                            onValueChange = {
                                sliderPosition = it.toLong()
                            },
                            onValueChangeFinished = {
                                sliderPosition?.let {
                                    if (duration != C.TIME_UNSET && duration > 0) {
                                        if (isCasting) {
                                            castHandler?.seekTo(it)
                                            lastManualSeekTime = System.currentTimeMillis()
                                        } else {
                                            playerConnection.player.seekTo(it)
                                        }
                                        position = it
                                    }
                                }
                                sliderPosition = null
                            },
                            modifier = Modifier.padding(horizontal = PlayerHorizontalPadding),
                            colors = PlayerSliderColors.getSliderColors(
                                activeColor = textButtonColor,
                                playerBackground = playerBackground,
                                useDarkTheme = useDarkTheme
                            ),
                            isPlaying = effectiveIsPlaying,
                        )
                    } else {
                        WavySlider(
                            value = (sliderPosition ?: effectivePosition).toFloat(),
                            valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                            onValueChange = {
                                sliderPosition = it.toLong()
                            },
                            onValueChangeFinished = {
                                sliderPosition?.let {
                                    if (duration != C.TIME_UNSET && duration > 0) {
                                        if (isCasting) {
                                            castHandler?.seekTo(it)
                                            lastManualSeekTime = System.currentTimeMillis()
                                        } else {
                                            playerConnection.player.seekTo(it)
                                        }
                                        position = it
                                    }
                                }
                                sliderPosition = null
                            },
                            colors = PlayerSliderColors.getSliderColors(
                                activeColor = textButtonColor,
                                playerBackground = playerBackground,
                                useDarkTheme = useDarkTheme
                            ),
                            modifier = Modifier.padding(horizontal = PlayerHorizontalPadding),
                            isPlaying = effectiveIsPlaying
                        )
                    }
                }

                SliderStyle.SLIM -> {
                    val trackInteractionSource = remember { MutableInteractionSource() }
                    val isTrackDragged by trackInteractionSource.collectIsDraggedAsState()
                    val isTrackPressed by trackInteractionSource.collectIsPressedAsState()
                    val isTrackActive = (isTrackDragged || isTrackPressed) && !useNewPlayerDesign

                    val trackHeight by animateDpAsState(
                        targetValue = if (isTrackActive) 16.dp else 10.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "trackHeight"
                    )

                    Slider(
                        value = (sliderPosition ?: effectivePosition).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = {
                            if (!isListenTogetherGuest) {
                                sliderPosition = it.toLong()
                            }
                        },
                        onValueChangeFinished = {
                            if (!isListenTogetherGuest) {
                                sliderPosition?.let {
                                    if (duration != C.TIME_UNSET && duration > 0) {
                                        if (isCasting) {
                                            castHandler?.seekTo(it)
                                            lastManualSeekTime = System.currentTimeMillis()
                                        } else {
                                            playerConnection.player.seekTo(it)
                                        }
                                        position = it
                                    }
                                }
                                sliderPosition = null
                            }
                        },
                        enabled = !isListenTogetherGuest,
                        interactionSource = trackInteractionSource,
                        thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                        track = { sliderState ->
                            PlayerSliderTrack(
                                sliderState = sliderState,
                                trackHeight = trackHeight,
                                colors = PlayerSliderColors.getSliderColors(
                                    activeColor = textButtonColor,
                                    playerBackground = playerBackground,
                                    useDarkTheme = useDarkTheme
                                )
                            )
                        },
                        modifier = Modifier.padding(horizontal = PlayerHorizontalPadding)
                    )
                }

                SliderStyle.EXPRESSIVE -> {
                    ExpressiveWavySlider(
                        value = { (sliderPosition ?: effectivePosition).toFloat() },
                        onValueChange = {
                            if (!isListenTogetherGuest) {
                                sliderPosition = it.toLong()
                            }
                        },
                        onValueChangeFinished = {
                            if (!isListenTogetherGuest) {
                                sliderPosition?.let {
                                    if (duration != C.TIME_UNSET && duration > 0) {
                                        if (isCasting) {
                                            castHandler?.seekTo(it)
                                            lastManualSeekTime = System.currentTimeMillis()
                                        } else {
                                            playerConnection.player.seekTo(it)
                                        }
                                        position = it
                                    }
                                }
                                sliderPosition = null
                            }
                        },
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        enabled = !isListenTogetherGuest,
                        isPlaying = effectiveIsPlaying,
                        activeTrackColor = textButtonColor,
                        thumbColor = textButtonColor,
                        modifier = Modifier.padding(horizontal = PlayerHorizontalPadding)
                    )
                }

                SliderStyle.THIN -> {
                    ThinSlider(
                        value = (sliderPosition ?: effectivePosition).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = {
                            if (!isListenTogetherGuest) {
                                sliderPosition = it.toLong()
                            }
                        },
                        onValueChangeFinished = {
                            if (!isListenTogetherGuest) {
                                sliderPosition?.let {
                                    if (duration != C.TIME_UNSET && duration > 0) {
                                        if (isCasting) {
                                            castHandler?.seekTo(it)
                                            lastManualSeekTime = System.currentTimeMillis()
                                        } else {
                                            playerConnection.player.seekTo(it)
                                        }
                                        position = it
                                    }
                                }
                                sliderPosition = null
                            }
                        },
                        enabled = !isListenTogetherGuest,
                        colors = PlayerSliderColors.getSliderColors(
                            activeColor = textButtonColor,
                            playerBackground = playerBackground,
                            useDarkTheme = useDarkTheme
                        ),
                        modifier = Modifier.padding(horizontal = PlayerHorizontalPadding)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding + 4.dp),
            ) {
                Text(
                    text = makeTimeString(sliderPosition ?: effectivePosition),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextBackgroundColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextBackgroundColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        val newPlaybackControlsContent: @Composable () -> Unit = {
            M3ePlaybackControls(
                playerConnection = playerConnection,
                castHandler = castHandler,
                isCasting = isCasting,
                castIsPlaying = castIsPlaying,
                playbackState = playbackState,
                effectiveIsPlaying = effectiveIsPlaying,
                isMuted = isMuted,
                isListenTogetherGuest = isListenTogetherGuest,
                canSkipPrevious = canSkipPrevious,
                canSkipNext = canSkipNext,
                textButtonColor = textButtonColor,
                iconButtonColor = iconButtonColor,
                sideButtonContainerColor = sideButtonContainerColor,
                sideButtonContentColor = sideButtonContentColor,
            )
        }

        val oldPlaybackControlsContent: @Composable () -> Unit = {
            ClassicPlaybackControls(
                playerConnection = playerConnection,
                castHandler = castHandler,
                isCasting = isCasting,
                castIsPlaying = castIsPlaying,
                playbackState = playbackState,
                effectiveIsPlaying = effectiveIsPlaying,
                isMuted = isMuted,
                isListenTogetherGuest = isListenTogetherGuest,
                canSkipPrevious = canSkipPrevious,
                canSkipNext = canSkipNext,
                currentSong = currentSong,
                repeatMode = repeatMode,
                TextBackgroundColor = TextBackgroundColor,
            )
        }

        val volumeBarContent: @Composable () -> Unit = {
            ClassicVolumeBar(
                audioManager = audioManager,
                castHandler = castHandler,
                castVolume = castVolume,
                isCasting = isCasting,
                maxSystemVolume = maxSystemVolume,
                systemVolume = systemVolume,
                TextBackgroundColor = TextBackgroundColor,
            )
        }

        val oldBottomActionsContent: @Composable () -> Unit = {
            ClassicBottomActions(
                bluetoothDeviceName = bluetoothDeviceName,
                nameToShow = nameToShow,
                queueSheetState = queueSheetState,
                showInlineLyrics = showInlineLyrics,
                showInlineQueue = showInlineQueue,
                useNewPlayerDesign = useNewPlayerDesign,
                textButtonColor = textButtonColor,
                onShowInlineLyricsChange = { showInlineLyrics = it },
                onShowInlineQueueChange = { showInlineQueue = it },
                onShowAudioDevices = { showAudioDeviceBottomSheet = true },
            )
        }

        val isLandscape = LocalAdaptiveLayout.current.playerSideBySide
        val isOldPlayerPortrait = !useNewPlayerDesign && !isLandscape

        if (isOldPlayerPortrait) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp)
            ) {
                // Group 1: Track Info + Slider + Time
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val isCompact = !useNewPlayerDesign && (showInlineLyrics || showInlineQueue)
                    if (!isCompact) {
                        trackInfoContent()
                        Spacer(Modifier.height(trackInfoSpacerHeight))
                    }
                    sliderContent()
                }

                // Group 2: Playback Controls (Middle)
                AnimatedVisibility(
                    visible = !isFullScreen,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    oldPlaybackControlsContent()
                }

                // Group 3: Volume + Bottom Actions (Bottom)
                AnimatedVisibility(
                    visible = !isFullScreen,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        volumeBarContent()
                        Spacer(Modifier.height(42.dp))
                        oldBottomActionsContent()
                    }
                }
            }
        } else {
            trackInfoContent()

            Spacer(Modifier.height(if (useNewPlayerDesign) 24.dp else 20.dp))

            sliderContent()

            Spacer(Modifier.height(if (useNewPlayerDesign) 24.dp else 32.dp))

            AnimatedVisibility(
                visible = !isFullScreen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Column {
                    if (useNewPlayerDesign) {
                        newPlaybackControlsContent()
                    } else {
                        oldPlaybackControlsContent()
                        Spacer(modifier = Modifier.height(28.dp))
                        volumeBarContent()
                        if (!isLandscape) {
                            Spacer(Modifier.height(42.dp))
                            oldBottomActionsContent()
                        }
                    }
                }
            }
        }
    }

        val adaptiveLayout = LocalAdaptiveLayout.current
        val queueBackground = if (useBlackBackground) Color.Black else MaterialTheme.colorScheme.surface

        when {
            adaptiveLayout.playerQueueBeside -> {
                DockedQueuePlayerLayout(
                    state = state,
                    queueSheetState = queueSheetState,
                    navController = navController,
                    sheetNestedScroll = sheetNestedScroll,
                    mediaMetadata = mediaMetadata,
                    controlsContent = controlsContent,
                    spotifyCanvasUrl = spotifyCanvasUrl,
                    playerBackground = playerBackground,
                    sliderPosition = sliderPosition,
                    effectivePosition = { effectivePosition },
                    isListenTogetherGuest = isListenTogetherGuest,
                    isFullScreen = isFullScreen,
                    useNewPlayerDesign = useNewPlayerDesign,
                    showInlineLyrics = showInlineLyrics,
                    onShowInlineLyricsChange = { showInlineLyrics = it },
                    queueBackground = queueBackground,
                    onBackgroundColor = onBackgroundColor,
                    TextBackgroundColor = TextBackgroundColor,
                    textButtonColor = textButtonColor,
                    iconButtonColor = iconButtonColor,
                    pureBlack = pureBlack,
                    bluetoothDeviceName = bluetoothDeviceName,
                    nameToShow = nameToShow,
                    onShowAudioDevices = { showAudioDeviceBottomSheet = true },
                )
            }

            adaptiveLayout.playerSideBySide -> {
                // Calculate vertical padding like OuterTune
                val density = LocalDensity.current
                val verticalPadding = max(
                    WindowInsets.systemBars.getTop(density),
                    WindowInsets.systemBars.getBottom(density)
                )
                val verticalPaddingDp = with(density) { verticalPadding.toDp() }
                val verticalWindowInsets = WindowInsets(left = 0.dp, top = verticalPaddingDp, right = 0.dp, bottom = verticalPaddingDp)

                val currentSliderPosition by rememberUpdatedState(sliderPosition)
                val sliderPositionProvider = remember { { currentSliderPosition } }
                val isExpandedProvider = remember(state) { { state.isExpanded } }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .windowInsetsPadding(
                            WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).add(verticalWindowInsets)
                        )
                        .padding(bottom = 16.dp, top = 8.dp)
                        .fillMaxSize()
                ) {
                    // Left Column: Artwork Box + Action Buttons below it
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .padding(bottom = 8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .then(sheetNestedScroll)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(thumbnailCornerRadius.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable(enabled = showInlineLyrics || showInlineQueue) {
                                        showInlineLyrics = false
                                        showInlineQueue = false
                                    }
                            ) {
                                Crossfade(
                                    targetState = mediaMetadata?.id to (mediaMetadata?.thumbnailUrl ?: currentSong?.song?.thumbnailUrl),
                                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                                    label = "landscapeArtworkCrossfade"
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
                                        contentScale = if (cropAlbumArt) ContentScale.Crop else ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                val motionVideoUrl = canvasArtwork?.animated ?: canvasArtwork?.videoUrl
                                if (enableCanvas && motionVideoUrl != null) {
                                    MotionartworkPlayer(
                                        primaryUrl = motionVideoUrl,
                                        fallbackUrl = canvasArtwork?.videoUrl,
                                        isPlaying = isPlaying,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Action buttons below the image showing box
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            // Left: Lyrics
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            val next = !showInlineLyrics
                                            showInlineLyrics = next
                                            if (next) showInlineQueue = false
                                        }
                                    )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.lyrics_apple),
                                    contentDescription = stringResource(R.string.lyrics),
                                    tint = if (showInlineLyrics) textButtonColor else textButtonColor.copy(alpha = 0.5f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Center: Audio Device / Routing
                            val isConnected = bluetoothDeviceName != null
                            val tintColor = if (isConnected) textButtonColor else textButtonColor.copy(alpha = 0.5f)
                            val label = if (isConnected) {
                                nameToShow ?: "Bluetooth"
                            } else {
                                stringResource(R.string.player_audio_this_phone)
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { showAudioDeviceBottomSheet = true }
                                    )
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (isConnected) {
                                            when {
                                                isSpeaker(nameToShow) -> R.drawable.speaker_applemusic
                                                isBuds(nameToShow) -> R.drawable.headphones_apple
                                                else -> R.drawable.headphones_apple
                                            }
                                        } else {
                                            R.drawable.airplay_audio
                                        }
                                    ),
                                    contentDescription = label,
                                    tint = tintColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Right: Queue
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            val next = !showInlineQueue
                                            showInlineQueue = next
                                            if (next) showInlineLyrics = false
                                        }
                                    )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.apple_queue),
                                    contentDescription = stringResource(R.string.queue),
                                    tint = if (showInlineQueue) textButtonColor else textButtonColor.copy(alpha = 0.5f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    // Right Column: Dynamic Pane (Controls / Lyrics / Queue)
                    Crossfade(
                        targetState = showInlineLyrics to showInlineQueue,
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                        label = "landscapeRightPane",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) { (showLyrics, showQueue) ->
                        when {
                            showLyrics -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(sheetNestedScroll)
                                ) {
                                    InlineLyricsView(
                                        mediaMetadata = mediaMetadata,
                                        showLyrics = true,
                                        positionProvider = { sliderPosition ?: effectivePosition }
                                    )

                                    val currentLyrics by playerConnection.currentLyrics.collectAsState(initial = null)
                                    val metaForLyrics = mediaMetadata
                                    if (metaForLyrics != null) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(bottom = 12.dp, end = 12.dp)
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(TextBackgroundColor.copy(alpha = 0.14f))
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = {
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
                                                )
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_vert),
                                                contentDescription = stringResource(R.string.lyrics),
                                                tint = TextBackgroundColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            showQueue -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(sheetNestedScroll)
                                ) {
                                    InlineQueueView(
                                        playerConnection = playerConnection,
                                        navController = navController,
                                        playerBottomSheetState = state,
                                        primaryColor = TextBackgroundColor,
                                        onDraggingChange = {},
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                                ) {
                                    Spacer(Modifier.weight(1f))

                                    mediaMetadata?.let {
                                        controlsContent(it)
                                    }

                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                val bottomPadding by animateDpAsState(
                    targetValue = if (isFullScreen) 0.dp else if (useNewPlayerDesign) queueSheetState.collapsedBound else WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
                    label = "bottomPadding"
                )

                if (useNewPlayerDesign) {
                    M3ePlayerLayout(
                        state = state,
                        bottomPadding = bottomPadding,
                        mediaMetadata = mediaMetadata,
                        showInlineLyrics = showInlineLyrics,
                        sheetNestedScroll = sheetNestedScroll,
                        sliderPosition = { sliderPosition },
                        effectivePosition = { effectivePosition },
                        spotifyCanvasUrl = spotifyCanvasUrl,
                        playerBackground = playerBackground,
                        isListenTogetherGuest = isListenTogetherGuest,
                        controlsContent = controlsContent,
                    )
                } else {
                    ClassicPlayerLayout(
                        state = state,
                        sheetNestedScroll = sheetNestedScroll,
                        navController = navController,
                        bottomPadding = bottomPadding,
                        mediaMetadata = mediaMetadata,
                        currentSong = currentSong,
                        playerConnection = playerConnection,
                        playerBackground = playerBackground,
                        showInlineLyrics = showInlineLyrics,
                        showInlineQueue = showInlineQueue,
                        onShowInlineLyricsChange = { showInlineLyrics = it },
                        onShowInlineQueueChange = { showInlineQueue = it },
                        isFullScreen = isFullScreen,
                        isPlaying = isPlaying,
                        effectiveIsPlaying = effectiveIsPlaying,
                        isListenTogetherGuest = isListenTogetherGuest,
                        canSkipNext = canSkipNext,
                        canSkipPrevious = canSkipPrevious,
                        swipeThumbnailPref = swipeThumbnailPref,
                        thumbnailCornerRadius = thumbnailCornerRadius,
                        cropAlbumArt = cropAlbumArt,
                        enableCanvas = enableCanvas,
                        canvasArtwork = canvasArtwork,
                        trackInfoHeightDp = trackInfoHeightDp,
                        trackInfoSpacerHeight = trackInfoSpacerHeight,
                        positionProvider = { sliderPosition ?: effectivePosition },
                        controlsContent = controlsContent,
                        TextBackgroundColor = TextBackgroundColor,
                    )
                }
            }
        }

        if (!adaptiveLayout.playerQueueBeside) {
        AnimatedVisibility(
            visible = !isFullScreen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Queue(
                state = queueSheetState,
                playerBottomSheetState = state,
            navController = navController,
            background =
            if (useBlackBackground) {
                Color.Black
            } else {
                MaterialTheme.colorScheme.surface //fixed the issue causing the queue ui not good surfacecontainer
            },
            onBackgroundColor = onBackgroundColor,
            TextBackgroundColor = TextBackgroundColor,
            textButtonColor = textButtonColor,
            iconButtonColor = iconButtonColor,
            pureBlack = pureBlack,
            showInlineLyrics = showInlineLyrics,
            playerBackground = playerBackground,
            onToggleLyrics = {
                showInlineLyrics = !showInlineLyrics
            },
            )
        }
        }

        if (showAudioDeviceBottomSheet) {
            AudioDeviceBottomSheet(onDismiss = { showAudioDeviceBottomSheet = false })
        }
    }
}
