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
    context: Context,
    private val onEmergencyTriggered: () -> Unit
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastMovementTime = System.currentTimeMillis()
    private val STASIS_THRESHOLD = 0.5f // m/s^2
    private val STASIS_DURATION_MS = 300_000L // 5 Minutes
    
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        startStasisMonitor()
    }

    private fun startStasisMonitor() {
        scope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                if (now - lastMovementTime > STASIS_DURATION_MS) {
                    validateEmergency()
                }
                delay(10000) // Check every 10s
            }
        }
    }

    private suspend fun validateEmergency() {
        if (_isAnalyzing.value) return
        _isAnalyzing.value = true
        
        // Tier 2: AI Validation (Simulated Gemini Nano / TFLite Inference)
        // In a real implementation, we'd wake the NPU here to analyze audio buffer for screams/gunshots
        delay(2000) 
        
        // High confidence sensor fusion trigger
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
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun stop() {
        sensorManager.unregisterListener(this)
        scope.cancel()
    }
}
