package com.akexorcist.screensharing.data

import java.awt.image.BufferedImage

sealed class VideoState {
    object Open : VideoState()

    object Closed : VideoState()

    object Disposed : VideoState()

    object ImageObtained : VideoState()
}

data class ImageData(
    val image: BufferedImage,
    val timestamp: Long,
    val frameRate: Double,
)
