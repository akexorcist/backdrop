package com.akexorcist.backdrop.ui.main

import com.akexorcist.backdrop.config.DeviceName
import com.akexorcist.backdrop.data.AudioRepository
import com.akexorcist.backdrop.data.ImageData
import com.akexorcist.backdrop.data.AudioPlaybackStatus
import com.akexorcist.backdrop.data.VideoRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MainViewModel(
    private val videoRepository: VideoRepository,
    private val audioRepository: AudioRepository,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())
    private val noSelectedVideo = Video(
        name = DeviceName.VIDEO_NONE,
        availableResolutions = listOf(),
        fps = .0,
    )

    private val noSelectedAudio = Audio(name = DeviceName.VIDEO_NONE)
    private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(
        defaultMainUiState(
            selectedVideo = noSelectedVideo,
            selectedInputAudio = noSelectedAudio,
            selectedOutputAudio = noSelectedAudio,
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState

    val availableImageData: StateFlow<ImageData?> = videoRepository.collectAvailableImageData()

    fun observeVideoInput() = coroutineScope.launch {
        videoRepository.getAvailableWebcam().collectLatest { videos ->
            val newVideos = listOf(noSelectedVideo) + videos.map { webcam ->
                Video(
                    name = webcam.name,
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

    fun observeAudioInput() = coroutineScope.launch {
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

    fun observeAudioOutput() = coroutineScope.launch {
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
            videoRepository.close()
            _uiState.update { it.copy(selectedVideo = noSelectedVideo) }
            return
        }
        videoRepository.open(video.name)
        _uiState.update {
            it.copy(selectedVideo = video)
        }
    }

    fun setVideoResolution(video: Video, resolution: Video.Resolution) {
        videoRepository.open(
            name = video.name,
            dimension = resolution.toDimension(),
        )
    }

    fun selectAudioInput(audio: Audio) = coroutineScope.launch {
        if (audio.name == _uiState.value.selectedAudioInput?.name) return@launch
        if (audio.name == DeviceName.AUDIO_NONE) {
            audioRepository.stopAudioPlayback()
            audioRepository.setAudioInput(DeviceName.AUDIO_NONE)
            _uiState.update {
                it.copy(
                    selectedAudioInput = noSelectedAudio,
                    selectedAudioOutputError = false,
                    selectedAudioInputError = false,
                )
            }
            return@launch
        }
        val status = audioRepository.run {
            stopAudioPlayback()
            delay(300L)
            setAudioInput(audio.name)
            startAudioPlayback()
        }
        _uiState.update {
            it.copy(
                selectedAudioInput = audio,
                selectedAudioInputError = status == AudioPlaybackStatus.AudioInputError,
                selectedAudioOutputError = status == AudioPlaybackStatus.AudioOutputError && it.selectedAudioOutput?.name != DeviceName.AUDIO_NONE,
            )
        }
    }

    fun selectAudioOutput(audio: Audio) = coroutineScope.launch {
        if (audio.name == _uiState.value.selectedAudioOutput?.name) return@launch
        if (audio.name == DeviceName.AUDIO_NONE) {
            audioRepository.stopAudioPlayback()
            audioRepository.setAudioOutput(DeviceName.AUDIO_NONE)
            _uiState.update {
                it.copy(
                    selectedAudioOutput = noSelectedAudio,
                    selectedAudioOutputError = false,
                    selectedAudioInputError = false,
                )
            }
            return@launch
        }
        val status = audioRepository.run {
            stopAudioPlayback()
            delay(300L)
            setAudioOutput(audio.name)
            startAudioPlayback()
        }
        _uiState.update {
            it.copy(
                selectedAudioOutput = audio,
                selectedAudioOutputError = status == AudioPlaybackStatus.AudioOutputError,
                selectedAudioInputError = status == AudioPlaybackStatus.AudioInputError && it.selectedAudioInput?.name != DeviceName.AUDIO_NONE,
            )
        }
    }
}
