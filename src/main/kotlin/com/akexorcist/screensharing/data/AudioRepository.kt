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
        while(isActive) {
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
        while(isActive) {
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

    private fun getOutputDataLine(
        comparator: (List<Pair<Mixer, Mixer.Info>>) -> Mixer.Info?,
        audioFormat: AudioFormat?,
    ): SourceDataLine? = comparator(getAvailableAudioOutputMixers())?.let { outputInfo ->
        AudioSystem.getSourceDataLine(audioFormat, outputInfo)
    }

    override fun startAudioPlayback() {
        val inputMixer = this.inputMixer ?: return
//        val audioFormat = inputMixer.targetLineInfo
//            ?.find { it.lineClass == TargetDataLine::class.java }
//            ?.let { (inputMixer.getLine(it) as TargetDataLine).format }
//            ?: return

        val inputAudioFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            44100f,
            16,
            1,
            2,
            44100f,
            true,
        )
        val outputAudioFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            44100f,
            16,
            2,
            4,
            44100f,
            true,
        )

        val targetDataLine = AudioSystem.getTargetDataLine(inputAudioFormat, inputMixer.mixerInfo).apply {
            open(inputAudioFormat)
            start()
        }

        val outputMixer = this.outputMixer ?: return
//        val sourceDataLine: SourceDataLine = outputMixer.sourceLineInfo
//                ?.find { it.lineClass == SourceDataLine::class.java }
//                ?.let { inputMixer.getLine(it) as SourceDataLine } ?: return
        val sourceDataLine = AudioSystem.getSourceDataLine(outputAudioFormat, outputMixer.mixerInfo).apply {
            open(outputAudioFormat)
            start()
        }

        println("currentAudioPlaybackJob")
        this.currentAudioPlaybackJob = coroutineScope.launch {
            val buffer = ByteArray(4096)
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
}
