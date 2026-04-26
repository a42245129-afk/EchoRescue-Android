package com.echorescue.app.platform

import android.content.Context

class CalibrationStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("echorescue_calibration", Context.MODE_PRIVATE)

    fun loadCalibrationOffsetMeters(): Double {
        return prefs.getFloat("calibration_offset_meters", 0f).toDouble()
    }

    fun saveCalibrationOffsetMeters(offsetMeters: Double) {
        prefs.edit().putFloat("calibration_offset_meters", offsetMeters.toFloat()).apply()
    }

    fun loadVictimReplyDelayMs(defaultValue: Long = 35L): Long {
        return prefs.getLong("victim_reply_delay_ms", defaultValue)
    }

    fun saveVictimReplyDelayMs(delayMs: Long) {
        prefs.edit().putLong("victim_reply_delay_ms", delayMs).apply()
    }

    fun loadEmergencyRole(): String {
        return prefs.getString("emergency_role", "OFF") ?: "OFF"
    }

    fun saveEmergencyRole(role: String) {
        prefs.edit().putString("emergency_role", role).apply()
    }
}
