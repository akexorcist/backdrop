package com.akexorcist.screensharing.data

import com.github.sarxos.webcam.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.awt.Dimension

interface WebcamRepository {
    fun getAvailableWebcam(): Flow<List<Webcam>>

    fun open(name: String, dimension: Dimension? = null)

    fun close()

    fun collectCurrentWebcam(): Flow<Webcam?>

    fun collectAvailableImageData(): StateFlow<ImageData?>
}

class DefaultWebcamRepository : WebcamRepository {
    private var currentWebcam: MutableStateFlow<Webcam?> = MutableStateFlow(null)
    private val webcamEventFlow: MutableStateFlow<VideoState> = MutableStateFlow(VideoState.Closed)
    private val availableImageDataFlow: MutableStateFlow<ImageData?> = MutableStateFlow(null)

    override fun getAvailableWebcam(): Flow<List<Webcam>> = callbackFlow {
        trySend(Webcam.getWebcams())
        val listener = object : WebcamDiscoveryListener {
            override fun webcamFound(event: WebcamDiscoveryEvent?) {
                trySend(Webcam.getWebcams())
            }

            override fun webcamGone(event: WebcamDiscoveryEvent?) {
                trySend(Webcam.getWebcams())
            }
        }
        Webcam.addDiscoveryListener(listener)
        awaitClose { Webcam.removeDiscoveryListener(listener) }
    }

    override fun open(name: String, dimension: Dimension?) {
        currentWebcam.value?.takeIf { it.isOpen }
            ?.let {
                it.close()
                it.removeWebcamListener(webcamListener)
            }
        Webcam.getWebcams().find { it.name == name }?.let { webcam ->
            (dimension ?: webcam.device.resolutions.getOrNull(0))?.let {
                webcam.device.resolution = it
            }
            webcam.open(true)
            webcam.addWebcamListener(webcamListener)
            currentWebcam.update { webcam }
        }
    }

    override fun close() {
        currentWebcam.value?.takeIf { it.isOpen }
            ?.let {
                it.close()
                it.removeWebcamListener(webcamListener)
            }
        currentWebcam.update { null }
    }

    override fun collectCurrentWebcam(): Flow<Webcam?> = currentWebcam

    override fun collectAvailableImageData(): StateFlow<ImageData?> = availableImageDataFlow

    private val webcamListener = object : WebcamListener {
        override fun webcamOpen(event: WebcamEvent) {
            webcamEventFlow.update {
                VideoState.Open
            }
        }

        override fun webcamClosed(event: WebcamEvent) {
            webcamEventFlow.update {
                VideoState.Closed
            }
        }

        override fun webcamDisposed(event: WebcamEvent) {
            webcamEventFlow.update {
                VideoState.Disposed
            }
        }

        override fun webcamImageObtained(event: WebcamEvent) {
            webcamEventFlow.update {
                VideoState.ImageObtained
            }
            availableImageDataFlow.update {
                ImageData(
                    image = event.image,
                    timestamp = System.currentTimeMillis(),
                    frameRate = currentWebcam.value?.fps ?: 0.0
                )
            }
        }
    }
}
