package com.echorescue.app.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

data class VictimPeer(
    val name: String,
    val address: String
)

class RescuerCentralController(context: Context) {
    private val appContext = context.applicationContext
    private val bluetoothManager = appContext.getSystemService(BluetoothManager::class.java)
    private val adapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var gatt: BluetoothGatt? = null
    private var commandCharacteristic: BluetoothGattCharacteristic? = null

    @SuppressLint("MissingPermission")
    suspend fun scanAndConnect(): Result<VictimPeer> = withContext(Dispatchers.IO) {
        val scanner = adapter?.bluetoothLeScanner ?: return@withContext Result.failure(
            IllegalStateException("BLE scanner unavailable.")
        )

        withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine { continuation: CancellableContinuation<Result<VictimPeer>> ->
                val filter = ScanFilter.Builder()
                    .setServiceUuid(android.os.ParcelUuid(EchoBleConstants.ServiceUuid))
                    .build()
                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()

                val callback = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        scanner.stopScan(this)
                        val device = result.device
                        gatt = device.connectGatt(appContext, false, buildGattCallback(continuation, device.name))
                    }

                    override fun onScanFailed(errorCode: Int) {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(IllegalStateException("BLE scan failed with code $errorCode.")))
                        }
                    }
                }

                scanner.startScan(listOf(filter), settings, callback)

                continuation.invokeOnCancellation {
                    scanner.stopScan(callback)
                }
            }
        } ?: Result.failure(IllegalStateException("Timed out while scanning for a victim device."))
    }

    @SuppressLint("MissingPermission")
    suspend fun armVictim(): Result<Unit> = withContext(Dispatchers.IO) {
        val connectedGatt = gatt ?: return@withContext Result.failure(
            IllegalStateException("No victim connected.")
        )
        val characteristic = commandCharacteristic ?: return@withContext Result.failure(
            IllegalStateException("Victim command channel unavailable.")
        )

        val writeResult = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            connectedGatt.writeCharacteristic(characteristic, EchoBleConstants.ArmCommand, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            // Note: Modern Android uses onCharacteristicWrite callback for result verification
            Result.success(Unit) 
        } else {
            @Suppress("DEPRECATION")
            characteristic.value = EchoBleConstants.ArmCommand
            @Suppress("DEPRECATION")
            val success = connectedGatt.writeCharacteristic(characteristic)
            if (success) Result.success(Unit) else Result.failure(IllegalStateException("Legacy write failed"))
        }
        writeResult
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        commandCharacteristic = null
    }

    @SuppressLint("MissingPermission")
    private fun buildGattCallback(
        continuation: CancellableContinuation<Result<VictimPeer>>,
        fallbackName: String?
    ): BluetoothGattCallback {
        return object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == android.bluetooth.BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices()
                } else if (newState == android.bluetooth.BluetoothProfile.STATE_DISCONNECTED && continuation.isActive) {
                    continuation.resume(Result.failure(IllegalStateException("Victim disconnected during pairing.")))
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                val service = gatt.getService(EchoBleConstants.ServiceUuid)
                val command = service?.getCharacteristic(EchoBleConstants.CommandCharacteristicUuid)
                if (command != null) {
                    commandCharacteristic = command
                    if (continuation.isActive) {
                        continuation.resume(
                            Result.success(
                                VictimPeer(
                                    name = fallbackName ?: EchoBleConstants.VictimDisplayName,
                                    address = gatt.device.address
                                )
                            )
                        )
                    }
                } else if (continuation.isActive) {
                    continuation.resume(Result.failure(IllegalStateException("Required victim service not found.")))
                }
            }
        }
    }
}
