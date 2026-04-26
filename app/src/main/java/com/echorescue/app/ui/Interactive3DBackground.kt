package com.echorescue.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.cos
import kotlin.math.sin

/**
 * UI/UX Designer - Immersive Glassmorphism Background
 * Inspired by Covidpinata.ooo: Interactive 3D depth with frosted layers.
 */
@Composable
fun Interactive3DBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "3d_drift")
    
    val driftX by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift_x"
    )

    val driftY by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift_y"
    )

    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer {
        translationX = driftX
        translationY = driftY
    }) {
        // Dynamic Radial Gradient - "The Core"
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00F3FF).copy(alpha = 0.1f), Color.Transparent),
                center = Offset(size.width / 2, size.height / 2),
                radius = size.minDimension / 1.5f
            ),
            radius = size.minDimension / 1.2f,
            center = Offset(size.width / 2, size.height / 2)
        )

        // Floating Particles (Simulating 3D Depth)
        for (i in 0..20) {
            val t = (System.currentTimeMillis() % 10000) / 10000f
            val x = (size.width * (0.1f + 0.8f * (i / 20f))) + 20f * cos(t * 2 * Math.PI.toFloat() * (i%3+1))
            val y = (size.height * (0.1f + 0.8f * ((i * 7) % 20 / 20f))) + 20f * sin(t * 2 * Math.PI.toFloat() * (i%2+1))
            
            drawCircle(
                color = Color(0xFFFA4DF3).copy(alpha = 0.2f),
                radius = 4f,
                center = Offset(x, y)
            )
        }
    }
}
