package com.szklimek.auto

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.szklimek.base.Log

class PreferenceManager(context: Context) {
    init {
        Log.d("Init $this")
    }

    private val preferences = context.getSharedPreferences("auto-prefs", Context.MODE_PRIVATE)
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    fun storePairedDevice(device: BluetoothDevice) {
        Log.d("Device: ${device.name}{${device.address}}")
        preferences.edit().putString(KEY_BT_PAIRED_DEVICE_ADDRESS, device.address).apply()
    }

    fun getPairedDevice(): BluetoothDevice? {
        Log.d("")
        val storedAddress = preferences.getString(KEY_BT_PAIRED_DEVICE_ADDRESS, "")
        return bluetoothAdapter.bondedDevices.find { it.address == storedAddress }
    }
}

const val KEY_BT_PAIRED_DEVICE_ADDRESS = "device"
