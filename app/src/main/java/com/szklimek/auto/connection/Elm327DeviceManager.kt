package com.szklimek.auto.connection

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.github.pires.obd.commands.ObdCommand
import com.github.pires.obd.commands.engine.RPMCommand
import com.github.pires.obd.commands.protocol.*
import com.github.pires.obd.enums.AvailableCommandNames
import com.szklimek.auto.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class Elm327DeviceManager(
    private val socket: BluetoothSocket,
    private val device: BluetoothDevice
) : ObdDeviceManager {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun initOBD(callback: (Result<String>) -> Unit) {
        Log.d("")
        coroutineScope.launch {
            val result = runCatching {
                socket.apply {
                    sendObdCommand(ObdResetCommand())
                    sendObdCommand(EchoOffCommand())
                    sendObdCommand(LineFeedOffCommand())
                    sendObdCommand(TimeoutCommand(60))
                }
                "OBD device ready"
            }
            withContext(Dispatchers.Main) {
                callback.invoke(result)
            }
        }
    }

    override fun runCommand(command: ObdCommand, callback: (Result<String>) -> Unit) {
        Log.d("Command: ${command.name}")
        coroutineScope.launch {
            val result = runCatching { socket.sendObdCommand(RPMCommand()) }
            withContext(Dispatchers.Main) {
                callback.invoke(result)
            }
        }
    }

    override fun closeOBD(callback: (Result<String>) -> Unit) {
        Log.d("")
        coroutineScope.launch {
            val result = runCatching { socket.sendObdCommand(CloseCommand()) }
            withContext(Dispatchers.Main) {
                callback.invoke(result)
            }
        }
    }
}

fun BluetoothSocket.sendObdCommand(command: ObdCommand): String {
    val commandName = AvailableCommandNames.valueOf(command.name)
    Log.d("Sending: $commandName")
    val elapsedRunTime = measureTimeMillis {
        command.run(inputStream, outputStream)
    }

    val result = command.formattedResult
    Log.d("Received [$commandName] in ${elapsedRunTime}ms: ${command.result} ")
    return result
}
