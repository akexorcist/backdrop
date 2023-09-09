package com.akexorcist.backdrop.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.sound.sampled.*

interface AudioRepository {
    fun getAvailableAudioInputs(): Flow<List<Pair<Mixer, Mixer.Info>>>

    fun getAvailableAudioOutputs(): Flow<List<Pair<Mixer, Mixer.Info>>>

    fun setAudioInput(name: String)

    fun setAudioOutput(name: String)

    fun startAudioPlayback(): PlaybackStatus

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
            .filter { (mixer, _) -> getSupportedLine(mixer, TargetDataLine::class.java) != null }
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
            .filter { (mixer, _) -> getSupportedLine(mixer, SourceDataLine::class.java) != null }
    }

    override fun setAudioInput(name: String) {
        this.inputMixer = AudioSystem.getMixerInfo()
            .find { it.name == name && getSupportedLine(AudioSystem.getMixer(it), TargetDataLine::class.java) != null }
            ?.let { AudioSystem.getMixer(it) }
    }

    override fun setAudioOutput(name: String) {
        this.outputMixer = AudioSystem.getMixerInfo()
            .find { it.name == name && getSupportedLine(AudioSystem.getMixer(it), SourceDataLine::class.java) != null }
            ?.let { AudioSystem.getMixer(it) }
    }

    override fun startAudioPlayback(): PlaybackStatus {
        val inputMixer = this.inputMixer ?: return PlaybackStatus.AudioInputError
        val outputMixer = this.outputMixer ?: return PlaybackStatus.AudioOutputError
        val targetDataLine = getSupportedLine(inputMixer, TargetDataLine::class.java)
            ?: return PlaybackStatus.AudioInputError
        val sourceDataLine = getSupportedLine(outputMixer, SourceDataLine::class.java)
            ?: return PlaybackStatus.AudioOutputError
        targetDataLine.apply {
            open()
            start()
        }

        println("Volume Control ${inputMixer.isControlSupported(FloatControl.Type.MASTER_GAIN)}")
        sourceDataLine.apply {
            open()
            start()
        }

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
        return PlaybackStatus.Playing
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
        null
    } catch (e: IllegalArgumentException) {
        null
    } as? T
}
