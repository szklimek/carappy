package com.szklimek.auto

import android.bluetooth.BluetoothAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val dataManager: DataManager by inject()
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initButtons()
        updateDeviceStatus()
    }

    private fun initButtons() {
        button_select_device.setOnClickListener { showPairedDevicesList() }

    }

    private fun updateDeviceStatus() {
        bt_selected_device.text =
            dataManager.getPairedDevice()?.run { "$name: [$address]" } ?: "No selected device"
    }

    private fun showPairedDevicesList() {
        val devices = bluetoothAdapter.bondedDevices.toList()
        Log.d("Paired devices: ${devices.map { "[${it.name}; ${it.address}; ${it.type}; ${it.bondState}]" }}")
        AlertDialog.Builder(this)
            .setTitle("Select OBD BT device")
            .setItems(devices.map { it.name }.toTypedArray()) { _, selectedPosition ->
                dataManager.storePairedDevice(devices[selectedPosition])
                updateDeviceStatus()
            }
            .setPositiveButton("Done") { _, _ -> }
            .show()
    }

}
