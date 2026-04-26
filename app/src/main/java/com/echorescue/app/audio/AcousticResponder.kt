package com.echorescue.app.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import kotlinx.coroutines.isActive

class AcousticResponder(
    private val detector: ChirpDetector,
    private val emitter: ChirpEmitter,
    private var responseDelayMs: Long = 35L
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var activeJob: Job? = null
    private var periodicJob: Job? = null
    private var armed = false

    fun startPeriodicPinging() {
        if (periodicJob != null) return
        periodicJob = scope.launch {
            while (isActive) {
                emitter.emit()
                // Duty cycle: emit chirp, then sleep for 58s
                delay(58_000) 
            }
        }
    }

    fun stopPeriodicPinging() {
        periodicJob?.cancel()
        periodicJob = null
    }

    suspend fun armForSingleResponse(onStatus: (String) -> Unit) {
        disarm()
        armed = true
        detector.start { detection ->
            if (!armed) return@start
            armed = false
            activeJob = scope.launch {
                onStatus("Inbound chirp detected at victim. Emitting calibrated reply.")
                delay(responseDelayMs)
                emitter.emit()
                onStatus("Victim reply chirp emitted.")
            }
        }
        onStatus("Victim acoustic responder armed.")
    }

    fun disarm() {
        armed = false
        activeJob?.cancel()
        activeJob = null
        detector.stop()
    }

    fun updateResponseDelay(delayMs: Long) {
        responseDelayMs = delayMs
    }

    fun release() {
        disarm()
        stopPeriodicPinging()
        detector.release()
        scope.cancel()
    }
}
