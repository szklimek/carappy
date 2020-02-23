package com.szklimek.auto.obd.elm327

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.github.pires.obd.commands.protocol.ObdRawCommand
import com.szklimek.base.BaseActivity
import com.szklimek.auto.PreferenceManager
import com.szklimek.auto.R
import com.szklimek.auto.obd.ConnectionState
import com.szklimek.auto.obd.ObdService
import com.szklimek.auto.obd.ObdState
import com.szklimek.base.Log
import kotlinx.android.synthetic.main.activity_connection_elm327.*
import kotlinx.android.synthetic.main.item_log.view.*
import org.koin.android.ext.android.inject

class Elm327BluetoothConnectionActivity : BaseActivity() {

    private val preferenceManager: PreferenceManager by inject()
    private val obdService: ObdService by inject()
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val logAdapter = LogAdapter()
    private val logsObserver = Observer<List<String>> { logAdapter.submitLogs(it) }

    private val connectionStateObserver = Observer<ConnectionState> { updateConnectionState(it) }
    private val obdStateObserver = Observer<ObdState> { updateObdState(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection_elm327)
        initViews()
        setSupportActionBar(toolbar)
        title = "Elm327 Bluetooth Adapter setup"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        obdService.connectionState.observe(this, connectionStateObserver)
        obdService.obdState.observe(this, obdStateObserver)
        obdService.logs.observe(this, logsObserver)
        updatePairedDevice(preferenceManager.getPairedDevice())
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initViews() {
        button_select_device.setOnClickListener { showPairedDevicesList() }
        button_connect.setOnClickListener { obdService.connect() }
        button_init_obd.setOnClickListener { obdService.initOBD() }
        button_close_obd.setOnClickListener { obdService.closeOBD() }
        button_obd_send_custom_command.setOnClickListener {
            val command = obd_custom_command_edit_text.text.toString()
            obdService.runAsyncCommand(ObdRawCommand(command)) {}
        }
        logs_recycler_view.adapter = logAdapter
        logs_recycler_view.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun updatePairedDevice(bluetoothDevice: BluetoothDevice?) {
        if (bluetoothDevice == null) {
            Log.d("No paired device")
            button_connect.isEnabled = false
            setObdProtocolViews(false)
            paired_device.text = "No selected device"
            return
        }
        val deviceIdentifier = "${bluetoothDevice.name}[${bluetoothDevice.address}]"

        Log.d("Update paired device: $deviceIdentifier")
        button_connect.isEnabled = true
        paired_device.text = deviceIdentifier
    }

    private fun updateConnectionState(connectionState: ConnectionState) {
        when (connectionState) {
            is ConnectionState.Connected -> {
                connection_state.text = "Connected"
                button_connect.isEnabled = false
                setObdProtocolViews(true)
            }
            is ConnectionState.Connecting -> {
                connection_state.text = "Connecting"
                button_connect.isEnabled = false
                setObdProtocolViews(false)
            }
            is ConnectionState.NotConnected -> {
                connection_state.text = "Not connected"
                button_connect.isEnabled = true
                setObdProtocolViews(false)
            }
        }
    }

    private fun updateObdState(obdState: ObdState) {
        val result = when (obdState) {
            is ObdState.ObdReady -> "OBD ready"
            is ObdState.ObdNotReady -> "OBD not ready"
            is ObdState.ObdLoading -> "OBD loading"
        }
        obd_state.text = result
    }

    private fun setObdProtocolViews(isEnabled: Boolean) {
        button_init_obd.isEnabled = isEnabled
        button_close_obd.isEnabled = isEnabled
        obd_custom_command_edit_text.isEnabled = isEnabled
        button_obd_send_custom_command.isEnabled = isEnabled
    }

    private fun showPairedDevicesList() {
        val devices = bluetoothAdapter.bondedDevices.toList()
        Log.d("Paired devices: ${devices.map { "[${it.name}; ${it.address}; ${it.type}; ${it.bondState}]" }}")
        AlertDialog.Builder(this)
            .setTitle("Select OBD BT device")
            .setItems(devices.map { it.name }.toTypedArray()) { _, selectedPosition ->
                val device = devices[selectedPosition]

                preferenceManager.storePairedDevice(device)
                updatePairedDevice(device)
            }
            .setPositiveButton("Done") { _, _ -> }
            .show()
    }
}

class LogAdapter : RecyclerView.Adapter<LogViewHolder>() {
    private var logs = listOf<String>()

    fun submitLogs(logs: List<String>) {
        this.logs = logs
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return LogViewHolder(view)
    }

    override fun getItemCount() = logs.size

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bindLog(logs[position])
    }
}

class LogViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun bindLog(log: String) {
        view.log_text.text = log
    }
}
