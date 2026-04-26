package com.echorescue.app.platform

data class DeviceProfile(
    val modelName: String,
    val supportsBlePeripheral: Boolean,
    val supportsUwb: Boolean,
    val recommendedTransport: String,
    val calibrationOffsetMeters: Double,
    val victimReplyDelayMs: Long
)
