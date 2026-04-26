package com.echorescue.app.platform

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

import com.echorescue.app.audio.AcousticResponder
import com.echorescue.app.audio.ChirpDetector
import com.echorescue.app.audio.ChirpEmitter
import com.echorescue.app.ble.VictimPeripheralController

class VictimForegroundService : Service() {
    private var sentinel: EmergencySentinel? = null
    private var responder: AcousticResponder? = null
    private var controller: VictimPeripheralController? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        
        // Initialize real hardware controllers
        responder = AcousticResponder(ChirpDetector(), ChirpEmitter())
        controller = VictimPeripheralController(applicationContext, responder!!)
        
        // Start AI Sentinel for stasis/audio detection
        sentinel = EmergencySentinel(applicationContext) {
            // Callback when emergency is triggered
            controller?.start { status ->
                // Update notification with BLE status
                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(NOTIFICATION_ID, buildNotification(status))
            }
            responder?.startPeriodicPinging()
        }
        sentinel?.start()

        val notification = buildNotification("AI Sentinel is monitoring for emergency triggers.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val status = intent?.getStringExtra(EXTRA_STATUS) ?: "Victim beacon is active."
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, buildNotification(status))
        return START_STICKY
    }

    override fun onDestroy() {
        sentinel?.stop()
        controller?.stop()
        responder?.stopPeriodicPinging()
        super.onDestroy()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "EchoRescue Victim Mode",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps the victim beacon service active during rescue operations."
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(status: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("EchoRescue Victim Mode")
            .setContentText(status)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "echorescue_victim_mode"
        const val NOTIFICATION_ID = 4201
        const val EXTRA_STATUS = "extra_status"
    }
}
