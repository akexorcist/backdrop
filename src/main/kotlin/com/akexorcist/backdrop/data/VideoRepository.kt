package com.akexorcist.backdrop.data

import com.github.sarxos.webcam.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.awt.Dimension
import kotlin.math.max

interface VideoRepository {
    fun getAvailableWebcam(): Flow<List<Webcam>>

    fun open(name: String, dimension: Dimension? = null)

    fun close()

    fun collectCurrentWebcam(): Flow<Webcam?>

    fun collectAvailableImageData(): StateFlow<ImageData?>
}

class DefaultVideoRepository(
    private val frameRateCounter: RawFrameRateCounter,
) : VideoRepository {
    private var currentVideo: MutableStateFlow<Webcam?> = MutableStateFlow(null)
    private val videoEventFlow: MutableStateFlow<VideoState> = MutableStateFlow(VideoState.Closed)
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
        currentVideo.value?.takeIf { it.isOpen }
            ?.let {
                it.close()
                it.removeWebcamListener(webcamListener)
            }
        Webcam.getWebcams().find { it.name == name }?.let { webcam ->
            (dimension ?: webcam.device.resolutions.getOrNull(0))?.let {
                webcam.device.resolution = it
            }
            webcam.open(true, sixtyFpsDelayCalculator)
            webcam.addWebcamListener(webcamListener)
            currentVideo.update { webcam }
        }
    }

    override fun close() {
        currentVideo.value?.takeIf { it.isOpen }
            ?.let {
                it.close()
                it.removeWebcamListener(webcamListener)
            }
        currentVideo.update { null }
    }

    override fun collectCurrentWebcam(): Flow<Webcam?> = currentVideo

    override fun collectAvailableImageData(): StateFlow<ImageData?> = availableImageDataFlow

    private val webcamListener: WebcamListener = object : WebcamListener {
        override fun webcamOpen(event: WebcamEvent) {
            videoEventFlow.update { VideoState.Open }
        }

        override fun webcamClosed(event: WebcamEvent) {
            videoEventFlow.update { VideoState.Closed }
        }

        override fun webcamDisposed(event: WebcamEvent) {
            videoEventFlow.update { VideoState.Disposed }
        }

        override fun webcamImageObtained(event: WebcamEvent) {
            videoEventFlow.update { VideoState.ImageObtained }
            availableImageDataFlow.update {
                ImageData(
                    image = event.image,
                    timestamp = System.currentTimeMillis(),
                    deviceFrameRate = currentVideo.value?.fps ?: 0.0,
                    rawFrameRate = frameRateCounter.calculateFrameRate()
                )
            }
        }
    }
}

private val sixtyFpsDelayCalculator = { snapshotDuration: Long, _: Double ->
    max(((1000 / 60) - snapshotDuration).toDouble(), 0.0).toLong()
}
