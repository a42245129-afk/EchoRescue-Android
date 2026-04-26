package com.echorescue.app.ui

import android.Manifest
import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.echorescue.app.audio.AcousticResponder
import com.echorescue.app.audio.ChirpDetector
import com.echorescue.app.audio.ChirpEmitter
import com.echorescue.app.ble.RescuerCentralController
import com.echorescue.app.ble.VictimPeripheralController
import com.echorescue.app.data.GuideRepository
import com.echorescue.app.localization.AnchorMeasurement
import com.echorescue.app.localization.RescueGeometry
import com.echorescue.app.localization.TrilaterationSolver
import com.echorescue.app.medical.GuideFallbackAssistant
import com.echorescue.app.medical.HybridMedicalAssistant
import com.echorescue.app.platform.CalibrationStore
import com.echorescue.app.platform.DeviceProfileManager
import com.echorescue.app.platform.PermissionSnapshot
import com.echorescue.app.platform.VictimForegroundService
import com.echorescue.app.ranging.RangingCoordinator
import com.echorescue.app.uwb.UnavailableUwbRangingController
import com.echorescue.app.platform.EmergencySentinel
import com.echorescue.app.medical.EmsAgent
import com.echorescue.app.audio.AcousticRangingEngine
import com.echorescue.app.medical.GeminiNanoAgent
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EchoRescueViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val calibrationStore = CalibrationStore(application)
    private val deviceProfileManager = DeviceProfileManager(application, calibrationStore)
    private val guideRepository = GuideRepository(application)
    private val medicalAssistant = HybridMedicalAssistant(GuideFallbackAssistant(guideRepository))
    private val uwbRangingController = UnavailableUwbRangingController()

    private val victimResponder = AcousticResponder(
        detector = ChirpDetector(),
        emitter = ChirpEmitter()
    )
    private val victimPeripheral = VictimPeripheralController(application, victimResponder)
    private val rescuerCentral = RescuerCentralController(application)
    private val emsAgent = EmsAgent()
    private val geminiNano = GeminiNanoAgent(application)
    private val sentinel = EmergencySentinel(application) {
        startVictimMode() // Autonomous trigger
        _state.update { it.copy(status = "AI TRIGGER: EMERGENCY DETECTED") }
    }
    private val rangingCoordinator = RangingCoordinator(
        centralController = rescuerCentral,
        emitter = ChirpEmitter(),
        detector = ChirpDetector(),
        profileProvider = deviceProfileManager::currentProfile
    )

    private val _state = MutableStateFlow(EchoRescueState())
    val state: StateFlow<EchoRescueState> = _state.asStateFlow()

    init {
        sentinel.start()
        startAiSimulation()
        
        // Wire real sensor flows to the UI state
        viewModelScope.launch {
            sentinel.currentHeartRate.collect { hr ->
                _state.update { it.copy(heartRate = hr) }
            }
        }
        viewModelScope.launch {
            sentinel.currentAudioScene.collect { scene ->
                _state.update { it.copy(detectedAudio = scene) }
            }
        }
        viewModelScope.launch {
            sentinel.currentMotionState.collect { motion ->
                _state.update { it.copy(motionState = motion) }
            }
        }

        viewModelScope.launch {
            val availability = medicalAssistant.availability()
            val emergencyRole = calibrationStore.loadEmergencyRole().toEmergencyRole()
            _state.update {
                it.copy(
                    assistantAvailability = availability,
                    deviceProfile = deviceProfileManager.currentProfile(),
                    emergencyRole = emergencyRole,
                    rescueMode = when (emergencyRole) {
                        EmergencyRole.Victim -> RescueMode.Victim
                        EmergencyRole.Rescuer -> RescueMode.Rescuer
                        EmergencyRole.Off -> it.rescueMode
                    }
                )
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    private fun startAiSimulation() {
        viewModelScope.launch {
            while (isActive) {
                delay(4000)
                
                geminiNano.runTraumaAnalysis(
                    _state.value.heartRate,
                    _state.value.detectedAudio,
                    _state.value.motionState
                )
                
                val diagnosis = geminiNano.lastAssessment.value
                _state.update { 
                    it.copy(
                        aiDiagnosticLog = diagnosis,
                        lastChirpTime = if (System.currentTimeMillis() % 60000 < 2000) System.currentTimeMillis() else it.lastChirpTime
                    )
                }
            }
        }
    }

    fun dismissLanding() {
        _state.update { it.copy(showLanding = false) }
    }

    fun selectRescueMode(mode: RescueMode) {
        _state.update { it.copy(rescueMode = mode, error = null) }
    }

    fun setEmergencyRole(role: EmergencyRole) {
        calibrationStore.saveEmergencyRole(role.name.uppercase())
        _state.update {
            it.copy(
                emergencyRole = role,
                rescueMode = when (role) {
                    EmergencyRole.Victim -> RescueMode.Victim
                    EmergencyRole.Rescuer -> RescueMode.Rescuer
                    EmergencyRole.Off -> it.rescueMode
                },
                status = when (role) {
                    EmergencyRole.Victim -> "Emergency auto-arm set to victim beacon mode."
                    EmergencyRole.Rescuer -> "Emergency auto-arm set to rescuer scan mode."
                    EmergencyRole.Off -> "Emergency auto-arm disabled."
                }
            )
        }
    }

    fun setCalibrationReference(value: String) {
        _state.update { it.copy(calibrationReferenceMeters = value) }
    }

    fun updatePermissionStatus(results: Map<String, Boolean>) {
        _state.update {
            it.copy(
                permissions = PermissionSnapshot(
                    bluetoothScan = results[Manifest.permission.BLUETOOTH_SCAN] == true,
                    bluetoothConnect = results[Manifest.permission.BLUETOOTH_CONNECT] == true,
                    bluetoothAdvertise = results[Manifest.permission.BLUETOOTH_ADVERTISE] == true,
                    recordAudio = results[Manifest.permission.RECORD_AUDIO] == true,
                    postNotifications = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        results[Manifest.permission.POST_NOTIFICATIONS] == true
                    } else {
                        true
                    })
                )
            )
        }
    }

    fun activateEmergencyRoleIfPossible() {
        val current = state.value
        if (!current.permissions.allGranted) return

        when (current.emergencyRole) {
            EmergencyRole.Victim -> if (!current.victimActive) {
                startVictimMode()
            }
            EmergencyRole.Rescuer -> if (current.connectedVictimName == null && !current.isBusy) {
                scanAndConnect()
            }
            EmergencyRole.Off -> Unit
        }
    }

    fun startVictimMode() {
        runCatching {
            victimResponder.updateResponseDelay(deviceProfileManager.currentProfile().victimReplyDelayMs)
            startVictimForegroundService("Victim beacon active and waiting for rescuer pairing.")
            victimPeripheral.start { status ->
                _state.update {
                    it.copy(
                        victimActive = true,
                        status = status,
                        error = null
                    )
                }
            }
        }.onFailure { throwable ->
            _state.update {
                it.copy(
                    victimActive = false,
                    error = throwable.message,
                    status = "Unable to start victim mode."
                )
            }
        }
    }

    fun stopVictimMode() {
        victimPeripheral.stop()
        appContext.stopService(Intent(appContext, VictimForegroundService::class.java))
        _state.update {
            it.copy(
                victimActive = false,
                status = "Victim beacon stopped."
            )
        }
    }

    fun scanAndConnect() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isBusy = true, status = "Scanning for victim BLE beacon...", error = null) }
                val result = rescuerCentral.scanAndConnect()
                result.onSuccess { peer ->
                    _state.update {
                        it.copy(
                            connectedVictimName = peer.name,
                            status = "Connected to ${peer.name}. Ready for acoustic ranging.",
                            isBusy = false
                        )
                    }
                }.onFailure { throwable ->
                    _state.update {
                        it.copy(
                            error = throwable.message,
                            status = "Victim scan/connect failed.",
                            isBusy = false
                        )
                    }
                }
            } catch (throwable: Throwable) {
                _state.update {
                    it.copy(
                        error = throwable.message,
                        status = "Victim scan/connect failed.",
                        isBusy = false
                    )
                }
            }
        }
    }

    fun runMeasurement() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isBusy = true, status = "Arming victim and running acoustic round-trip measurement...", error = null) }
                val result = if (uwbRangingController.isAvailable) {
                    uwbRangingController.measure()
                } else {
                    rangingCoordinator.measure()
                }
                result.onSuccess { measurement ->
                    _state.update {
                        it.copy(
                            distanceMeters = measurement.distanceMeters,
                            confidence = measurement.confidence,
                            noiseFloorDb = measurement.noiseFloorDb,
                            signalPower = measurement.signalPower,
                            status = if (uwbRangingController.isAvailable) "UWB measurement completed." else "Acoustic measurement completed.",
                            deviceProfile = deviceProfileManager.currentProfile(),
                            victimEstimate = it.victimEstimate,
                            isBusy = false
                        )
                    }
                }.onFailure { throwable ->
                    _state.update {
                        it.copy(
                            error = throwable.message,
                            status = "Measurement failed.",
                            isBusy = false
                        )
                    }
                }
            } catch (throwable: Throwable) {
                _state.update {
                    it.copy(
                        error = throwable.message,
                        status = "Measurement failed.",
                        isBusy = false
                    )
                }
            }
        }
    }

    fun saveCalibrationFromLastMeasurement() {
        val current = state.value
        val expectedMeters = current.calibrationReferenceMeters.toDoubleOrNull()
        if (expectedMeters == null || expectedMeters <= 0.0) {
            _state.update { it.copy(error = "Enter a valid known reference distance.", status = "Calibration not saved.") }
            return
        }

        val offsetMeters = expectedMeters - current.distanceMeters
        calibrationStore.saveCalibrationOffsetMeters(offsetMeters)
        _state.update {
            it.copy(
                deviceProfile = deviceProfileManager.currentProfile(),
                status = "Calibration saved with ${"%.2f".format(offsetMeters)} m offset.",
                error = null
            )
        }
    }

    fun recordCurrentMeasurementAtNextAnchor() {
        val current = state.value
        val nextAnchor = RescueGeometry.defaultAnchors.getOrNull(current.anchorMeasurements.size)
        if (nextAnchor == null) {
            _state.update { it.copy(error = "All default anchors are already populated. Clear anchors to resample.") }
            return
        }

        val measurement = AnchorMeasurement(
            anchor = nextAnchor,
            distanceMeters = current.distanceMeters,
            confidence = current.confidence
        )

        _state.update {
            it.copy(
                anchorMeasurements = it.anchorMeasurements + measurement,
                status = "${nextAnchor.label} recorded at ${"%.2f".format(current.distanceMeters)} m.",
                error = null
            )
        }
    }

    fun clearAnchorMeasurements() {
        _state.update {
            it.copy(
                anchorMeasurements = emptyList(),
                victimEstimate = null,
                status = "Anchor measurements cleared.",
                error = null
            )
        }
    }

    fun solveVictimLocation() {
        val estimate = TrilaterationSolver.solve(state.value.anchorMeasurements)
        if (estimate == null) {
            _state.update {
                it.copy(
                    error = "At least three anchor measurements are required for trilateration.",
                    status = "Unable to solve victim zone."
                )
            }
            return
        }

        _state.update {
            it.copy(
                victimEstimate = estimate,
                status = "Victim zone estimated near (${ "%.2f".format(estimate.position.xMeters) }, ${ "%.2f".format(estimate.position.yMeters) }) m.",
                error = null
            )
        }
    }

    fun setMedicalQuestion(question: String) {
        _state.update { it.copy(medicalQuestion = question) }
    }

    fun askMedical() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(medicalAnswer = "", isBusy = true, error = null) }
                medicalAssistant.answer(state.value.medicalQuestion).collect { chunk ->
                    _state.update { current ->
                        current.copy(
                            medicalAnswer = current.medicalAnswer + chunk
                        )
                    }
                }
                _state.update { it.copy(isBusy = false) }
            } catch (throwable: Throwable) {
                _state.update {
                    it.copy(
                        error = throwable.message,
                        isBusy = false
                    )
                }
            }
        }
    }

    override fun onCleared() {
        victimPeripheral.release()
        rescuerCentral.disconnect()
        rangingCoordinator.release()
        victimResponder.release()
        super.onCleared()
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EchoRescueViewModel(application) as T
        }
    }

    private fun startVictimForegroundService(status: String) {
        val intent = Intent(appContext, VictimForegroundService::class.java).apply {
            putExtra(VictimForegroundService.EXTRA_STATUS, status)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appContext.startForegroundService(intent)
        } else {
            appContext.startService(intent)
        }
    }

    private fun String.toEmergencyRole(): EmergencyRole {
        return when (this) {
            "VICTIM" -> EmergencyRole.Victim
            "RESCUER" -> EmergencyRole.Rescuer
            else -> EmergencyRole.Off
        }
    }
}
