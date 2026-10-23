package com.techquantum.hushka.audio

import kotlin.math.sqrt

class SilenceDetector(
    private val sampleRate: Int = 16_000,
    private val minSpeechMs: Long = 1_000L,
    private val silenceStopMs: Long = 1_200L,
    private val maxRecordMs: Long = 5_000L,
    private val calibrationStartMs: Long = 100L,
    private val calibrationEndMs: Long = 400L,
    private val maxNoiseFloor: Double = 800.0,
    private val minVoicedMs: Long = 150L,
    private val maxGapMs: Long = 500L,
    private val noiseMultiplier: Double = 3.0,
    private val floorThreshold: Double = 800.0,
    private val stopThresholdRatio: Double = 0.7,
) {
    enum class StopReason { MAX_TIME, SPEECH_ENDED, NO_SPEECH }

    data class Snapshot(
        val elapsedMs: Long,
        val voicedMs: Long,
        val silenceMs: Long,
        val noiseFloor: Double,
        val lastRms: Double,
        val startThreshold: Double,
        val speechStarted: Boolean,
        val shouldStop: Boolean,
        val stopReason: StopReason?,
    )

    private var elapsedMs = 0L
    private var voicedMs = 0L
    private var silenceMs = 0L
    private var noiseFloor = Double.MAX_VALUE
    private var lastRms = 0.0
    private var speechStarted = false
    private var stopReason: StopReason? = null

    fun feed(bytes: ByteArray): Snapshot {
        val shorts = ShortArray(bytes.size / 2)
        for (i in shorts.indices) {
            shorts[i] = ((bytes[2 * i].toInt() and 0xFF) or
                    (bytes[2 * i + 1].toInt() shl 8)).toShort()
        }
        return feed(shorts)
    }

    fun feed(samples: ShortArray): Snapshot {
        if (stopReason != null) return snapshot()
        if (samples.isEmpty()) return snapshot()
        val blockMs = (samples.size * 1000L) / sampleRate
        val blockRms = rms(samples)
        if (elapsedMs in calibrationStartMs until calibrationEndMs) {
            val clamped = blockRms.coerceAtMost(maxNoiseFloor)
            noiseFloor = if (noiseFloor == Double.MAX_VALUE) clamped else minOf(noiseFloor, clamped)
        }
        val effectiveNoise = if (noiseFloor == Double.MAX_VALUE) floorThreshold else noiseFloor
        val startThreshold = maxOf(effectiveNoise * noiseMultiplier, floorThreshold)
        val stopThreshold = startThreshold * stopThresholdRatio
        val activeThreshold = if (speechStarted) stopThreshold else startThreshold
        val voiced = blockRms > activeThreshold
        if (voiced) {
            voicedMs += blockMs
            silenceMs = 0L
        } else {
            silenceMs += blockMs
            if (silenceMs > maxGapMs) voicedMs = 0L
        }
        if (voicedMs >= minVoicedMs) speechStarted = true
        elapsedMs += blockMs
        lastRms = blockRms
        stopReason = when {
            elapsedMs >= maxRecordMs -> StopReason.MAX_TIME
            elapsedMs >= minSpeechMs && silenceMs >= silenceStopMs ->
                if (speechStarted) StopReason.SPEECH_ENDED else StopReason.NO_SPEECH
            else -> null
        }
        return Snapshot(
            elapsedMs = elapsedMs,
            voicedMs = voicedMs,
            silenceMs = silenceMs,
            noiseFloor = effectiveNoise,
            lastRms = blockRms,
            startThreshold = activeThreshold,
            speechStarted = speechStarted,
            shouldStop = stopReason != null,
            stopReason = stopReason,
        )
    }

    fun snapshot(): Snapshot = Snapshot(
        elapsedMs = elapsedMs,
        voicedMs = voicedMs,
        silenceMs = silenceMs,
        noiseFloor = if (noiseFloor == Double.MAX_VALUE) 0.0 else noiseFloor,
        lastRms = lastRms,
        startThreshold = if (noiseFloor == Double.MAX_VALUE) floorThreshold
            else maxOf(noiseFloor * noiseMultiplier, floorThreshold),
        speechStarted = speechStarted,
        shouldStop = stopReason != null,
        stopReason = stopReason,
    )

    val shouldStop: Boolean get() = stopReason != null

    fun reset() {
        elapsedMs = 0L
        voicedMs = 0L
        silenceMs = 0L
        noiseFloor = Double.MAX_VALUE
        lastRms = 0.0
        speechStarted = false
        stopReason = null
    }

    private fun rms(samples: ShortArray): Double {
        var sum = 0.0
        for (s in samples) {
            val v = s.toDouble()
            sum += v * v
        }
        return sqrt(sum / samples.size)
    }
}
