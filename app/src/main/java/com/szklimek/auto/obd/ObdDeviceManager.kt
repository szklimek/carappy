package com.szklimek.auto.obd

import androidx.lifecycle.LiveData
import com.github.pires.obd.commands.ObdCommand

interface ObdDeviceManager {

    val deviceConnectionState: LiveData<ConnectionState>
    val obdProtocolState: LiveData<ObdState>
    val logs: LiveData<List<String>>

    fun connect()

    fun resetConnection()

    fun initOBD()

    fun runAsyncCommand(command: ObdCommand, resultHandler: (Result<String>) -> Unit)

    fun runCommand(command: ObdCommand): Result<String>

    fun closeOBD()
}

sealed class ConnectionState {
    object Connecting : ConnectionState()
    object NotConnected : ConnectionState()
    object Connected : ConnectionState()
}

sealed class ObdState {
    object ObdLoading : ObdState()
    object ObdNotReady: ObdState()
    object ObdReady : ObdState()
}
