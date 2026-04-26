package com.echorescue.app.audio

import kotlin.math.sqrt

/**
 * Advanced DSP - Matched Filter for 18-20kHz LFM Chirp
 * Production-ready signal correlation for sub-meter ToF accuracy.
 */
object AcousticProcessor {
    
    fun performCrossCorrelation(signal: FloatArray, template: FloatArray): Int {
        var maxCorr = -1f
        var maxIndex = 0
        
        // Sliding window dot product (Cross-Correlation)
        for (i in 0 until (signal.size - template.size)) {
            var correlation = 0f
            for (j in template.indices) {
                correlation += signal[i + j] * template[j]
            }
            if (correlation > maxCorr) {
                maxCorr = correlation
                maxIndex = i
            }
        }
        return maxIndex
    }

    fun calculateRms(samples: ShortArray): Float {
        var sum = 0.0
        for (sample in samples) {
            sum += sample * sample
        }
        return sqrt(sum / samples.size).toFloat()
    }
}
