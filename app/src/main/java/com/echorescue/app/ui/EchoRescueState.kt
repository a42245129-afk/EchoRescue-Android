package com.echorescue.app.ui

import com.echorescue.app.localization.AnchorMeasurement
import com.echorescue.app.localization.VictimEstimate
import com.echorescue.app.medical.AssistantAvailability
import com.echorescue.app.platform.DeviceProfile
import com.echorescue.app.platform.PermissionSnapshot

enum class MainTab {
    Rescue,
    Medical,
    Settings
}

enum class RescueMode {
    Rescuer,
    Victim
}

enum class EmergencyRole {
    Off,
    Victim,
    Rescuer
}

data class EchoRescueState(
    val selectedTab: MainTab = MainTab.Rescue,
    val rescueMode: RescueMode = RescueMode.Rescuer,
    val emergencyRole: EmergencyRole = EmergencyRole.Off,
    val permissions: PermissionSnapshot = PermissionSnapshot(),
    val status: String = "Ready for field operations.",
    val victimActive: Boolean = false,
    val connectedVictimName: String? = null,
    val distanceMeters: Double = 0.0,
    val confidence: Int = 0,
    val noiseFloorDb: Float = 0f,
    val signalPower: Float = 0f,
    val deviceProfile: DeviceProfile? = null,
    val calibrationReferenceMeters: String = "1.0",
    val anchorMeasurements: List<AnchorMeasurement> = emptyList(),
    val victimEstimate: VictimEstimate? = null,
    val exactLocationNote: String = "Phone-only BLE plus acoustic ranging cannot guarantee sub-1 m victim localization. UWB beacon hardware is required for that tier.",
    val medicalQuestion: String = "How to treat a burn?",
    val medicalAnswer: String = "",
    val assistantAvailability: AssistantAvailability = AssistantAvailability.FallbackOnly,
    val isBusy: Boolean = false,
    val useLightTheme: Boolean = false,
    val showLanding: Boolean = true,
    val heartRate: Int = 72,
    val detectedAudio: String = "AMBIENT",
    val motionState: String = "STATIONARY",
    val aiDiagnosticLog: String = "AI SENTINEL: STANDBY",
    val lastChirpTime: Long = 0,
    val error: String? = null
)
