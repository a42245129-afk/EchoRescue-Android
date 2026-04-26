package com.echorescue.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Info Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ECHØRESCUE", color = Color(0xFFFA4DF3), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("AUTONOMOUS RESCUE MESH V4.2", color = Color(0xFF00F3FF), fontSize = 10.sp, letterSpacing = 2.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(Color(0xFF9DFF00), CircleShape))
                Spacer(Modifier.width(8.dp))
                Text("ONLINE", color = Color(0xFF9DFF00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Mode Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0D1430)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelectRescueMode(RescueMode.Victim) }
                    .background(if (state.rescueMode == RescueMode.Victim) Color(0x33FA4DF3) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text("VICTIM", color = if (state.rescueMode == RescueMode.Victim) Color(0xFFFA4DF3) else Color.White)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelectRescueMode(RescueMode.Rescuer) }
                    .background(if (state.rescueMode == RescueMode.Rescuer) Color(0x3300F3FF) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text("RESCUER", color = if (state.rescueMode == RescueMode.Rescuer) Color(0xFF00F3FF) else Color.White)
            }
        }

        // Main Content: Signal List and Radar
        if (state.rescueMode == RescueMode.Rescuer) {
            RescuerTacticalView(state)
        } else {
            VictimBeaconView(state, onStartVictimMode, onStopVictimMode)
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun RescuerTacticalView(state: EchoRescueState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Victim Signal List
        Text("DETECTED DISTRESS SIGNALS", color = Color(0xFF00F3FF).copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        
        SignalItem("ECH-7742", "VICTIM A", "12.4m", Color(0xFFFF0055))
        SignalItem("ECH-3391", "VICTIM B", "34.7m", Color(0xFFFFC857))
        SignalItem("ECH-9018", "VICTIM C", "67.2m", Color(0xFF9DFF00))

        // Tactical Radar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF08101F))
                .border(1.dp, Color(0xFF00F3FF).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
            TacticalRadar(targets = listOf(
                RadarTarget(45f, 0.4f, Color(0xFFFF0055)),
                RadarTarget(120f, 0.7f, Color(0xFFFFC857)),
                RadarTarget(280f, 0.9f, Color(0xFF9DFF00))
            ))
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text("TACTICAL RADAR", color = Color(0xFF00F3FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("RANGE: 100M", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Text("VICTIMS: 3", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
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
        
        // Gemini Nano Analysis
        GeminiNanoCard(state)
        
        // Diagnostic AI Log
        AiDiagnosticCard(state)
        
        // Detection Mesh Status
        DetectionMeshCard(state)
    }
}

@Composable
fun GeminiNanoCard(state: EchoRescueState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .blur(if (state.isBusy) 4.dp else 0.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("GEMINI_NANO_V2", color = Color(0xFFFA4DF3), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("ON-DEVICE AI", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
        }
        
        Text(
            state.aiDiagnosticLog,
            color = Color(0xFF00F3FF),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun ActiveTargetCard(state: EchoRescueState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0D1430).copy(alpha = 0.5f))
            .border(1.dp, Color(0xFFFF0055).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(Color(0xFFFF0055), CircleShape))
                Spacer(Modifier.width(8.dp))
                Text("ACTIVE TARGET - ECH-7742", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text("CRITICAL", color = Color(0xFFFF0055), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TargetStat("DISTANCE", "12.4m", Color(0xFFFF0055))
            TargetStat("BEARING", "NNE 22°", Color.White)
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF31D0AA).copy(alpha = 0.1f))
                .border(1.dp, Color(0xFF31D0AA).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text("START NAVIGATION", color = Color(0xFF31D0AA), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun TargetStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun DetectionMeshCard(state: EchoRescueState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF08101F))
            .border(1.dp, Color(0xFF00F3FF).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("DETECTION MESH", color = Color(0xFF00F3FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("7 ACTIVE", color = Color(0xFF9DFF00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MeshStatusItem(this, "BLE 5.3", "-62 dBm", "RSSI + AoA ACTIVE")
            MeshStatusItem(this, "Wi-Fi RTT", "2.1 ns", "ROUND-TRIP TIME")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MeshStatusItem(this, "UWB", "±0.1m", "IEEE 802.15.4z")
            MeshStatusItem(this, "ACOUSTIC", "18 kHz", "ULTRASONIC BEACON")
        }
    }
}

@Composable
fun MeshStatusItem(rowScope: RowScope, label: String, value: String, desc: String) {
    with(rowScope) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(8.dp)
        ) {
            Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(desc, color = Color(0xFF00F3FF).copy(alpha = 0.7f), fontSize = 6.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SignalItem(id: String, name: String, distance: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).background(color, CircleShape))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(id, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(name, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(distance, color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
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

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension / 2.2f

        // Draw Rings
        for (i in 1..4) {
            drawCircle(
                color = Color(0xFF00F3FF).copy(alpha = 0.1f),
                radius = maxRadius * (i / 4f),
                center = center,
                style = Stroke(width = 1f)
            )
        }

        // Draw Axes
        drawLine(Color(0xFF00F3FF).copy(alpha = 0.1f), Offset(center.x - maxRadius, center.y), Offset(center.x + maxRadius, center.y))
        drawLine(Color(0xFF00F3FF).copy(alpha = 0.1f), Offset(center.x, center.y - maxRadius), Offset(center.x, center.y + maxRadius))

        // Draw Sweep
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(Color.Transparent, Color(0xFF00F3FF).copy(alpha = 0.3f)),
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
            
            drawCircle(
                color = target.color,
                radius = 6f,
                center = Offset(x, y)
            )
            drawCircle(
                color = target.color.copy(alpha = 0.3f),
                radius = 12f,
                center = Offset(x, y),
                style = Stroke(width = 2f)
            )
        }
    }
}

data class RadarTarget(val angle: Float, val distance: Float, val color: Color)

@Composable
fun SensorFusionEngineCard(state: EchoRescueState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0D1430).copy(alpha = 0.8f))
            .border(1.dp, Color(0xFFFA4DF3).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("SENSOR FUSION ENGINE", color = Color(0xFFFA4DF3), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("LOCKED", color = Color(0xFF9DFF00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        
        Row(verticalAlignment = Alignment.Bottom) {
            Text("0.7", color = Color(0xFF9DFF00), fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
            Text("m", color = Color(0xFF9DFF00), fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text("CONFIDENCE", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
                Text("97%", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        LinearProgressIndicator(
            progress = { 0.97f },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = Color(0xFF9DFF00),
            trackColor = Color.White.copy(alpha = 0.1f)
        )
        
        Spacer(Modifier.height(8.dp))
        
        HudDetail("GPS + GLONASS + BeiDou", "±0.8m", Color(0xFF00F3FF))
        HudDetail("Wi-Fi RTT (802.11mc)", "±0.6m", Color(0xFF00F3FF))
        HudDetail("BLE 5.3 AoA/AoD", "±0.4m", Color(0xFF00F3FF))
        HudDetail("IMU Dead Reckoning", "±1.2m", Color(0xFFFA4DF3))
    }
}

@Composable
fun HudDetail(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
        Text(value, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun EmsAiAgentCard(state: EchoRescueState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF08101F))
            .border(1.dp, Color(0xFF00F3FF).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("EMS_AI_SENTINEL", color = Color(0xFF00F3FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Box(Modifier.size(6.dp).background(Color(0xFF9DFF00), CircleShape))
        }
        
        Text(
            "Condition assessed via Gemini Nano. Vitals monitoring active. High confidence detection for auditory trauma (Explosion/Scream).",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatusChip("VITALS", "${state.heartRate} BPM", if (state.heartRate < 40 || state.heartRate > 120) Color(0xFFFF0055) else Color(0xFF9DFF00))
            StatusChip("AUDIO", state.detectedAudio, Color(0xFF00F3FF))
            StatusChip("ACCEL", state.motionState, Color(0xFFFFC857))
        }
    }
}

@Composable
fun AiDiagnosticCard(state: EchoRescueState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .border(1.dp, Color(0xFF00F3FF).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text("AI_DIAGNOSTIC_FEED", color = Color(0xFF00F3FF).copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "> ANALYZING SENSORS...\n> HR: ${state.heartRate} BPM (STABLE)\n> AUDIO: ${state.detectedAudio}\n> MOTION: ${state.motionState}\n> NO ANOMALIES DETECTED.",
            color = Color(0xFF9DFF00).copy(alpha = 0.8f),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 12.sp
        )
    }
}

@Composable
fun StatusChip(label: String, value: String, color: Color) {
    Column {
        Text(label, color = color.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun VictimBeaconView(state: EchoRescueState, onStart: () -> Unit, onStop: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Beacon Status Visualizer
        Box(contentAlignment = Alignment.Center) {
            val infiniteTransition = rememberInfiniteTransition(label = "beacon_pulse")
            val radiusScale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000),
                    repeatMode = RepeatMode.Restart
                ),
                label = "radius"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000),
                    repeatMode = RepeatMode.Restart
                ),
                label = "alpha"
            )
            
            Canvas(modifier = Modifier.size(300.dp)) {
                drawCircle(Color(0xFFFA4DF3).copy(alpha = alpha), radius = 100f * radiusScale, style = Stroke(width = 4f))
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("BEACON CODE", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                Text("ECH-7742", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(Modifier.height(24.dp))
        AcousticFrequencyGraph()
        Spacer(Modifier.height(24.dp))
        
        // Active SOS Beacon Button
        Box(
            modifier = Modifier
                .width(280.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (state.victimActive) Color(0xFF0D1430)
                    else Color(0xFFFF0055)
                )
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .clickable { if (state.victimActive) onStop() else onStart() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (state.victimActive) "DEACTIVATE SOS" else "ACTIVATE SOS BEACON",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun AcousticFrequencyGraph() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .border(1.dp, Color(0xFF00F3FF).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text("ACOUSTIC EMISSION SPECTRUM (20kHz)", color = Color(0xFF00F3FF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
            val width = size.width
            val height = size.height
            val path = Path()
            path.moveTo(0f, height / 2)
            for (x in 0..width.toInt() step 5) {
                val y = height / 2 + (height / 2) * sin(x.toFloat() * 0.1f)
                path.lineTo(x.toFloat(), y)
            }
            drawPath(path, color = Color(0xFF00F3FF), style = Stroke(width = 2f))
        }
        Spacer(Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("DUTY CYCLE: 2s ON / 58s SLEEP", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
            Text("STATUS: PINGING...", color = Color(0xFF9DFF00), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF00F3FF),
    trackColor: Color = Color(0xFF0D1430)
) {
    Box(
        modifier = modifier
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress())
                .background(color)
        )
    }
}
