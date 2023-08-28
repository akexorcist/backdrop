@file:Suppress("FunctionName")

package com.akexorcist.screensharing.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
    val currentVideo = uiState.selectedVideo
    val currentAudioInput = uiState.selectedAudioInput
    val selectedAudioOutput = uiState.selectedAudioOutput
    val availableVideo = uiState.availableVideos
    val availableAudioInputs = uiState.availableAudioInputs
    val availableAudioOutputs = uiState.availableAudioOutputs
    Box(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colors.background)
    ) {
        VideoSurface(
            currentVideo = currentVideo,
            availableImageData = availableImageData,
        )
        Row(modifier = Modifier.padding(32.dp)) {
            // Webcam
            VideoChooser(
                currentVideo = currentVideo,
                availableVideos = availableVideo,
                onVideoSelect = onVideoSelect,
            )
            Spacer(Modifier.size(16.dp))
            VideoResolutionInformation(
                video = currentVideo,
                onResolutionSelect = onVideoResolutionSelect,
            )

            VideoStatusInformation(
                currentVideo = currentVideo,
                availableImageData = availableImageData,
            )

            // Audio
            Spacer(Modifier.size(16.dp))
            AudioInputChooser(
                currentAudioInput = currentAudioInput,
                availableAudioInputs = availableAudioInputs,
                onAudioInputSelect = onAudioInputSelect,
            )
            Spacer(Modifier.size(16.dp))
            AudioOutputChooser(
                currentAudioOutput = selectedAudioOutput,
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
    currentVideo: Video?,
    availableImageData: ImageData?,
) {
    if (currentVideo?.name == DeviceName.VIDEO_NONE) return
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
    currentVideo: Video?,
    availableVideos: List<Video>,
    onVideoSelect: (Video) -> Unit,
) {
    DeviceChooser(
        label = "Video",
        selectedDevice = currentVideo?.name,
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
    currentAudioInput: Audio?,
    availableAudioInputs: List<Audio>,
    onAudioInputSelect: (Audio) -> Unit,
) {
    DeviceChooser(
        label = "Audio Input",
        selectedDevice = currentAudioInput?.name,
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
    currentAudioOutput: Audio?,
    availableAudioOutputs: List<Audio>,
    onAudioOutputSelect: (Audio) -> Unit,
) {
    DeviceChooser(
        label = "Audio Output",
        selectedDevice = currentAudioOutput?.name,
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
    availableDeviceNames: List<String>,
    onDeviceSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier
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
        availableDeviceNames.forEachIndexed { index, name ->
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(4.dp))
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
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = name,
                    color = MaterialTheme.colors.onSurface,
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
