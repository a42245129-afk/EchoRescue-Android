package com.echorescue.app.audio

import android.os.SystemClock
import kotlin.math.PI
import kotlin.math.sin

/**
 * DSP Engineer - Exact Distance Calculation
 * Uses LFM (Linear Frequency Modulation) and Cross-Correlation for sub-meter ToF.
 */
object AcousticRangingEngine {
    private const val SPEED_OF_SOUND = 343.0 // m/s at 20C (Production Standard)
    private const val SAMPLE_RATE = 48000
    
    // Cross-Correlation / Matched Filter result logic
    fun estimateToA(signal: FloatArray, template: FloatArray): Int {
        var maxCorr = -1f
        var maxLag = 0
        val searchRange = signal.size - template.size
        
        for (lag in 0 until searchRange) {
            var corr = 0f
            for (i in template.indices) {
                corr += signal[lag + i] * template[i]
            }
            if (corr > maxCorr) {
                maxCorr = corr
                maxLag = lag
            }
        }
        return maxLag
    }

    fun calculateDistance(
        emissionNanos: Long,
        arrivalNanos: Long,
        victimProcessingDelayMs: Long = 0,
        calibrationOffsetMeters: Double = 0.0
    ): Double {
        val deltaNanos = arrivalNanos - emissionNanos
        val deltaSeconds = (deltaNanos.toDouble() / 1_000_000_000.0) - (victimProcessingDelayMs / 1000.0)
        
        // Exact 1-way distance calculation for synced systems or 2-way for Echo
        val rawDistance = SPEED_OF_SOUND * deltaSeconds
        return (rawDistance + calibrationOffsetMeters).coerceAtLeast(0.0)
    }

    /**
     * LFM Chirp Generator: 18kHz to 20kHz
     */
    fun generateLfmChirp(durationMs: Int = 100): ShortArray {
        val sampleCount = (SAMPLE_RATE * durationMs) / 1000
        val samples = ShortArray(sampleCount)
        val f0 = 18000.0
        val f1 = 20000.0
        val T = durationMs / 1000.0

        for (n in 0 until sampleCount) {
            val t = n.toDouble() / SAMPLE_RATE
            // Phase = 2*PI * (f0*t + (BW/(2*T))*t^2)
            val phase = 2.0 * PI * (f0 * t + ((f1 - f0) / (2.0 * T)) * (t * t))
            samples[n] = (0.8 * Short.MAX_VALUE * sin(phase)).toInt().toShort()
        }
        return samples
    }
}
