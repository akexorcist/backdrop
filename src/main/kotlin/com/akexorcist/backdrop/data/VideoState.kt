package com.akexorcist.backdrop.data

import java.awt.image.BufferedImage

sealed class VideoState {
    data object Open : VideoState()

    data object Closed : VideoState()

    data object Disposed : VideoState()

    data object ImageObtained : VideoState()
}

data class ImageData(
    val image: BufferedImage,
    val timestamp: Long,
    val deviceFrameRate: Double,
    val rawFrameRate: Double,
)
