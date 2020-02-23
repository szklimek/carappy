package com.szklimek.auto.obd

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.github.pires.obd.commands.ObdCommand
import com.szklimek.auto.PreferenceManager
import com.szklimek.auto.obd.elm327.Elm327DeviceManager
import com.szklimek.base.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ObdService(val preferenceManager: PreferenceManager) {
    val connectionState = MutableLiveData<ConnectionState>()
    val obdState = MutableLiveData<ObdState>()
    val logs = MutableLiveData<List<String>>()
    val serviceState =
        MutableLiveData<ObdServiceState>().apply { value = ObdServiceState.Uninitialized }

    private var connectionJob: Job? = null
    private var obdDeviceManager: ObdDeviceManager? = null
    private val connectionStateObserver = Observer<ConnectionState> {
        Log.d("ConnectionState changed: ${it.javaClass.simpleName}")
        connectionState.value = it
    }
    private val obdStateObserver = Observer<ObdState> {
        Log.d("ObdState changed: ${it.javaClass.simpleName}")
        obdState.value = it
    }
    private val logsObserver = Observer<List<String>> { logs.value = it }

    init {
        preferenceManager.getPairedDevice()
            ?.let { initializeDeviceManager(Elm327DeviceManager(it)) }
    }

    private fun initializeDeviceManager(obdDeviceManager: ObdDeviceManager) {
        Log.d("")
        this.obdDeviceManager = obdDeviceManager
        obdDeviceManager.deviceConnectionState.observeForever(connectionStateObserver)
        obdDeviceManager.obdProtocolState.observeForever(obdStateObserver)
        obdDeviceManager.logs.observeForever(logsObserver)
        serviceState.value = ObdServiceState.Initialized
    }

    fun startSession() {
        if (serviceState.value is ObdServiceState.Started) {
            Log.d("Session already started. Aborting")
            return
        }

        if (serviceState.value is ObdServiceState.Uninitialized) {
            Log.d("Device not selected causing service to be uninitialized. Aborting")
            return
        }

        serviceState.value = ObdServiceState.Started
        connectionJob = CoroutineScope(Dispatchers.IO).launch {
            while (connectionState.value !is ConnectionState.Connected) {
                Log.d("Connection attempt")
                connect()
                Thread.sleep(5000)
            }
            while (obdState.value !is ObdState.ObdReady) {
                if (connectionState.value == ConnectionState.Connected)
                    Log.d("Attempt to initialize")
                initOBD()
                Thread.sleep(3000)
            }
        }
    }

    fun stopSession() {
        Log.d("")
        serviceState.value = ObdServiceState.Initialized
        connectionJob?.cancel()
    }

    fun connect() {
        Log.d("")
        obdDeviceManager!!.connect()
    }

    fun resetConnection() {
        Log.d("")
        obdDeviceManager!!.resetConnection()
    }

    fun resetDevice() {
        Log.d("")
        obdDeviceManager?.deviceConnectionState?.removeObserver(connectionStateObserver)
        obdDeviceManager?.obdProtocolState?.removeObserver(obdStateObserver)
        obdDeviceManager?.logs?.removeObserver(logsObserver)
        obdDeviceManager = null
        serviceState.value = ObdServiceState.Uninitialized
    }

    fun initOBD() {
        obdDeviceManager!!.initOBD()
    }

    fun runCommand(command: ObdCommand): Result<String> {
        val result = obdDeviceManager!!.runCommand(command)
        Log.d("Command result {${command.name}}: $result")
        return result
    }

    fun runAsyncCommand(command: ObdCommand, resultHandler: (Result<String>) -> Unit) {
        obdDeviceManager?.runAsyncCommand(command, resultHandler)
    }

    fun closeOBD() {
        obdDeviceManager!!.closeOBD()
    }
}

sealed class ObdServiceState {
    object Uninitialized : ObdServiceState()
    object Initialized : ObdServiceState()
    object Started : ObdServiceState()
}
