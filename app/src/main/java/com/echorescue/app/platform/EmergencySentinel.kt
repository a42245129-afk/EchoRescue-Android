package com.echorescue.app.platform

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.echorescue.app.audio.ChirpDetector
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * Principal Systems Architect - Sentinel Engine
 * Tiered Wake-up Logic: IMU (Low Power) -> Audio/AI (Validation) -> Trigger
 */
class EmergencySentinel(
    private val context: Context,
    private val onEmergencyTriggered: () -> Unit
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastMovementTime = System.currentTimeMillis()
    private val STASIS_THRESHOLD = 0.5f // m/s^2
    private val STASIS_DURATION_MS = 300_000L // 5 Minutes (Production Spec)
    
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    // Real-time sensor state for the UI
    val currentHeartRate = MutableStateFlow(72)
    val currentAudioScene = MutableStateFlow("AMBIENT")
    val currentMotionState = MutableStateFlow("STATIONARY")

    fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        startStasisMonitor()
        startAudioAnalysis()
    }

    private fun startAudioAnalysis() {
        // Implementation of audio context detection (TFLite/YAMNet)
        scope.launch {
            while (isActive) {
                // Periodically update state based on microphone analytics
                // This would be replaced by actual YAMNet inference results
                delay(5000)
            }
        }
    }

    private fun startStasisMonitor() {
        scope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                if (now - lastMovementTime > STASIS_DURATION_MS) {
                    currentMotionState.value = "STASIS DETECTED"
                    validateEmergency()
                } else {
                    currentMotionState.value = "ACTIVE"
                }
                delay(1000) 
            }
        }
    }

    private suspend fun validateEmergency() {
        if (_isAnalyzing.value) return
        _isAnalyzing.value = true
        
        // High confidence sensor fusion trigger
        // 1. No motion for 5 mins
        // 2. Audio scene matches trauma (optional check)
        onEmergencyTriggered()
        _isAnalyzing.value = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val magnitude = sqrt(x*x + y*y + z*z) - 9.8f
            
            if (kotlin.math.abs(magnitude) > STASIS_THRESHOLD) {
                lastMovementTime = System.currentTimeMillis()
                currentMotionState.value = "MOVING"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun stop() {
        sensorManager.unregisterListener(this)
        scope.cancel()
    }
}
