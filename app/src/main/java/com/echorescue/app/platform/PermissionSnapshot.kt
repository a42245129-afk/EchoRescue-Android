package com.echorescue.app.platform

data class PermissionSnapshot(
    val bluetoothScan: Boolean = false,
    val bluetoothConnect: Boolean = false,
    val bluetoothAdvertise: Boolean = false,
    val recordAudio: Boolean = false,
    val postNotifications: Boolean = false
) {
    val allGranted: Boolean
        get() = bluetoothScan && bluetoothConnect && bluetoothAdvertise && recordAudio && postNotifications
}
