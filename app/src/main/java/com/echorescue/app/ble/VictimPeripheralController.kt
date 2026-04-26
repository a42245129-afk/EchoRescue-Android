package com.echorescue.app.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import com.echorescue.app.audio.AcousticResponder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class VictimPeripheralController(
    context: Context,
    private val responder: AcousticResponder
) {
    private val appContext = context.applicationContext
    private val bluetoothManager = appContext.getSystemService(BluetoothManager::class.java)
    private val adapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var gattServer: BluetoothGattServer? = null
    private var isAdvertising = false
    private var advertiseCallback: AdvertiseCallback? = null

    @SuppressLint("MissingPermission")
    fun start(onStatus: (String) -> Unit) {
        val bluetoothAdapter = adapter ?: run {
            onStatus("Bluetooth adapter unavailable on this device.")
            return
        }
        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser ?: run {
            onStatus("BLE advertising is unavailable on this device.")
            return
        }

        if (gattServer == null) {
            gattServer = bluetoothManager?.openGattServer(appContext, buildServerCallback(onStatus))?.apply {
                addService(buildService())
            }
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(android.os.ParcelUuid(EchoBleConstants.ServiceUuid))
            .build()

        val callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                isAdvertising = true
                onStatus("Victim beacon live. BLE service advertising for rescuer pairing.")
            }

            override fun onStartFailure(errorCode: Int) {
                onStatus("BLE advertising failed with code $errorCode.")
            }
        }

        advertiseCallback = callback
        advertiser.startAdvertising(settings, data, callback)
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        val advertiser = adapter?.bluetoothLeAdvertiser
        advertiseCallback?.let { advertiser?.stopAdvertising(it) }
        advertiseCallback = null
        gattServer?.close()
        gattServer = null
        isAdvertising = false
        responder.disarm()
    }

    fun release() {
        stop()
        scope.cancel()
    }

    private fun buildService(): BluetoothGattService {
        val service = BluetoothGattService(
            EchoBleConstants.ServiceUuid,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        val commandCharacteristic = BluetoothGattCharacteristic(
            EchoBleConstants.CommandCharacteristicUuid,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        val statusCharacteristic = BluetoothGattCharacteristic(
            EchoBleConstants.StatusCharacteristicUuid,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        ).apply {
            value = "READY".toByteArray()
        }
        service.addCharacteristic(commandCharacteristic)
        service.addCharacteristic(statusCharacteristic)
        return service
    }

    private fun buildServerCallback(onStatus: (String) -> Unit): BluetoothGattServerCallback {
        return object : BluetoothGattServerCallback() {
            @SuppressLint("MissingPermission")
            override fun onCharacteristicWriteRequest(
                device: android.bluetooth.BluetoothDevice?,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic?,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
            ) {
                val gatt = gattServer ?: return
                if (characteristic?.uuid == EchoBleConstants.CommandCharacteristicUuid &&
                    value?.contentEquals(EchoBleConstants.ArmCommand) == true
                ) {
                    scope.launch {
                        responder.armForSingleResponse { status ->
                            onStatus(status)
                        }
                    }
                    if (responseNeeded) {
                        gatt.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, EchoBleConstants.ArmCommand)
                    }
                    onStatus("Victim armed. Listening for inbound acoustic challenge.")
                } else if (responseNeeded) {
                    gatt.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                }
            }
        }
    }
}
