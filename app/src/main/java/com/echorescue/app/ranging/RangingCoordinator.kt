package com.echorescue.app.ranging

import com.echorescue.app.audio.ChirpDetection
import com.echorescue.app.audio.ChirpDetector
import com.echorescue.app.audio.ChirpEmitter
import com.echorescue.app.ble.RescuerCentralController
import com.echorescue.app.platform.DeviceProfile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withContext

data class MeasurementResult(
    val distanceMeters: Double,
    val confidence: Int,
    val noiseFloorDb: Float,
    val signalPower: Float
)

class RangingCoordinator(
    private val centralController: RescuerCentralController,
    private val emitter: ChirpEmitter,
    private val detector: ChirpDetector,
    private val profileProvider: () -> DeviceProfile
) {
    suspend fun measure(): Result<MeasurementResult> = withContext(Dispatchers.Default) {
        val detectionDeferred = CompletableDeferred<ChirpDetection>()
        var outboundStart = 0L
        detector.start { detection ->
            if (!detectionDeferred.isCompleted &&
                (outboundStart == 0L || detection.timestampNanos - outboundStart > 60_000_000L)
            ) {
                detectionDeferred.complete(detection)
            }
        }

        val armResult = centralController.armVictim()
        if (armResult.isFailure) {
            return@withContext Result.failure(armResult.exceptionOrNull() ?: IllegalStateException("Unable to arm victim."))
        }

        outboundStart = emitter.emit()
        val detection = withTimeout(4_000L) {
            detectionDeferred.await()
        }
        val profile = profileProvider()
        val elapsedSeconds = (detection.timestampNanos - outboundStart).toDouble() / 1_000_000_000.0
        val distance = DistanceCalculator.estimateDistanceMeters(
            elapsedSeconds = elapsedSeconds,
            fixedVictimDelaySeconds = profile.victimReplyDelayMs / 1000.0,
            calibrationOffsetMeters = profile.calibrationOffsetMeters
        ).coerceIn(0.0, 30.0)
        val confidence = DistanceCalculator.estimateConfidence(detection.signalPower, detection.noiseFloor)

        Result.success(
            MeasurementResult(
                distanceMeters = distance,
                confidence = confidence,
                noiseFloorDb = detection.noiseFloorDb,
                signalPower = detection.signalPower
            )
        )
    }

    fun stopListening() {
        detector.stop()
    }

    fun release() {
        detector.release()
    }
}
