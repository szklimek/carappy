package com.szklimek.auto

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Observer
import com.github.pires.obd.commands.ObdCommand
import com.github.pires.obd.commands.engine.RPMCommand
import com.szklimek.auto.obd.elm327.Elm327BluetoothConnectionActivity
import com.szklimek.auto.obd.ObdService
import com.szklimek.auto.obd.ConnectionState
import com.szklimek.auto.obd.ObdServiceState
import com.szklimek.auto.obd.ObdState
import com.szklimek.base.BaseActivity
import com.szklimek.base.Log
import com.szklimek.base.extensions.startWithTransition
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class MainActivity : BaseActivity() {

    private val obdService: ObdService by inject()
    private val connectionStateObserver = Observer<ConnectionState> { updateConnectionState(it) }
    private val obdStateObserver = Observer<ObdState> { updateObdState(it) }
    private val serviceStateObserver = Observer<ObdServiceState> { updateObdServiceState(it) }

    private val obdFailureHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initButtons()
        obdService.connectionState.observe(this, connectionStateObserver)
        obdService.obdState.observe(this, obdStateObserver)
        obdService.serviceState.observe(this, serviceStateObserver)
    }

    private fun initButtons() {
        button_setup.setOnClickListener {
            Log.d("Open device setup")
            startWithTransition(Intent(this, Elm327BluetoothConnectionActivity::class.java))
        }
    }

    private fun startSession() {
        obdService.startSession()
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                if (obdService.serviceState.value is ObdServiceState.Started &&
                    obdService.connectionState.value == ConnectionState.Connected &&
                    obdService.obdState.value == ObdState.ObdReady
                ) {
                    loadEcuData()
                }
                if (obdService.serviceState.value !is ObdServiceState.Started) {
                    return@launch
                }
            }
        }
    }

    private fun loadEcuData() {
        Log.d("")
        listOf(
            RPMCommand()
        ).forEach { command ->

            // TODO Handle OBD connection problem during session
            // Set command result timeout (if no response received)
//            obdFailureHandler.postDelayed(
//                { obdService.resetConnection() }, 3000
//            )
            obdService.runCommand(command).apply {
                // TODO Handle OBD connection problem during session
//                obdFailureHandler.removeCallbacksAndMessages(null)
                when {
                    isFailure -> {
                        Log.e("Unable to retrieve OBD data for command: $command")
                    }
                    isSuccess -> {
                        runOnUiThread {
                            updateEcuDataViews(command, getOrDefault(""))
                        }
                    }
                }
            }
        }
    }

    private fun updateEcuDataViews(command: ObdCommand, result: String) {
        if (result.isEmpty()) {
            Log.d("${command.name} returned empty result. Aborting")
            return
        }
        when (command) {
            is RPMCommand -> {
                rpm_status.text = result
                runCatching {
                    rpm_gauge.moveToValue(command.rpm.toFloat().div(100))
                }
            }
        }
    }

    private fun updateConnectionState(connectionState: ConnectionState) {
        connection_state.text = when (connectionState) {
            is ConnectionState.Connected -> "Connected"
            is ConnectionState.Connecting -> "Connecting"
            is ConnectionState.NotConnected -> "Not connected"
        }
    }

    private fun updateObdState(obdState: ObdState) {
        obd_state.text = when (obdState) {
            is ObdState.ObdReady -> "OBD ready"
            is ObdState.ObdNotReady -> "OBD not ready"
            is ObdState.ObdLoading -> "OBD loading"
        }
    }

    private fun updateObdServiceState(obdServiceState: ObdServiceState) {
        button_action.apply {
            when (obdServiceState) {
                is ObdServiceState.Started -> {
                    text = "Stop"
                    isEnabled = true
                    setOnClickListener { obdService.stopSession() }
                }
                is ObdServiceState.Initialized -> {
                    text = "Start"
                    isEnabled = true
                    setOnClickListener { startSession() }
                }
                is ObdServiceState.Uninitialized -> {
                    text = "Start"
                    isEnabled = false
                    setOnClickListener { }
                }
            }
        }
    }
}
