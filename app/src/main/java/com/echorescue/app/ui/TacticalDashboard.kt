package com.echorescue.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TacticalDashboard(
    state: EchoRescueState,
    onSelectRescueMode: (RescueMode) -> Unit,
    onStartVictimMode: () -> Unit,
    onStopVictimMode: () -> Unit,
    onToggleLightTheme: () -> Unit
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Top Branding
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "ECHØRESCUE", 
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "GUARDIAN SENTINEL", 
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThemeToggle(
                        useLightTheme = state.useLightTheme,
                        onToggle = onToggleLightTheme
                    )
                    StatusIndicator(state.status == "ONLINE")
                }
            }

            // Elegant Mode Toggle
            SurfaceToggle(
                selectedMode = state.rescueMode,
                onModeSelected = onSelectRescueMode
            )

            if (state.rescueMode == RescueMode.Rescuer) {
                RescuerTacticalView(state)
            } else {
                VictimBeaconView(state, onStartVictimMode, onStopVictimMode)
            }
        }
    }
}

@Composable
fun ThemeToggle(useLightTheme: Boolean, onToggle: () -> Unit) {
    val tint = if (useLightTheme) Color(0xFFFFC857) else Color(0xFF00F3FF)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tint.copy(alpha = 0.12f))
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(tint, CircleShape)
        )
        Text(
            text = if (useLightTheme) "LIGHT" else "DARK",
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
    }
}

@Composable
fun SurfaceCard(modifier: Modifier = Modifier, tintColor: Color? = null, content: @Composable ColumnScope.() -> Unit) {
    val bgColor = tintColor?.copy(alpha = 0.12f) ?: MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    val topWash = tintColor?.copy(alpha = 0.06f) ?: Color.White.copy(alpha = 0.02f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(topWash, bgColor)
                )
            )
            .padding(32.dp),
        content = content
    )
}

@Composable
fun SurfaceToggle(selectedMode: RescueMode, onModeSelected: (RescueMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onModeSelected(RescueMode.Victim) }
                .background(if (selectedMode == RescueMode.Victim) Color(0xFFFA4DF3).copy(alpha = 0.18f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "VICTIM", 
                style = MaterialTheme.typography.labelSmall,
                color = if (selectedMode == RescueMode.Victim) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onModeSelected(RescueMode.Rescuer) }
                .background(if (selectedMode == RescueMode.Rescuer) Color(0xFF00F3FF).copy(alpha = 0.16f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "RESCUER", 
                style = MaterialTheme.typography.labelSmall,
                color = if (selectedMode == RescueMode.Rescuer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatusIndicator(isOnline: Boolean) {
    val color = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(
            if (isOnline) "ONLINE" else "OFFLINE", 
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun RescuerTacticalView(state: EchoRescueState) {
    val activeDistance = if (state.distanceMeters > 0) (state.distanceMeters / 100.0).coerceIn(0.12, 0.95).toFloat() else 0.4f
    val radarTargets = if (state.connectedVictimName != null || state.distanceMeters > 0.0) {
        listOf(
            RadarTarget(45f, activeDistance, true, Color(0xFFFF0055)),
            RadarTarget(120f, 0.7f, false, Color(0xFFFFC857)),
            RadarTarget(280f, 0.9f, false, Color(0xFF9DFF00))
        )
    } else {
        emptyList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Victim Signal List
        Text(
            "DETECTED SIGNALS", 
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SignalItem("ECH-7742", "VICTIM A", "12.4m", true, Color(0xFFFF0055))
            SignalItem("ECH-3391", "VICTIM B", "34.7m", false, Color(0xFFFFC857))
            SignalItem("ECH-9018", "VICTIM C", "67.2m", false, Color(0xFF9DFF00))
        }

        // Proximity Tracker
        SurfaceCard(tintColor = Color(0xFF00F3FF)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("PROXIMITY TRACKER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("100M RANGE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00F3FF).copy(alpha = 0.10f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                ProximityVisualizer(targets = radarTargets)

                if (radarTargets.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AWAITING SIGNAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Scan for a victim to activate the proximity tracker.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Active Target Detail
        ActiveTargetCard(state)
        
        // Precision Stats
        SensorFusionEngineCard(state)
        
        // Local AI EMS Agent Card
        EmsAiAgentCard(state)
        
        // Detection Mesh Status
        DetectionMeshCard(state)
    }
}

@Composable
fun ActiveTargetCard(state: EchoRescueState) {
    SurfaceCard(tintColor = Color(0xFFFF0055)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                Spacer(Modifier.width(12.dp))
                Text("ACTIVE TARGET", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Text("ECH-7742", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TargetStat("DISTANCE", "12.4m")
            TargetStat("BEARING", "NNE 22°")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text("START NAVIGATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun TargetStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun DetectionMeshCard(state: EchoRescueState) {
    SurfaceCard(tintColor = Color(0xFF00F3FF)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("DETECTION MESH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("7 ACTIVE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MeshStatusItem("BLE 5.3", "-62 dBm", "RSSI + AoA ACTIVE")
            MeshStatusItem("Wi-Fi RTT", "2.1 ns", "ROUND-TRIP TIME")
            MeshStatusItem("UWB", "±0.1m", "IEEE 802.15.4z")
            MeshStatusItem("ACOUSTIC", "18 kHz", "ULTRASONIC BEACON")
        }
    }
}

@Composable
fun MeshStatusItem(label: String, value: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun SignalItem(id: String, name: String, distance: String, isActive: Boolean, tintColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tintColor.copy(alpha = 0.08f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(tintColor, CircleShape))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(id, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(distance, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SensorFusionEngineCard(state: EchoRescueState) {
    SurfaceCard(tintColor = Color(0xFFFFC857)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("SENSOR FUSION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("LOCKED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(verticalAlignment = Alignment.Bottom) {
            Text("0.7", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
            Text("m", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text("CONFIDENCE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text("97%", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.97f)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun EmsAiAgentCard(state: EchoRescueState) {
    val tintColor = Color(0xFF9DFF00)
    SurfaceCard(tintColor = tintColor) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("AI SENTINEL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(Modifier.size(6.dp).background(tintColor, CircleShape))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Vitals monitoring active. High confidence detection for auditory trauma.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusChip("VITALS", "${state.heartRate} BPM", Color(0xFFFF0055))
            StatusChip("AUDIO", state.detectedAudio, Color(0xFF00F3FF))
            StatusChip("ACCEL", state.motionState, Color(0xFFFFC857))
        }
    }
}

@Composable
fun StatusChip(label: String, value: String, tintColor: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(tintColor.copy(alpha = 0.10f))
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = tintColor)
    }
}

@Composable
fun VictimBeaconView(state: EchoRescueState, onStart: () -> Unit, onStop: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        
        // Beacon Status
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("BEACON CODE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Text("ECH-7742", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(64.dp))
        AcousticFrequencyGraph()
        Spacer(modifier = Modifier.height(64.dp))
        
        // Active SOS Beacon Button
        val beaconColor = Color(0xFFFA4DF3)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (state.victimActive) beaconColor.copy(alpha = 0.2f)
                    else beaconColor.copy(alpha = 0.08f)
                )
                .clickable { if (state.victimActive) onStop() else onStart() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (state.victimActive) "DEACTIVATE" else "ACTIVATE BEACON",
                style = MaterialTheme.typography.labelSmall,
                color = beaconColor
            )
        }
    }
}

@Composable
fun AcousticFrequencyGraph() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF00F3FF).copy(alpha = 0.08f))
            .padding(32.dp)
    ) {
        Text("ACOUSTIC SPECTRUM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        val waveColor = MaterialTheme.colorScheme.primary
        Canvas(modifier = Modifier.fillMaxWidth().height(64.dp)) {
            val width = size.width
            val height = size.height
            val path = Path()
            path.moveTo(0f, height / 2)
            for (x in 0..width.toInt() step 5) {
                val y = height / 2 + (height / 2) * sin(x.toFloat() * 0.1f)
                path.lineTo(x.toFloat(), y)
            }
            drawPath(path, color = waveColor, style = Stroke(width = 2f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("DUTY CYCLE: 2s ON", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("PINGING...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ProximityVisualizer(targets: List<RadarTarget>) {
    val infiniteTransition = rememberInfiniteTransition(label = "ping")
    val pingProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing)
        ),
        label = "ping_progress"
    )

    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension / 2.2f

        // Draw distance rings with dashed lines
        for (i in 1..4) {
            drawCircle(
                color = onSurfaceVariant.copy(alpha = 0.1f),
                radius = maxRadius * (i / 4f),
                center = center,
                style = Stroke(
                    width = 2f, 
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            )
        }

        // Draw crosshairs instead of axes
        val crosshairLength = 10f
        drawLine(onSurfaceVariant.copy(alpha = 0.3f), Offset(center.x - crosshairLength, center.y), Offset(center.x + crosshairLength, center.y), strokeWidth = 2f)
        drawLine(onSurfaceVariant.copy(alpha = 0.3f), Offset(center.x, center.y - crosshairLength), Offset(center.x, center.y + crosshairLength), strokeWidth = 2f)

        // Draw expanding ping (acoustic wave)
        drawCircle(
            color = primary.copy(alpha = (1f - pingProgress) * 0.4f),
            radius = maxRadius * pingProgress,
            center = center,
            style = Stroke(width = 4f)
        )
        drawCircle(
            color = primary.copy(alpha = (1f - pingProgress) * 0.1f),
            radius = maxRadius * pingProgress,
            center = center
        )

        // Draw Targets
        targets.forEach { target ->
            val rad = Math.toRadians(target.angle.toDouble())
            val x = center.x + maxRadius * target.distance * cos(rad).toFloat()
            val y = center.y + maxRadius * target.distance * sin(rad).toFloat()
            
            val targetColor = if (target.isActive) target.tintColor else target.tintColor.copy(alpha = 0.6f)

            // React to the ping passing by
            val distanceDiff = kotlin.math.abs(pingProgress - target.distance)
            val isPingNear = distanceDiff < 0.1f
            val pulseAlpha = if (isPingNear) 0.8f * (1f - distanceDiff * 10f) else 0.2f
            val pulseRadius = if (isPingNear) 24f - (distanceDiff * 100f) else 12f

            drawCircle(
                color = targetColor.copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = Offset(x, y)
            )
            
            drawCircle(
                color = targetColor,
                radius = 8f,
                center = Offset(x, y)
            )
            
            if (target.isActive) {
                 drawCircle(
                    color = targetColor.copy(alpha = 0.4f),
                    radius = 16f,
                    center = Offset(x, y),
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

data class RadarTarget(val angle: Float, val distance: Float, val isActive: Boolean, val tintColor: Color)
