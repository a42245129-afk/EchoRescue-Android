package com.echorescue.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EchoRescueColors = darkColorScheme(
    primary = RescueRed,
    secondary = RescueIce,
    tertiary = RescueGold,
    background = RescueBlue,
    surface = RescueSurface
)

@Composable
fun EchoRescueTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EchoRescueColors,
        content = content
    )
}
