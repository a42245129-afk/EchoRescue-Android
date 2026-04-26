package com.echorescue.app.platform

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class DeviceProfileManager(
    private val context: Context,
    private val calibrationStore: CalibrationStore
) {
    fun currentProfile(): DeviceProfile {
        val packageManager = context.packageManager
        val supportsBlePeripheral = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        val supportsUwb = packageManager.hasSystemFeature(PackageManager.FEATURE_UWB)
        val calibrationOffset = calibrationStore.loadCalibrationOffsetMeters()
        val victimReplyDelayMs = calibrationStore.loadVictimReplyDelayMs()
        val recommendedTransport = when {
            supportsUwb -> "UWB + BLE + acoustic fallback"
            supportsBlePeripheral -> "BLE + calibrated acoustic ranging"
            else -> "Acoustic-only fallback; device not recommended"
        }

        return DeviceProfile(
            modelName = "${Build.MANUFACTURER} ${Build.MODEL}",
            supportsBlePeripheral = supportsBlePeripheral,
            supportsUwb = supportsUwb,
            recommendedTransport = recommendedTransport,
            calibrationOffsetMeters = calibrationOffset,
            victimReplyDelayMs = victimReplyDelayMs
        )
    }
}
