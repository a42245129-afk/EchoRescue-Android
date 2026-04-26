package com.echorescue.app.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class GoertzelDetector(
    private val sampleRate: Int = 48_000,
    private val blockSize: Int = 1024
) {
    fun analyze(samples: FloatArray, targetFrequency: Int): Float {
        val k = (blockSize * (targetFrequency.toFloat() / sampleRate)).roundToInt()
        val omega = (2.0 * PI * k.toDouble()) / blockSize.toDouble()
        val coefficient = 2.0 * cos(omega)

        var q0 = 0.0
        var q1 = 0.0
        var q2 = 0.0
        var totalEnergy = 0.0

        for (sample in samples) {
            q0 = coefficient * q1 - q2 + sample
            q2 = q1
            q1 = q0
            totalEnergy += sample * sample
        }

        val real = q1 - q2 * cos(omega)
        val imaginary = q2 * sin(omega)
        val magnitudeSquared = real * real + imaginary * imaginary
        return if (totalEnergy <= 0.0) 0f else (magnitudeSquared / totalEnergy).toFloat().coerceAtLeast(0f)
    }
}
