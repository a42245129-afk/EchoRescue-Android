package com.echorescue.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echorescue.app.audio.ChirpDetection
import com.echorescue.app.localization.AnchorMeasurement
import com.echorescue.app.localization.VictimEstimate
import com.echorescue.app.medical.AssistantAvailability
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EchoRescueApp(
    state: EchoRescueState,
    onSelectTab: (MainTab) -> Unit,
    onSelectRescueMode: (RescueMode) -> Unit,
    onStartVictimMode: () -> Unit,
    onStopVictimMode: () -> Unit,
    onScanVictims: () -> Unit,
    onRunMeasurement: () -> Unit,
    onSetCalibrationReference: (String) -> Unit,
    onSaveCalibration: () -> Unit,
    onRecordAnchorMeasurement: () -> Unit,
    onClearAnchorMeasurements: () -> Unit,
    onSolveVictimLocation: () -> Unit,
    onSetEmergencyRole: (EmergencyRole) -> Unit,
    onAskMedical: () -> Unit,
    onQuestionChange: (String) -> Unit,
    onDismissLanding: () -> Unit,
    onToggleLightTheme: () -> Unit,
    onRetryPermissions: () -> Unit
) {
    if (state.showLanding) {
        LandingScreen(onStart = onDismissLanding)
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TacticalDashboard(
            state = state,
            onSelectRescueMode = onSelectRescueMode,
            onStartVictimMode = onStartVictimMode,
            onStopVictimMode = onStopVictimMode,
            onToggleLightTheme = onToggleLightTheme
        )
    }
}

/*
@Composable
fun EchoRescueAppLegacy(
        containerColor = Color(0xFF070B1A),
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF08101F)) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = state.selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        if (state.selectedTab == tab) Color(0xFFF75C5C) else Color(0xFF8BE9FD),
                                        CircleShape
                                    )
                            )
                        },
                        label = { Text(tab.name) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF070B1A), Color(0xFF0D1430), Color(0xFF140A24))
                    )
                )
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HeaderCard(status = state.status)
                }
                item {
                    when (state.selectedTab) {
                        MainTab.Rescue -> RescueScreen(
                            state = state,
                            onSelectRescueMode = onSelectRescueMode,
                            onStartVictimMode = onStartVictimMode,
                            onStopVictimMode = onStopVictimMode,
                            onScanVictims = onScanVictims,
                            onRunMeasurement = onRunMeasurement,
                            onSetCalibrationReference = onSetCalibrationReference,
                            onSaveCalibration = onSaveCalibration,
                            onRecordAnchorMeasurement = onRecordAnchorMeasurement,
                            onClearAnchorMeasurements = onClearAnchorMeasurements,
                            onSolveVictimLocation = onSolveVictimLocation,
                            onSetEmergencyRole = onSetEmergencyRole
                        )
                        MainTab.Medical -> MedicalScreen(
                            state = state,
                            onAskMedical = onAskMedical,
                            onQuestionChange = onQuestionChange
                        )
                        MainTab.Settings -> SettingsScreen(
                            state = state,
                            onRetryPermissions = onRetryPermissions
                        )
                    }
                }
            }
        }        
    }
}

@Composable
private fun HeaderCard(status: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xCC0B1020)),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.border(1.dp, Color(0x6645F0FF), RoundedCornerShape(28.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("ECHØRESCUE", color = Color(0xFFFA4DF3), fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Cyber-rescue operator console", color = Color(0xFF45F0FF))
            Spacer(Modifier.height(12.dp))
            Text(status, color = Color.White.copy(alpha = 0.92f))
        }
    }
}

@Composable
private fun RescueScreen(
    state: EchoRescueState,
    onSelectRescueMode: (RescueMode) -> Unit,
    onStartVictimMode: () -> Unit,
    onStopVictimMode: () -> Unit,
    onScanVictims: () -> Unit,
    onRunMeasurement: () -> Unit,
    onSetCalibrationReference: (String) -> Unit,
    onSaveCalibration: () -> Unit,
    onRecordAnchorMeasurement: () -> Unit,
    onClearAnchorMeasurements: () -> Unit,
    onSolveVictimLocation: () -> Unit,
    onSetEmergencyRole: (EmergencyRole) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ToggleCard(
            selected = state.rescueMode,
            onSelect = onSelectRescueMode
        )
        EmergencyRoleCard(
            selected = state.emergencyRole,
            onSelect = onSetEmergencyRole
        )
        OperationalLimitsCard()

        if (state.rescueMode == RescueMode.Victim) {
            VictimCard(state, onStartVictimMode, onStopVictimMode)
        } else {
            RescuerCard(state, onScanVictims, onRunMeasurement)
            GaugeCard(
                distanceMeters = state.distanceMeters,
                confidence = state.confidence,
                noiseFloorDb = state.noiseFloorDb,
                signalPower = state.signalPower
            )
            CalibrationCard(
                calibrationReferenceMeters = state.calibrationReferenceMeters,
                onSetCalibrationReference = onSetCalibrationReference,
                onSaveCalibration = onSaveCalibration
            )
            LocalizationCard(
                anchorMeasurements = state.anchorMeasurements,
                victimEstimate = state.victimEstimate,
                onRecordAnchorMeasurement = onRecordAnchorMeasurement,
                onClearAnchorMeasurements = onClearAnchorMeasurements,
                onSolveVictimLocation = onSolveVictimLocation
            )
        }
        InfoCard(title = "Precision reality") {
            Text(state.exactLocationNote, color = Color(0xFFFFC857))
        }
        state.deviceProfile?.let { profile ->
            PlatformCard(profile.modelName, profile.recommendedTransport, profile.supportsUwb, profile.calibrationOffsetMeters, profile.victimReplyDelayMs)
        }

        state.error?.let { ErrorCard(it) }
    }
}

@Composable
private fun EmergencyRoleCard(selected: EmergencyRole, onSelect: (EmergencyRole) -> Unit) {
    InfoCard(title = "Emergency auto-arm") {
        Text("Pre-arm the phone before deployment. On next launch with permissions granted, EchoRescue will automatically assume the selected role.", color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoleChip("OFF", selected == EmergencyRole.Off) { onSelect(EmergencyRole.Off) }
            RoleChip("VICTIM", selected == EmergencyRole.Victim) { onSelect(EmergencyRole.Victim) }
            RoleChip("RESCUER", selected == EmergencyRole.Rescuer) { onSelect(EmergencyRole.Rescuer) }
        }
    }
}

@Composable
private fun RoleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) Color(0xFFFA4DF3) else Color(0x6645F0FF)
    val background = if (selected) Color(0x33FA4DF3) else Color(0x220B1020)
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .background(background, RoundedCornerShape(18.dp))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
    ) {
        Text(label, color = if (selected) Color(0xFFFA4DF3) else Color(0xFF45F0FF))
    }
}

@Composable
private fun CalibrationCard(
    calibrationReferenceMeters: String,
    onSetCalibrationReference: (String) -> Unit,
    onSaveCalibration: () -> Unit
) {
    InfoCard(title = "Calibration") {
        Text("Stand at a known distance, run a measurement, then save the offset.", color = Color.White)
        OutlinedTextField(
            value = calibrationReferenceMeters,
            onValueChange = onSetCalibrationReference,
            label = { Text("Known distance (m)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = onSaveCalibration) {
            Text("Save Calibration")
        }
    }
}

@Composable
private fun LocalizationCard(
    anchorMeasurements: List<AnchorMeasurement>,
    victimEstimate: VictimEstimate?,
    onRecordAnchorMeasurement: () -> Unit,
    onClearAnchorMeasurements: () -> Unit,
    onSolveVictimLocation: () -> Unit
) {
    InfoCard(title = "Victim zone localization") {
        Text("Capture measurements from three known rescuer anchor positions, then solve the nearest victim zone.", color = Color.White)
        Button(onClick = onRecordAnchorMeasurement) {
            Text("Record Next Anchor")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSolveVictimLocation, enabled = anchorMeasurements.size >= 3) {
                Text("Solve Zone")
            }
            Button(onClick = onClearAnchorMeasurements) {
                Text("Clear")
            }
        }

        anchorMeasurements.forEach { measurement ->
            Text(
                "${measurement.anchor.label}: ${"%.2f".format(measurement.distanceMeters)} m, confidence ${measurement.confidence}%",
                color = Color.White
            )
        }

        victimEstimate?.let {
            Text(
                "Estimated victim zone: x=${"%.2f".format(it.position.xMeters)} m, y=${"%.2f".format(it.position.yMeters)} m",
                color = Color(0xFF8BE9FD)
            )
            Text(
                "Uncertainty radius: ${"%.2f".format(it.uncertaintyMeters)} m",
                color = Color(0xFFFFC857)
            )
        }
    }
}

@Composable
private fun ToggleCard(selected: RescueMode, onSelect: (RescueMode) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xCC0B1020)),
        modifier = Modifier.border(1.dp, Color(0x6645F0FF), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Rescue Role", color = Color(0xFF45F0FF), fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onSelect(RescueMode.Rescuer) }) {
                    Text(if (selected == RescueMode.Rescuer) "Rescuer Active" else "Rescuer")
                }
                Button(onClick = { onSelect(RescueMode.Victim) }) {
                    Text(if (selected == RescueMode.Victim) "Victim Active" else "Victim")
                }
            }
        }
    }
}

@Composable
private fun OperationalLimitsCard() {
    InfoCard(title = "Operational limits") {
        Text("Acoustic ranging requires air gaps in loose rubble, not solid concrete.", color = Color.White)
        Text("Effective prototype range is roughly 10-30 meters.", color = Color.White)
        Text("Best performance comes below 70 dB ambient noise.", color = Color.White)
        Text("Estimated low-duty battery drain is 2-5% per hour.", color = Color.White)
    }
}

@Composable
private fun VictimCard(state: EchoRescueState, onStart: () -> Unit, onStop: () -> Unit) {
    InfoCard(title = "Victim beacon") {
        Text(
            if (state.victimActive) "BLE peripheral advertising is active and acoustic response is armed on BLE command."
            else "Start victim mode to advertise the rescue service and wait for rescuer arming.",
            color = Color.White
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onStart) { Text("Start Victim Mode") }
            Button(onClick = onStop) { Text("Stop") }
        }
    }
}

@Composable
private fun RescuerCard(state: EchoRescueState, onScanVictims: () -> Unit, onRunMeasurement: () -> Unit) {
    InfoCard(title = "Rescuer console") {
        Text("Connected victim: ${state.connectedVictimName ?: "none"}", color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onScanVictims, enabled = !state.isBusy) { Text("Scan + Connect") }
            Button(onClick = onRunMeasurement, enabled = !state.isBusy && state.connectedVictimName != null) { Text("Measure") }
        }
    }
}

@Composable
private fun GaugeCard(distanceMeters: Double, confidence: Int, noiseFloorDb: Float, signalPower: Float) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xCC0B1020)),
        modifier = Modifier.border(1.dp, Color(0x66FA4DF3), RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Acoustic Range Estimate", color = Color(0xFF45F0FF), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            DistanceGauge(distanceMeters)
            Spacer(Modifier.height(16.dp))
            Text("Confidence: $confidence%", color = Color.White)
            LinearProgressIndicator(
                progress = { confidence / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFA4DF3),
                trackColor = Color.White.copy(alpha = 0.12f)
            )
            Spacer(Modifier.height(12.dp))
            val noiseColor = when {
                noiseFloorDb < 50f -> Color(0xFF31D0AA)
                noiseFloorDb < 70f -> Color(0xFFFFC857)
                else -> Color(0xFFF75C5C)
            }
            Text("Noise floor: ${noiseFloorDb.toInt()} dB", color = noiseColor)
            Text("Signal power: ${"%.3f".format(signalPower)}", color = Color(0xFF45F0FF))
        }
    }
}

@Composable
private fun DistanceGauge(distanceMeters: Double) {
    val normalized = (distanceMeters / 30.0).coerceIn(0.0, 1.0)
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val stroke = 22f
            drawArc(
                color = Color(0x2245F0FF),
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(Color(0xFF45F0FF), Color(0xFFFA4DF3), Color(0xFFFFC857))),
                startAngle = 150f,
                sweepAngle = (240f * normalized).toFloat(),
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            val angle = Math.toRadians(150.0 + 240.0 * normalized)
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = size.minDimension / 2.8f
            val endX = centerX + radius * cos(angle).toFloat()
            val endY = centerY + radius * sin(angle).toFloat()
            drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(centerX, centerY), end = androidx.compose.ui.geometry.Offset(endX, endY), strokeWidth = 8f, cap = StrokeCap.Round)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${"%.1f".format(distanceMeters)}m", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Estimated distance", color = Color(0xFF45F0FF))
        }
    }
}

@Composable
private fun MedicalScreen(state: EchoRescueState, onAskMedical: () -> Unit, onQuestionChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard(title = "Offline medical support") {
            Text(
                if (state.assistantAvailability == AssistantAvailability.Ready) "On-device assistant ready."
                else "Offline guide fallback active. This build keeps a clean abstraction for future on-device AI integration.",
                color = Color.White
            )
            OutlinedTextField(
                value = state.medicalQuestion,
                onValueChange = onQuestionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ask a medical question") }
            )
            Button(onClick = onAskMedical, enabled = !state.isBusy) {
                Text("Get Guidance")
            }
        }

        InfoCard(title = "Answer stream") {
            Text(
                state.medicalAnswer.ifBlank {"Response will stream here."}
                color = Color.White
            )
        }
    }
}

@Composable
private fun SettingsScreen(state: EchoRescueState, onRetryPermissions: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard(title = "Permissions") {
            Text("Bluetooth scan: ${state.permissions.bluetoothScan}", color = Color.White)
            Text("Bluetooth connect: ${state.permissions.bluetoothConnect}", color = Color.White)
            Text("Bluetooth advertise: ${state.permissions.bluetoothAdvertise}", color = Color.White)
            Text("Record audio: ${state.permissions.recordAudio}", color = Color.White)
            Button(onClick = onRetryPermissions) {
                Text("Request Again")
            }
        }

        InfoCard(title = "Field realism") {
            Text("This native build uses BLE service discovery and acoustic round-trip ranging.", color = Color.White)
            Text("Distance is still hardware-dependent and requires per-device calibration before operational use.", color = Color.White)
            Text("Treat medical guidance as supportive reference, not definitive clinical direction.", color = Color.White)
        }

        state.deviceProfile?.let { profile ->
            InfoCard(title = "Device profile") {
                Text("Model: ${profile.modelName}", color = Color.White)
                Text("Recommended transport: ${profile.recommendedTransport}", color = Color.White)
                Text("UWB support detected: ${profile.supportsUwb}", color = Color.White)
                Text("Calibration offset: ${"%.2f".format(profile.calibrationOffsetMeters)} m", color = Color.White)
                Text("Victim reply delay: ${profile.victimReplyDelayMs} ms", color = Color.White)
            }
        }
    }
}

@Composable
private fun PlatformCard(
    modelName: String,
    recommendedTransport: String,
    supportsUwb: Boolean,
    calibrationOffsetMeters: Double,
    victimReplyDelayMs: Long
) {
    InfoCard(title = "Platform strategy") {
        Text("Device: $modelName", color = Color.White)
        Text("Recommended stack: $recommendedTransport", color = Color.White)
        Text("UWB available: $supportsUwb", color = Color.White)
        Text("Calibration offset: ${"%.2f".format(calibrationOffsetMeters)} m", color = Color.White)
        Text("Victim reply delay baseline: ${victimReplyDelayMs} ms", color = Color.White)
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xCC0B1020)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.border(1.dp, Color(0x3345F0FF), RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
                content()
            }
        )
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x66F75C5C)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = Color.White,
            textAlign = TextAlign.Start
        )
    }
}
*/
