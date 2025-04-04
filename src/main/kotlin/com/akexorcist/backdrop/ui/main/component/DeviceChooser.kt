package com.akexorcist.backdrop.ui.main.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akexorcist.backdrop.extension.surfaceBackground
import com.akexorcist.backdrop.resource.StringResource
import com.akexorcist.backdrop.ui.main.*

@Composable
fun VideoChooser(
    modifier: Modifier = Modifier,
    selectedVideo: Video?,
    availableVideos: List<Video>?,
    onVideoSelect: (Video) -> Unit,
) {
    DeviceChooser(
        modifier = modifier,
        label = StringResource.labelVideo,
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
fun AudioInputChooser(
    modifier: Modifier = Modifier,
    selectedAudioInput: Audio?,
    selectedAudioInputError: Boolean,
    availableAudioInputs: List<Audio>?,
    onAudioInputSelect: (Audio) -> Unit,
) {
    DeviceChooser(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        label = StringResource.labelAudioInput,
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
fun AudioOutputChooser(
    modifier: Modifier = Modifier,
    selectedAudioOutput: Audio?,
    selectedAudioOutputError: Boolean,
    availableAudioOutputs: List<Audio>?,
    onAudioOutputSelect: (Audio) -> Unit,
) {
    DeviceChooser(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        label = StringResource.labelAudioOutput,
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
                color = MaterialTheme.colors.primary,
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
                                    name == selectedDevice -> MaterialTheme.colors.primary
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
