package com.akexorcist.screensharing.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.sound.sampled.*


interface AudioRepository {
    fun getAvailableAudioInputs(): Flow<List<Pair<Mixer, Mixer.Info>>>

    fun getAvailableAudioOutputs(): Flow<List<Pair<Mixer, Mixer.Info>>>

    fun setAudioInput(name: String)

    fun setAudioOutput(name: String)

    fun startAudioPlayback()

    fun stopAudioPlayback()
}

class DefaultAudioRepository : AudioRepository {
    private var inputMixer: Mixer? = null
    private var outputMixer: Mixer? = null

    private var targetDataLine: TargetDataLine? = null
    private var sourceDataLine: SourceDataLine? = null

    private var currentAudioPlaybackJob: Job? = null

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.IO)

    override fun getAvailableAudioInputs(): Flow<List<Pair<Mixer, Mixer.Info>>> = callbackFlow {
        while (isActive) {
            trySend(getAvailableAudioInputMixers())
            delay(1000)
        }
    }

    private fun getAvailableAudioInputMixers(): List<Pair<Mixer, Mixer.Info>> {
        return AudioSystem.getMixerInfo()
            .map { mixerInfo -> AudioSystem.getMixer(mixerInfo) to mixerInfo }
            .filter { (mixer, _) -> mixer.targetLineInfo.any { it.lineClass == TargetDataLine::class.java } }
    }

    override fun getAvailableAudioOutputs(): Flow<List<Pair<Mixer, Mixer.Info>>> = callbackFlow {
        while (isActive) {
            trySend(getAvailableAudioOutputMixers())
            delay(1000)
        }
    }

    private fun getAvailableAudioOutputMixers(): List<Pair<Mixer, Mixer.Info>> {
        return AudioSystem.getMixerInfo()
            .map { mixerInfo -> AudioSystem.getMixer(mixerInfo) to mixerInfo }
            .filter { (mixer, _) -> mixer.sourceLineInfo.any { it.lineClass == SourceDataLine::class.java } }
    }

    override fun setAudioInput(name: String) {
        this.inputMixer = AudioSystem.getMixerInfo()
            .find { it.name == name }
            ?.let { AudioSystem.getMixer(it) }
    }

    override fun setAudioOutput(name: String) {
        this.outputMixer = AudioSystem.getMixerInfo()
            .find { it.name == name }
            ?.let { AudioSystem.getMixer(it) }
    }

    override fun startAudioPlayback() {
        println("startAudioPlayback")
        val inputMixer = this.inputMixer ?: return
        val outputMixer = this.outputMixer ?: return
        val targetDataLine = getSupportedLine(inputMixer, TargetDataLine::class.java) ?: return
        val sourceDataLine = getSupportedLine(outputMixer, SourceDataLine::class.java) ?: return
        targetDataLine.apply {
            open()
            start()
        }
        sourceDataLine.apply {
            open()
            start()
        }

        println("playing")
        this.currentAudioPlaybackJob = coroutineScope.launch {
            val buffer = ByteArray(1024)
            while (isActive) {
                val byteRead = targetDataLine.read(buffer, 0, buffer.size)
                if (byteRead == -1) break
                sourceDataLine.write(buffer, 0, byteRead)
            }
        }

        this.targetDataLine = targetDataLine
        this.sourceDataLine = sourceDataLine
    }

    override fun stopAudioPlayback() {
        this.targetDataLine
            ?.takeIf { it.isOpen }
            ?.apply {
                stop()
                close()
            }
        this.sourceDataLine
            ?.takeIf { it.isOpen }
            ?.apply {
                stop()
                close()
            }
        this.currentAudioPlaybackJob?.cancel()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getSupportedLine(mixer: Mixer, lineClass: Class<T>): T? = try {
        mixer.getLine((DataLine.Info(lineClass, null)))
    } catch (e: LineUnavailableException) {
        e.printStackTrace()
        null
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        null
    } as? T

    private fun printAudioFormat(label: String, format: AudioFormat) {
        println(
            "$label :: Sample Rate ${format.sampleRate}, " +
                    "Encoding ${format.encoding}, " +
                    "Channels ${format.channels}, " +
                    "Sample Size ${format.sampleSizeInBits}, " +
                    "Frame Size ${format.frameSize}, " +
                    "Frame rate ${format.frameRate}, " +
                    "Big Endian ${format.isBigEndian}"
        )
    }
}
