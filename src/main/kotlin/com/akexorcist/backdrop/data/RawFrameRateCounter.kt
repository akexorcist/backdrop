package com.akexorcist.backdrop.data

class RawFrameRateCounter {
    private var lastTimestamp: Long = 0
    private var frameCount: Int = 0

    fun calculateFrameRate(): Double {
        val currentTimestamp = System.currentTimeMillis()
        frameCount++
        var frameRate = 0.0
        if (lastTimestamp != 0L) {
            val elapsedTime = currentTimestamp - lastTimestamp
            if (elapsedTime > 0) {
                frameRate = 1000.0 / elapsedTime
            }
        }
        lastTimestamp = currentTimestamp
        return frameRate
    }

    fun reset() {
        lastTimestamp = 0
        frameCount = 0
    }
}