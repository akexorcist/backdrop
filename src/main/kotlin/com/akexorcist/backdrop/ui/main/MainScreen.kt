@file:Suppress("FunctionName")

package com.akexorcist.backdrop.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akexorcist.backdrop.config.DeviceName
import com.akexorcist.backdrop.data.*
import kotlinx.coroutines.delay

private val SectionWidth = 300.dp

@Composable
fun MainRoute(
    mainViewModel: MainViewModel,
    isFullScreen: Boolean,
    onMaximizeWindowClick: () -> Unit,
    onMinimizeWindowClick: () -> Unit,
    onCloseAppClick: () -> Unit,
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val availableImageData by mainViewModel.availableImageData.collectAsState()
    MainScreen(
        uiState = uiState,
        availableImageData = availableImageData,
        isFullScreen = isFullScreen,
        onVideoSelect = { mainViewModel.selectVideo(it) },
        onVideoResolutionSelect = { video, resolution ->
            mainViewModel.setVideoResolution(video, resolution)
        },
        onAudioInputSelect = { mainViewModel.selectAudioInput(it) },
        onAudioOutputSelect = { mainViewModel.selectAudioOutput(it) },
        onMaximizeWindowClick = onMaximizeWindowClick,
        onMinimizeWindowClick = onMinimizeWindowClick,
        onCloseAppClick = onCloseAppClick,
    )
    LaunchedEffect(Unit) { mainViewModel.observeVideoInput() }
    LaunchedEffect(Unit) { mainViewModel.observeAudioInput() }
    LaunchedEffect(Unit) { mainViewModel.observeAudioOutput() }
}

@Composable
private fun MainScreen(
    uiState: MainUiState,
    availableImageData: ImageData?,
    isFullScreen: Boolean,
    onVideoSelect: (Video) -> Unit,
    onVideoResolutionSelect: (Video, Video.Resolution) -> Unit,
    onAudioInputSelect: (Audio) -> Unit,
    onAudioOutputSelect: (Audio) -> Unit,
    onMaximizeWindowClick: () -> Unit,
    onMinimizeWindowClick: () -> Unit,
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
    var isConsoleShowing by remember { mutableStateOf(true) }
    var isHiding by remember { mutableStateOf(false) }
    var isInvisibleHovered by remember { mutableStateOf(false) }

    LaunchedEffect(isConsoleShowing, isInvisibleHovered) {
        if (isConsoleShowing) return@LaunchedEffect
        isHiding = if (isInvisibleHovered) {
            false
        } else {
            delay(3000L)
            true
        }
    }

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
            Column {
                CloseButton(
                    isHiding = isHiding,
                    onInvisibleHovered = { isInvisibleHovered = it },
                    onClick = onCloseAppClick,
                )
                Spacer(Modifier.size(16.dp))
                FullscreenButton(
                    isFullScreen = isFullScreen,
                    isHiding = isHiding,
                    onInvisibleHovered = { isInvisibleHovered = it },
                    onClick = when (isFullScreen) {
                        true -> onMinimizeWindowClick
                        false -> onMaximizeWindowClick
                    },
                )
                Spacer(Modifier.size(16.dp))
                ToggleUiButton(
                    clickable = availableVideo != null &&
                            availableAudioInputs != null &&
                            availableAudioOutputs != null,
                    isHiding = isHiding,
                    isConsoleShowing = isConsoleShowing,
                    onInvisibleHovered = { isInvisibleHovered = it },
                    onClick = { isConsoleShowing = !isConsoleShowing },
                )
            }
            Spacer(Modifier.size(16.dp))
            AnimatedVisibility(
                visible = isConsoleShowing,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -25 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -25 }),
            ) {
                Row {
                    // Webcam
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        VideoChooser(
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
                        selectedAudioInput = selectedAudioInput,
                        selectedAudioInputError = selectedAudioInputError,
                        availableAudioInputs = availableAudioInputs,
                        onAudioInputSelect = onAudioInputSelect,
                    )
                    Spacer(Modifier.size(16.dp))
                    AudioOutputChooser(
                        selectedAudioOutput = selectedAudioOutput,
                        selectedAudioOutputError = selectedAudioOutputError,
                        availableAudioOutputs = availableAudioOutputs,
                        onAudioOutputSelect = onAudioOutputSelect,
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleUiButton(
    clickable: Boolean,
    isHiding: Boolean,
    isConsoleShowing: Boolean,
    onInvisibleHovered: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    LaunchedEffect(isHovered) {
        onInvisibleHovered(isHovered)
    }

    val animatedIconRotate by animateFloatAsState(
        targetValue = if (isConsoleShowing) 0f else 180f,
        animationSpec = tween(durationMillis = 300)
    )
    val animatedButtonAlpha by animateFloatAsState(
        targetValue = if (isHiding) 0f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearEasing,
        )
    )
    IconButton(
        modifier = Modifier.alpha(animatedButtonAlpha),
        interactionSource = interactionSource,
        enabled = clickable,
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.rotate(animatedIconRotate),
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Toggle UI display",
        )
    }
}

@Composable
private fun CloseButton(
    isHiding: Boolean,
    onInvisibleHovered: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        onInvisibleHovered(isHovered)
    }
    val animatedButtonAlpha by animateFloatAsState(
        targetValue = if (isHiding) 0f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearEasing,
        )
    )

    IconButton(
        modifier = Modifier.alpha(animatedButtonAlpha),
        interactionSource = interactionSource,
        enabled = true,
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close the app",
        )
    }
}

@Composable
private fun FullscreenButton(
    isFullScreen: Boolean,
    isHiding: Boolean,
    onInvisibleHovered: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        onInvisibleHovered(isHovered)
    }
    val animatedButtonAlpha by animateFloatAsState(
        targetValue = if (isHiding) 0f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearEasing,
        )
    )

    IconButton(
        modifier = Modifier.alpha(animatedButtonAlpha),
        interactionSource = interactionSource,
        enabled = true,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(
                when (isFullScreen) {
                    true -> "ic_exit_fullscreen.svg"
                    false -> "ic_enter_fullscreen.svg"
                }
            ),
            contentDescription = when (isFullScreen) {
                true -> "Exit from fullscreen"
                false -> "Enter to fullscreen"
            },
        )
    }
}

@Composable
private fun IconButton(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Button(
        modifier = Modifier.size(48.dp).then(modifier),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colors.onSurface,
            disabledBackgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.125f),
            disabledContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.125f),
        ),
        elevation = ButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
            hoveredElevation = 0.dp,
            focusedElevation = 0.dp,
        ),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        interactionSource = interactionSource,
        onClick = onClick,
    ) {
        content()
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
        val fps = availableImageData.frameRate.toInt()
        Text(
            text = "Current Resolution",
            color = MaterialTheme.colors.secondary,
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
            text = "Resolution",
            color = MaterialTheme.colors.secondary,
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

@Composable
private fun VideoChooser(
    selectedVideo: Video?,
    availableVideos: List<Video>?,
    onVideoSelect: (Video) -> Unit,
) {
    DeviceChooser(
        modifier = Modifier.width(SectionWidth),
        label = "Video",
        selectedDevice = selectedVideo?.name,
        selectedDeviceError = false,
        availableDeviceNames = availableVideos?.map { it.name },
        onDeviceSelect = { selectedDevice ->
            availableVideos
                ?.find { it.name == selectedDevice }
                ?.let { onVideoSelect(it) }
        }
    )
}

@Composable
private fun AudioInputChooser(
    selectedAudioInput: Audio?,
    selectedAudioInputError: Boolean,
    availableAudioInputs: List<Audio>?,
    onAudioInputSelect: (Audio) -> Unit,
) {
    DeviceChooser(
        modifier = Modifier
            .width(SectionWidth)
            .verticalScroll(rememberScrollState()),
        label = "Audio Input",
        selectedDevice = selectedAudioInput?.name,
        selectedDeviceError = selectedAudioInputError,
        availableDeviceNames = availableAudioInputs?.map { it.name },
        onDeviceSelect = { selectedAudio ->
            availableAudioInputs
                ?.find { it.name == selectedAudio }
                ?.let { onAudioInputSelect(it) }
        }
    )
}

@Composable
private fun AudioOutputChooser(
    selectedAudioOutput: Audio?,
    selectedAudioOutputError: Boolean,
    availableAudioOutputs: List<Audio>?,
    onAudioOutputSelect: (Audio) -> Unit,
) {
    DeviceChooser(
        modifier = Modifier
            .width(SectionWidth)
            .verticalScroll(rememberScrollState()),
        label = "Audio Output",
        selectedDevice = selectedAudioOutput?.name,
        selectedDeviceError = selectedAudioOutputError,
        availableDeviceNames = availableAudioOutputs?.map { it.name },
        onDeviceSelect = { selectedAudio ->
            availableAudioOutputs
                ?.find { it.name == selectedAudio }
                ?.let { onAudioOutputSelect(it) }
        }
    )
}

@Composable
private fun DeviceChooser(
    modifier: Modifier,
    label: String,
    selectedDevice: String?,
    selectedDeviceError: Boolean,
    availableDeviceNames: List<String>?,
    onDeviceSelect: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .surfaceBackground()
            .padding(8.dp)
    ) {
        Spacer(Modifier.size(8.dp))
        Row {
            Spacer(Modifier.size(8.dp))
            Text(
                text = label,
                color = MaterialTheme.colors.secondary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.subtitle1,
            )
            Spacer(Modifier.size(16.dp))
        }
        Spacer(Modifier.size(16.dp))
        AnimatedVisibility(visible = availableDeviceNames != null) {
            if (availableDeviceNames != null) {
                Column {
                    availableDeviceNames.forEachIndexed { index, name ->
                        Row(
                            modifier = Modifier
                                .wrapContentWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .selectable(
                                    selected = name == selectedDevice,
                                    onClick = { onDeviceSelect(name) },
                                    role = Role.RadioButton,
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = name == selectedDevice,
                                onClick = null,
                                colors = when {
                                    name == selectedDevice && selectedDeviceError -> RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colors.error,
                                        unselectedColor = MaterialTheme.colors.onError.copy(alpha = 0.6f),
                                        disabledColor = MaterialTheme.colors.onError.copy(alpha = ContentAlpha.disabled),
                                    )

                                    else -> RadioButtonDefaults.colors()
                                }
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = name,
                                color = when {
                                    name == selectedDevice && selectedDeviceError -> MaterialTheme.colors.error
                                    name == selectedDevice -> MaterialTheme.colors.secondary
                                    else -> MaterialTheme.colors.onSurface
                                }
                            )
                        }
                        if (index != availableDeviceNames.lastIndex) {
                            Spacer(Modifier.size(8.dp))
                        }
                    }
                }
            }
        }
        AnimatedVisibility(visible = availableDeviceNames == null) {
            DeviceListLoading()
        }
    }
}

@Composable
private fun DeviceListLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        LinearProgressIndicator(color = MaterialTheme.colors.onSurface)
    }
}

@Composable
private fun Modifier.surfaceBackground() = this.background(
    color = MaterialTheme.colors.surface.copy(alpha = 0.7f),
    shape = RoundedCornerShape(16.dp),
).padding(horizontal = 8.dp, vertical = 16.dp)
