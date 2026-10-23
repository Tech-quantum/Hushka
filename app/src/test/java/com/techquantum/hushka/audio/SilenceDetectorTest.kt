package com.techquantum.hushka.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class SilenceDetectorTest {

    private fun block(rmsLevel: Int, size: Int = 1600): ShortArray =
        ShortArray(size) { i ->
            (sin(2.0 * PI * i / 20.0) * rmsLevel * 1.414)
                .toInt().coerceIn(-32768, 32767).toShort()
        }

    @Test
    fun silence_in_quiet_room_stops_around_1200ms() {
        val d = SilenceDetector(sampleRate = 16_000, minSpeechMs = 1000, silenceStopMs = 1200)
        val quiet = block(50)
        repeat(20) { d.feed(quiet) }
        val snap = d.snapshot()
        assertTrue(snap.shouldStop)
        assertEquals(SilenceDetector.StopReason.NO_SPEECH, snap.stopReason)
        assertTrue("elapsed=${snap.elapsedMs}", snap.elapsedMs in 1200..1300)
    }

    @Test
    fun speech_then_silence_stops_with_speech_ended() {
        val d = SilenceDetector(sampleRate = 16_000, minSpeechMs = 1000, silenceStopMs = 1200)
        repeat(3) { d.feed(block(3000)) }
        assertTrue(d.snapshot().speechStarted)
        var snap: SilenceDetector.Snapshot
        do {
            snap = d.feed(block(100))
        } while (!snap.shouldStop && snap.elapsedMs < 5000)
        assertEquals(SilenceDetector.StopReason.SPEECH_ENDED, snap.stopReason)
    }

    @Test
    fun noisy_cafe_does_not_false_trigger_speech() {
        val d = SilenceDetector(sampleRate = 16_000)
        val noise = block(1500)
        repeat(20) { d.feed(noise) }
        assertFalse(d.snapshot().speechStarted)
        assertTrue(d.shouldStop)
    }

    @Test
    fun short_spike_does_not_count_as_speech() {
        val d = SilenceDetector(sampleRate = 16_000)
        d.feed(block(30_000, size = 1600))
        d.feed(block(100))
        d.feed(block(100))
        assertFalse(d.snapshot().speechStarted)
    }

    @Test
    fun scattered_spikes_do_not_accumulate_into_speech() {
        val d = SilenceDetector(sampleRate = 16_000, maxGapMs = 500)
        repeat(3) {
            d.feed(block(5000))
            repeat(6) { d.feed(block(80)) }
        }
        assertFalse(d.snapshot().speechStarted)
    }

    @Test
    fun continuous_speech_hits_max_time() {
        val d = SilenceDetector(sampleRate = 16_000, maxRecordMs = 5000)
        var snap = d.snapshot()
        repeat(60) {
            snap = d.feed(block(5000))
            if (snap.shouldStop) return@repeat
        }
        assertTrue(snap.shouldStop)
        assertEquals(SilenceDetector.StopReason.MAX_TIME, snap.stopReason)
        assertTrue("elapsed=${snap.elapsedMs}", snap.elapsedMs in 5000..5100)
    }
}
