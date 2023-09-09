package com.akexorcist.backdrop.data

sealed class AudioPlaybackStatus {
    object Playing : AudioPlaybackStatus()

    object AudioInputError : AudioPlaybackStatus()

    object AudioOutputError : AudioPlaybackStatus()
}
