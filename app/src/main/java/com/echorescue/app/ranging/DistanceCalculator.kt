package com.echorescue.app.ranging

object DistanceCalculator {
    private const val SpeedOfSoundMetersPerSecond = 343.0

    fun estimateDistanceMeters(
        elapsedSeconds: Double,
        fixedVictimDelaySeconds: Double = 0.035,
        calibrationOffsetMeters: Double = 0.0
    ): Double {
        val acousticRoundTripSeconds = (elapsedSeconds - fixedVictimDelaySeconds).coerceAtLeast(0.0)
        return ((acousticRoundTripSeconds * SpeedOfSoundMetersPerSecond) / 2.0) + calibrationOffsetMeters
    }

    fun estimateConfidence(signalPower: Float, noiseFloor: Float): Int {
        val value = ((signalPower / (noiseFloor + 0.01f)) * 100f).toInt()
        return value.coerceIn(0, 100)
    }
}
