@file:Suppress("FunctionName")

package com.akexorcist.backdrop.ui.main

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akexorcist.backdrop.config.DeviceName
import com.akexorcist.backdrop.data.*
import com.akexorcist.backdrop.extension.surfaceBackground
import com.akexorcist.backdrop.resource.StringResource
import com.akexorcist.backdrop.ui.BackdropAppState
import com.akexorcist.backdrop.ui.main.component.AudioInputChooser
import com.akexorcist.backdrop.ui.main.component.AudioOutputChooser
import com.akexorcist.backdrop.ui.main.component.MenuButtonContainer
import com.akexorcist.backdrop.ui.main.component.VideoChooser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SectionWidth = 300.dp

@Composable
fun MainRoute(
    appState: BackdropAppState,
    mainViewModel: MainViewModel,
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val availableImageData by mainViewModel.availableImageData.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var isDeviceConsoleShowing by remember { mutableStateOf(true) }
    var isMenuHiding by remember { mutableStateOf(false) }
    var showFrameRate by remember { mutableStateOf(false) }
    var isHidingMenuHovered by remember { mutableStateOf(false) }

    MainScreen(
        uiState = uiState,
        availableImageData = availableImageData,
        isDeviceConsoleShowing = isDeviceConsoleShowing,
        isMenuHiding = isMenuHiding,
        showFrameRate = showFrameRate,
        isFullScreen = appState.isFullscreen(),
        onVideoSelect = { mainViewModel.selectVideo(it) },
        onVideoResolutionSelect = { video, resolution -> mainViewModel.setVideoResolution(video, resolution) },
        onAudioInputSelect = { mainViewModel.selectAudioInput(it) },
        onAudioOutputSelect = { mainViewModel.selectAudioOutput(it) },
        onHidingMenuHovered = { isHidingMenuHovered = it },
        onToggleConsoleUiClick = { isDeviceConsoleShowing = !isDeviceConsoleShowing },
        onEnterFullscreen = { coroutineScope.launch { appState.enterFullScreen() } },
        onExitFullscreenClick = { appState.exitFullScreen() },
        onShowFrameRateClick = { showFrameRate = true },
        onHideFrameRateClick = { showFrameRate = false },
        onCloseAppClick = { appState.exitApplication() },
    )

    AutoMenuHidingLaunchedEffect(isDeviceConsoleShowing, isHidingMenuHovered) {
        isMenuHiding = it
    }
    LaunchedEffect(Unit) {
        delay(50)
        mainViewModel.observeVideoInput()
    }
    LaunchedEffect(Unit) {
        delay(50)
        mainViewModel.observeAudioInput()
    }
    LaunchedEffect(Unit) {
        delay(50)
        mainViewModel.observeAudioOutput()
    }
}

@Composable
private fun AutoMenuHidingLaunchedEffect(
    isDeviceConsoleShowing: Boolean,
    isHidingMenuHovered: Boolean,
    onMenuHidingChange: (Boolean) -> Unit
) {
    LaunchedEffect(isDeviceConsoleShowing, isHidingMenuHovered) {
        if (isDeviceConsoleShowing) return@LaunchedEffect
        if (isHidingMenuHovered) {
            onMenuHidingChange(false)
        } else {
            delay(3000L)
            onMenuHidingChange(true)
        }
    }
}

@Composable
private fun MainScreen(
    uiState: MainUiState,
    availableImageData: ImageData?,
    isDeviceConsoleShowing: Boolean,
    isMenuHiding: Boolean,
    showFrameRate: Boolean,
    isFullScreen: Boolean,
    onVideoSelect: (Video) -> Unit,
    onVideoResolutionSelect: (Video, Video.Resolution) -> Unit,
    onAudioInputSelect: (Audio) -> Unit,
    onAudioOutputSelect: (Audio) -> Unit,
    onHidingMenuHovered: (Boolean) -> Unit,
    onToggleConsoleUiClick: () -> Unit,
    onEnterFullscreen: () -> Unit,
    onExitFullscreenClick: () -> Unit,
    onShowFrameRateClick: () -> Unit,
    onHideFrameRateClick: () -> Unit,
    onCloseAppClick: () -> Unit,
) {
    val selectedVideo = uiState.selectedVideo
    val selectedAudioInput = uiState.selectedAudioInput
    val selectedAudioOutput = uiState.selectedAudioOutput
    val selectedAudioInputError = uiState.selectedAudioInputError
    val selectedAudioOutputError = uiState.selectedAudioOutputError
    val availableVideo = uiState.availableVideos
    val availableAudioInputs = uiState.availableAudioInputs
    val availableAudioOutputs = uiState.availableAudioOutputs
    val isToggleConsoleUiClickable = availableVideo != null &&
            availableAudioInputs != null &&
            availableAudioOutputs != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
    ) {
        VideoSurface(
            selectedVideo = selectedVideo,
            availableImageData = availableImageData,
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(32.dp)
        ) {
            MenuButtonContainer(
                isToggleConsoleUiClickable = isToggleConsoleUiClickable,
                isDeviceConsoleShowing = isDeviceConsoleShowing,
                isMenuHiding = isMenuHiding,
                showFrameRate = showFrameRate,
                isFullScreen = isFullScreen,
                onHidingMenuHovered = onHidingMenuHovered,
                onToggleConsoleUiClick = onToggleConsoleUiClick,
                onEnterFullscreenClick = onEnterFullscreen,
                onExitFullscreenClick = onExitFullscreenClick,
                onShowFrameRateClick = onShowFrameRateClick,
                onHideFrameRateClick = onHideFrameRateClick,
                onCloseAppClick = onCloseAppClick,
            )
            Spacer(Modifier.size(16.dp))
            AnimatedVisibility(
                visible = isDeviceConsoleShowing,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -25 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -25 }),
            ) {
                Row {
                    // Webcam
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        VideoChooser(
                            modifier = Modifier.width(SectionWidth),
                            selectedVideo = selectedVideo,
                            availableVideos = availableVideo,
                            onVideoSelect = onVideoSelect,
                        )
                        Spacer(Modifier.size(16.dp))
                        VideoStatusInformation(
                            modifier = Modifier.width(SectionWidth),
                            currentVideo = selectedVideo,
                            availableImageData = availableImageData,
                        )
                        Spacer(Modifier.size(16.dp))
                        VideoResolutionInformation(
                            modifier = Modifier.width(SectionWidth),
                            video = selectedVideo,
                            availableImageData = availableImageData,
                            onResolutionSelect = onVideoResolutionSelect,
                        )
                    }

                    // Audio
                    Spacer(Modifier.size(16.dp))
                    AudioInputChooser(
                        modifier = Modifier.width(SectionWidth),
                        selectedAudioInput = selectedAudioInput,
                        selectedAudioInputError = selectedAudioInputError,
                        availableAudioInputs = availableAudioInputs,
                        onAudioInputSelect = onAudioInputSelect,
                    )
                    Spacer(Modifier.size(16.dp))
                    AudioOutputChooser(
                        modifier = Modifier.width(SectionWidth),
                        selectedAudioOutput = selectedAudioOutput,
                        selectedAudioOutputError = selectedAudioOutputError,
                        availableAudioOutputs = availableAudioOutputs,
                        onAudioOutputSelect = onAudioOutputSelect,
                    )
                }
            }
        }

        if (showFrameRate) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                FrameRateInformation(
                    frameRate = uiState.selectedVideo
                        ?.takeIf { it.name != DeviceName.VIDEO_NONE }
                        ?.let { availableImageData?.rawFrameRate }
                )
            }
        }
    }
}

private const val FRAME_RATE_UPDATE_INTERVAL_MILLIS = 200L

@Composable
private fun FrameRateInformation(frameRate: Double?) {
    var lastUpdatedTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var displayFrameRate by remember { mutableStateOf(frameRate) }
    LaunchedEffect(frameRate) {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastUpdatedTimeMillis > FRAME_RATE_UPDATE_INTERVAL_MILLIS) {
            displayFrameRate = frameRate
            lastUpdatedTimeMillis = currentTimeMillis
        }
    }
    Box(
        modifier = Modifier
            .width(50.dp)
            .height(40.dp)
            .background(
                color = MaterialTheme.colors.background.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${displayFrameRate?.toInt() ?: 0}",
            color = MaterialTheme.colors.onBackground,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.subtitle1,
        )
    }
}

@Composable
private fun VideoStatusInformation(
    modifier: Modifier,
    currentVideo: Video?,
    availableImageData: ImageData?,
) {
    if (currentVideo?.name == DeviceName.VIDEO_NONE) return
    availableImageData ?: return
    Column(
        modifier = modifier
            .surfaceBackground()
            .padding(16.dp)
    ) {
        val width = availableImageData.image.width
        val height = availableImageData.image.height
        val fps = availableImageData.deviceFrameRate.toInt()
        Text(
            text = StringResource.labelCurrentResolution,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.subtitle1,
        )
        Spacer(Modifier.size(24.dp))
        Text(
            text = "$width x $height (${fps} FPS)",
            color = MaterialTheme.colors.onSurface,
        )
    }
}

@Composable
private fun VideoSurface(
    selectedVideo: Video?,
    availableImageData: ImageData?,
) {
    if (selectedVideo?.name == DeviceName.VIDEO_NONE) return
    if (availableImageData == null) return
    val imageRatio = availableImageData.image.width.toFloat() / availableImageData.image.height
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(
                    ratio = imageRatio,
                    matchHeightConstraintsFirst = true,
                ),
            painter = BitmapPainter(availableImageData.image.toComposeImageBitmap()),
            contentScale = ContentScale.Fit,
            contentDescription = null,
        )
    }
}

@Composable
private fun VideoResolutionInformation(
    modifier: Modifier,
    video: Video?,
    availableImageData: ImageData?,
    onResolutionSelect: (Video, Video.Resolution) -> Unit,
) {
    video ?: return
    availableImageData ?: return
    if (video.availableResolutions.isEmpty()) return
    val selectedVideoResolution = Video.Resolution(
        width = availableImageData.image.width,
        height = availableImageData.image.height,
    )
    Column(
        modifier = modifier
            .surfaceBackground()
            .padding(16.dp)
    ) {
        Text(
            text = StringResource.labelResolution,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.subtitle1,
        )
        Spacer(Modifier.size(16.dp))
        video.availableResolutions.forEachIndexed { index, resolution ->
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .selectable(
                        selected = resolution == selectedVideoResolution,
                        onClick = { onResolutionSelect(video, resolution) },
                        role = Role.RadioButton,
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = resolution == selectedVideoResolution,
                    onClick = null,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "${resolution.width} x ${resolution.height}",
                    color = MaterialTheme.colors.onSurface,
                )
            }
            if (index != video.availableResolutions.lastIndex) {
                Spacer(Modifier.size(8.dp))
            }
        }
    }
}
