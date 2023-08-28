@file:Suppress("FunctionName")

package com.akexorcist.screensharing.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.akexorcist.screensharing.config.DeviceName
import com.akexorcist.screensharing.data.*

@Composable
fun MainRoute(mainViewModel: MainViewModel) {
    val uiState by mainViewModel.uiState.collectAsState()
    val availableImageData by mainViewModel.availableImageData.collectAsState()
    MainScreen(
        uiState = uiState,
        availableImageData = availableImageData,
        onVideoSelect = { mainViewModel.selectVideo(it) },
        onVideoResolutionSelect = { video, resolution ->
            mainViewModel.setVideoResolution(video, resolution)
        },
        onAudioInputSelect = { mainViewModel.selectAudioInput(it) },
        onAudioOutputSelect = { mainViewModel.selectAudioOutput(it) },
    )
    LaunchedEffect(Unit) { mainViewModel.observeVideoInput() }
    LaunchedEffect(Unit) { mainViewModel.observeAudioInput() }
    LaunchedEffect(Unit) { mainViewModel.observeAudioOutput() }
}

@Composable
private fun MainScreen(
    uiState: MainUiState,
    availableImageData: ImageData?,
    onVideoSelect: (Video) -> Unit,
    onVideoResolutionSelect: (Video, Video.Resolution) -> Unit,
    onAudioInputSelect: (Audio) -> Unit,
    onAudioOutputSelect: (Audio) -> Unit,
) {
    val selectedVideo = uiState.selectedVideo
    val selectedAudioInput = uiState.selectedAudioInput
    val selectedAudioOutput = uiState.selectedAudioOutput
    val selectedAudioInputError = uiState.selectedAudioInputError
    val selectedAudioOutputError = uiState.selectedAudioOutputError
    val availableVideo = uiState.availableVideos
    val availableAudioInputs = uiState.availableAudioInputs
    val availableAudioOutputs = uiState.availableAudioOutputs
    Box(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colors.background)
    ) {
        VideoSurface(
            selectedVideo = selectedVideo,
            availableImageData = availableImageData,
        )
        Row(modifier = Modifier.padding(32.dp)) {
            // Webcam
            VideoChooser(
                selectedVideo = selectedVideo,
                availableVideos = availableVideo,
                onVideoSelect = onVideoSelect,
            )
            Spacer(Modifier.size(16.dp))
            VideoResolutionInformation(
                video = selectedVideo,
                onResolutionSelect = onVideoResolutionSelect,
            )

            VideoStatusInformation(
                currentVideo = selectedVideo,
                availableImageData = availableImageData,
            )

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

@Composable
private fun VideoStatusInformation(
    currentVideo: Video?,
    availableImageData: ImageData?,
) {
    if (currentVideo?.name == DeviceName.VIDEO_NONE) return
    availableImageData ?: return
    Row {
        Spacer(Modifier.size(16.dp))
        Column {
            Column(
                modifier = Modifier
                    .surfaceBackground()
                    .padding(16.dp)
            ) {
                val width = availableImageData.image.width
                val height = availableImageData.image.height
                val fps = availableImageData.frameRate.toInt()
                Text(
                    text = "$width x $height (${fps} FPS)",
                    color = MaterialTheme.colors.onSurface,
                )
            }
        }
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
    video: Video?,
    onResolutionSelect: (Video, Video.Resolution) -> Unit,
) {
    video ?: return
    if (video.availableResolutions.isEmpty()) return
    Column(
        modifier = Modifier
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
                        selected = resolution == video.selectedResolution,
                        onClick = { onResolutionSelect(video, resolution) },
                        role = Role.RadioButton,
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = resolution == video.selectedResolution,
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
    availableVideos: List<Video>,
    onVideoSelect: (Video) -> Unit,
) {
    DeviceChooser(
        label = "Video",
        selectedDevice = selectedVideo?.name,
        selectedDeviceError = false,
        availableDeviceNames = availableVideos.map { it.name },
        onDeviceSelect = { selectedDevice ->
            availableVideos
                .find { it.name == selectedDevice }
                ?.let { onVideoSelect(it) }
        }
    )
}

@Composable
private fun AudioInputChooser(
    selectedAudioInput: Audio?,
    selectedAudioInputError: Boolean,
    availableAudioInputs: List<Audio>,
    onAudioInputSelect: (Audio) -> Unit,
) {
    DeviceChooser(
        label = "Audio Input",
        selectedDevice = selectedAudioInput?.name,
        selectedDeviceError = selectedAudioInputError,
        availableDeviceNames = availableAudioInputs.map { it.name },
        onDeviceSelect = { selectedAudio ->
            availableAudioInputs
                .find { it.name == selectedAudio }
                ?.let { onAudioInputSelect(it) }
        }
    )
}

@Composable
private fun AudioOutputChooser(
    selectedAudioOutput: Audio?,
    selectedAudioOutputError: Boolean,
    availableAudioOutputs: List<Audio>,
    onAudioOutputSelect: (Audio) -> Unit,
) {
    DeviceChooser(
        label = "Audio Output",
        selectedDevice = selectedAudioOutput?.name,
        selectedDeviceError = selectedAudioOutputError,
        availableDeviceNames = availableAudioOutputs.map { it.name },
        onDeviceSelect = { selectedAudio ->
            availableAudioOutputs
                .find { it.name == selectedAudio }
                ?.let { onAudioOutputSelect(it) }
        }
    )
}

@Composable
private fun DeviceChooser(
    label: String,
    selectedDevice: String?,
    selectedDeviceError: Boolean,
    availableDeviceNames: List<String>,
    onDeviceSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .surfaceBackground()
            .verticalScroll(rememberScrollState())
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

@Composable
private fun Modifier.surfaceBackground() = this.background(
    color = MaterialTheme.colors.surface.copy(alpha = 0.25f),
    shape = RoundedCornerShape(16.dp),
).padding(horizontal = 8.dp, vertical = 16.dp)
