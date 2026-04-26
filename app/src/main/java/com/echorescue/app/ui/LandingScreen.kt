package com.echorescue.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    LandingScreen(onStart = {})
}

@Composable
fun LandingScreen(onStart: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_pulse")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val animatedRotationY by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .drawBehind {
                drawGrid()
            }
    ) {
        // Neon Glow Background
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.Center)
                .blur(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF00F3FF).copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Isometric Phone Representation
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .graphicsLayer {
                        rotationX = 45f
                        rotationZ = -20f
                        rotationY = animatedRotationY
                        cameraDistance = 12f * density
                    },
                contentAlignment = Alignment.Center
            ) {
                // Pulse Waves
                PulseWaves(pulseAlpha)

                // Phone Body
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0D1430), Color(0xFF050505))
                            )
                        )
                        .border(2.dp, Color(0xFF00F3FF), RoundedCornerShape(16.dp))
                ) {
                    // Internal UI Scanlines
                    Scanlines()
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Cyberpunk Branding
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ECHØRESCUE",
                    color = Color(0xFF00F3FF),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.drawBehind {
                        // Subtle glitch line
                    }
                )
                Text(
                    text = "PROTOCOL: GUARDIAN_SENTINEL_V1",
                    color = Color(0xFFFA4DF3),
                    fontSize = 12.sp,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Status HUD
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HudElement("HEART_RATE", "105 BPM", Color(0xFFFF0055))
                HudElement("SIGNAL", "BROADCASTING", Color(0xFF9DFF00))
                HudElement("AI_CORE", "OPTIMIZED", Color(0xFF00F3FF))
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Action Button
            Button(
                onClick = onStart,
                modifier = Modifier
                    .width(260.dp)
                    .height(60.dp)
                    .border(1.dp, Color(0xFF00F3FF), RoundedCornerShape(4.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    "INITIATE PROTOCOL",
                    color = Color(0xFF00F3FF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
fun HudElement(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, color = color.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun PulseWaves(alpha: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        for (i in 1..3) {
            drawCircle(
                color = Color(0xFF00F3FF),
                radius = 100f + (i * 40f * alpha),
                center = center,
                style = Stroke(width = 2f),
                alpha = (1.0f - (i * 0.3f)) * alpha
            )
        }
    }
}

@Composable
fun Scanlines() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineCount = 40
        val spacing = size.height / lineCount
        for (i in 0 until lineCount) {
            drawLine(
                color = Color(0xFF00F3FF).copy(alpha = 0.05f),
                start = Offset(0f, i * spacing),
                end = Offset(size.width, i * spacing),
                strokeWidth = 1f
            )
        }
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrid() {
    val gridSpacing = 40.dp.toPx()
    val lineColor = Color(0xFF00F3FF).copy(alpha = 0.05f)
    
    // Vertical lines
    var x = 0f
    while (x < size.width) {
        drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), 1f)
        x += gridSpacing
    }
    
    // Horizontal lines
    var y = 0f
    while (y < size.height) {
        drawLine(lineColor, Offset(0f, y), Offset(size.width, y), 1f)
        y += gridSpacing
    }
}
