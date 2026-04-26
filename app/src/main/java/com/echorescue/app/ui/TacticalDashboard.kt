package com.echorescue.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    onStopVictimMode: () -> Unit
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
                StatusIndicator(state.status == "ONLINE")
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
fun SurfaceCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
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
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onModeSelected(RescueMode.Victim) }
                .background(if (selectedMode == RescueMode.Victim) MaterialTheme.colorScheme.outlineVariant else Color.Transparent),
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
                .background(if (selectedMode == RescueMode.Rescuer) MaterialTheme.colorScheme.outlineVariant else Color.Transparent),
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
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
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
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Victim Signal List
        Text(
            "DETECTED SIGNALS", 
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SignalItem("ECH-7742", "VICTIM A", "12.4m", true)
            SignalItem("ECH-3391", "VICTIM B", "34.7m", false)
            SignalItem("ECH-9018", "VICTIM C", "67.2m", false)
        }

        // Tactical Radar
        SurfaceCard {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("TACTICAL RADAR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("100M RANGE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            ) {
                TacticalRadar(targets = listOf(
                    RadarTarget(45f, 0.4f, true),
                    RadarTarget(120f, 0.7f, false),
                    RadarTarget(280f, 0.9f, false)
                ))
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
    SurfaceCard {
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
    SurfaceCard {
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
fun SignalItem(id: String, name: String, distance: String, isActive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (isActive) MaterialTheme.colorScheme.outlineVariant else Color.Transparent, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, CircleShape))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(id, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(distance, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun SensorFusionEngineCard(state: EchoRescueState) {
    SurfaceCard {
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
    SurfaceCard {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("AI SENTINEL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Vitals monitoring active. High confidence detection for auditory trauma.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusChip("VITALS", "${state.heartRate} BPM")
            StatusChip("AUDIO", state.detectedAudio)
            StatusChip("ACCEL", state.motionState)
        }
    }
}

@Composable
fun StatusChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (state.victimActive) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.primary
                )
                .border(
                    1.dp,
                    if (state.victimActive) MaterialTheme.colorScheme.outlineVariant else Color.Transparent,
                    RoundedCornerShape(16.dp)
                )
                .clickable { if (state.victimActive) onStop() else onStart() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (state.victimActive) "DEACTIVATE" else "ACTIVATE BEACON",
                style = MaterialTheme.typography.labelSmall,
                color = if (state.victimActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
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
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            .padding(32.dp)
    ) {
        Text("ACOUSTIC SPECTRUM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(64.dp)) {
            val width = size.width
            val height = size.height
            val path = Path()
            path.moveTo(0f, height / 2)
            for (x in 0..width.toInt() step 5) {
                val y = height / 2 + (height / 2) * sin(x.toFloat() * 0.1f)
                path.lineTo(x.toFloat(), y)
            }
            drawPath(path, color = Color.White, style = Stroke(width = 2f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("DUTY CYCLE: 2s ON", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("PINGING...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun TacticalRadar(targets: List<RadarTarget>) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "sweep"
    )

    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension / 2.2f

        // Draw Rings
        for (i in 1..4) {
            drawCircle(
                color = onSurfaceVariant.copy(alpha = 0.2f),
                radius = maxRadius * (i / 4f),
                center = center,
                style = Stroke(width = 1f)
            )
        }

        // Draw Axes
        drawLine(onSurfaceVariant.copy(alpha = 0.2f), Offset(center.x - maxRadius, center.y), Offset(center.x + maxRadius, center.y))
        drawLine(onSurfaceVariant.copy(alpha = 0.2f), Offset(center.x, center.y - maxRadius), Offset(center.x, center.y + maxRadius))

        // Draw Sweep
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(Color.Transparent, primary.copy(alpha = 0.15f)),
                center = center
            ),
            startAngle = sweepAngle - 30f,
            sweepAngle = 30f,
            useCenter = true,
            topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
            size = androidx.compose.ui.geometry.Size(maxRadius * 2, maxRadius * 2)
        )

        // Draw Targets
        targets.forEach { target ->
            val rad = Math.toRadians(target.angle.toDouble())
            val x = center.x + maxRadius * target.distance * cos(rad).toFloat()
            val y = center.y + maxRadius * target.distance * sin(rad).toFloat()
            
            val targetColor = if (target.isActive) primary else onSurfaceVariant

            drawCircle(
                color = targetColor,
                radius = 6f,
                center = Offset(x, y)
            )
            drawCircle(
                color = targetColor.copy(alpha = 0.3f),
                radius = 12f,
                center = Offset(x, y),
                style = Stroke(width = 2f)
            )
        }
    }
}

data class RadarTarget(val angle: Float, val distance: Float, val isActive: Boolean)
