package com.akexorcist.screensharing.ui.main

import com.akexorcist.screensharing.config.DeviceName
import com.akexorcist.screensharing.data.AudioRepository
import com.akexorcist.screensharing.data.ImageData
import com.akexorcist.screensharing.data.WebcamRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MainViewModel(
    private val webcamRepository: WebcamRepository,
    private val audioRepository: AudioRepository,
) {
    private val noSelectedVideo = Video(
        name = DeviceName.VIDEO_NONE,
        selectedResolution = Video.Resolution(0, 0),
        availableResolutions = listOf(),
        fps = .0,
    )

    private val noSelectedAudio = Audio(name = DeviceName.VIDEO_NONE)

    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())
    private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(
        defaultMainUiState(
            selectedVideo = noSelectedVideo,
            selectedInputAudio = noSelectedAudio,
            selectedOutputAudio = noSelectedAudio,
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState

    val availableImageData: StateFlow<ImageData?> = webcamRepository.collectAvailableImageData()

    suspend fun observeVideoInput() {
        webcamRepository.getAvailableWebcam().collectLatest { videos ->
            val newVideos = listOf(noSelectedVideo) + videos.map { webcam ->
                Video(
                    name = webcam.name,
                    selectedResolution = webcam.device.resolution.toResolution(),
                    availableResolutions = webcam.device.resolutions.map { it.toResolution() },
                    fps = webcam.fps,
                )
            }
            if (_uiState.value.availableVideos != newVideos) {
                _uiState.update { state ->
                    state.copy(availableVideos = newVideos)
                }
            }
        }
    }

    suspend fun observeAudioInput() {
        audioRepository.getAvailableAudioInputs().collectLatest { audios ->
            val newAudios = listOf(noSelectedAudio) + audios.map { (_, info) ->
                Audio(info.name)
            }
            if (_uiState.value.availableAudioInputs != newAudios) {
                _uiState.update { uiState ->
                    uiState.copy(availableAudioInputs = newAudios)
                }
            }
        }
    }

    suspend fun observeAudioOutput() {
        audioRepository.getAvailableAudioOutputs().collectLatest { audios ->
            val newAudios = listOf(noSelectedAudio) + audios.map { (_, info) ->
                Audio(info.name)
            }
            if (_uiState.value.availableAudioOutputs != newAudios) {
                _uiState.update { uiState ->
                    uiState.copy(availableAudioOutputs = newAudios)
                }
            }
        }
    }

    fun selectVideo(video: Video) {
        if (video.name == _uiState.value.selectedVideo?.name) return
        if (video.name == DeviceName.VIDEO_NONE) {
            webcamRepository.close()
            _uiState.update { it.copy(selectedVideo = noSelectedVideo) }
            return
        }
        webcamRepository.open(video.name)
        _uiState.update {
            it.copy(selectedVideo = video)
        }
    }

    fun setVideoResolution(video: Video, resolution: Video.Resolution) {
        webcamRepository.open(
            name = video.name,
            dimension = resolution.toDimension(),
        )
        _uiState.update {
            it.copy(
                selectedVideo = it.selectedVideo?.copy(selectedResolution = resolution)
            )
        }
    }

    fun selectAudioInput(audio: Audio) = coroutineScope.launch {
        if (audio.name == _uiState.value.selectedAudioInput?.name) return@launch
        if (audio.name == DeviceName.AUDIO_NONE) {
            audioRepository.stopAudioPlayback()
            audioRepository.setAudioInput(DeviceName.AUDIO_NONE)
            _uiState.update { it.copy(selectedAudioInput = noSelectedAudio) }
            return@launch
        }
        audioRepository.apply {
            stopAudioPlayback()
            delay(300L)
            setAudioInput(audio.name)
            startAudioPlayback()
        }
        _uiState.update {
            it.copy(selectedAudioInput = audio)
        }
    }

    fun selectAudioOutput(audio: Audio) = coroutineScope.launch {
        if (audio.name == _uiState.value.selectedAudioOutput?.name) return@launch
        if (audio.name == DeviceName.AUDIO_NONE) {
            audioRepository.stopAudioPlayback()
            audioRepository.setAudioOutput(DeviceName.AUDIO_NONE)
            _uiState.update { it.copy(selectedAudioOutput = noSelectedAudio) }
            return@launch
        }
        audioRepository.apply {
            stopAudioPlayback()
            delay(300L)
            setAudioOutput(audio.name)
            startAudioPlayback()
        }
        _uiState.update {
            it.copy(selectedAudioOutput = audio)
        }
    }
}

