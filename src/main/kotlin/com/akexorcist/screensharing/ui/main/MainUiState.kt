package com.akexorcist.screensharing.ui.main

import java.awt.Dimension

fun defaultMainUiState(
    selectedVideo: Video?,
    selectedInputAudio: Audio?,
    selectedOutputAudio: Audio?,
) = MainUiState(
    selectedVideo = selectedVideo,
    selectedAudioInput = selectedInputAudio,
    selectedAudioOutput = selectedOutputAudio,
    selectedAudioInputError = false,
    selectedAudioOutputError = false,
    availableVideos = listOf(),
    availableAudioInputs = listOf(),
    availableAudioOutputs = listOf(),
)

data class MainUiState(
    val selectedVideo: Video?,
    val selectedAudioInput: Audio?,
    val selectedAudioOutput: Audio?,
    val selectedAudioInputError: Boolean,
    val selectedAudioOutputError: Boolean,
    val availableVideos: List<Video>,
    val availableAudioInputs: List<Audio>,
    val availableAudioOutputs: List<Audio>,
)

data class Video(
    val name: String,
    val selectedResolution: Resolution,
    val availableResolutions: List<Resolution>,
    val fps: Double,
) {
    data class Resolution(
        val width: Int,
        val height: Int,
    )
}

fun Dimension.toResolution() = Video.Resolution(width = this.width, height = this.height)

fun Video.Resolution.toDimension() = Dimension(this.width, this.height)

data class Audio(
    val name: String,
)
