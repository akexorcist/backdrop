package com.akexorcist.backdrop.data

sealed class PlaybackStatus {
    object Playing : PlaybackStatus()

    object AudioInputError : PlaybackStatus()

    object AudioOutputError : PlaybackStatus()
}
