package com.echorescue.app.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootRescueReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        val store = CalibrationStore(context)
        if (store.loadEmergencyRole() == "VICTIM") {
            val serviceIntent = Intent(context, VictimForegroundService::class.java).apply {
                putExtra(
                    VictimForegroundService.EXTRA_STATUS,
                    "Emergency auto-arm is enabled. Open EchoRescue to activate victim beacon."
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
