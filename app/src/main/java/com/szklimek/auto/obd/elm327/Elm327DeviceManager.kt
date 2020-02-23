package com.szklimek.auto.obd.elm327

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.lifecycle.MutableLiveData
import com.github.pires.obd.commands.ObdCommand
import com.github.pires.obd.commands.protocol.*
import com.szklimek.auto.obd.ConnectionState
import com.szklimek.auto.obd.ObdDeviceManager
import com.szklimek.auto.obd.ObdState
import com.szklimek.base.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import kotlin.system.measureTimeMillis

class Elm327DeviceManager(private val device: BluetoothDevice) :
    ObdDeviceManager {

    override val deviceConnectionState =
        MutableLiveData<ConnectionState>().apply { value = ConnectionState.NotConnected }

    override val obdProtocolState =
        MutableLiveData<ObdState>().apply { value = ObdState.ObdNotReady }

    private var socket: BluetoothSocket? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override val logs = MutableLiveData<List<String>>().apply { value = emptyList() }

    override fun connect() {
        Log.d("")
        socket =
            device.createRfcommSocketToServiceRecord(
                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            )

        coroutineScope.launch {

            // First trial
            val firstResult = runCatching {
                Log.d("Connecting to: ${device.name}[${device.address}]")

                addDeviceLog("Connecting to: ${device.name}[${device.address}]")
                deviceConnectionState.postValue(ConnectionState.Connecting)
                socket!!.connect()
            }
            if (firstResult.isFailure) {

                // Second (and last) attempt
                Log.d("Connection error: ${firstResult.exceptionOrNull()?.message}")
                addDeviceLog("Connection error: ${firstResult.exceptionOrNull()?.message}")
                var sockFallback: BluetoothSocket?

                val clazz = socket!!.remoteDevice.javaClass
                val paramTypes = arrayOf<Class<*>>(Integer.TYPE)

                val secondResult = runCatching {
                    Log.d("Retrying")
                    addDeviceLog("Retrying")
                    val m = clazz.getMethod("createRfcommSocket", *paramTypes)
                    val params = arrayOf<Any>(Integer.valueOf(1))
                    sockFallback = m.invoke(socket!!.remoteDevice, *params) as BluetoothSocket
                    sockFallback!!.connect()
                    socket = sockFallback
                }
                if (secondResult.isFailure) {
                    Log.d("Connection error 2nd trial: ${firstResult.exceptionOrNull()?.message}")

                    addDeviceLog("Connection error 2nd trial: ${firstResult.exceptionOrNull()?.message}")
                    deviceConnectionState.postValue(ConnectionState.NotConnected)
                }
                return@launch
            }
            deviceConnectionState.postValue(ConnectionState.Connected)
            addDeviceLog("Connection established")
        }
    }

    override fun resetConnection() {
        Log.d("")
        socket?.close()
        deviceConnectionState.postValue(ConnectionState.NotConnected)
        obdProtocolState.postValue(ObdState.ObdNotReady)
    }

    override fun initOBD() {
        Log.d("")
        if (!invalidateConnection()) return
        if (obdProtocolState.value == ObdState.ObdLoading) {
            Log.d("Trying to reinitialize OBD while OBD loading. Aborting")
            return
        }

        obdProtocolState.postValue(ObdState.ObdLoading)
        coroutineScope.launch {
            val result = runCatching {
                socket!!.apply {
                    listOf(
                        ObdRawCommand("AT D"),
                        ObdResetCommand(),
                        EchoOffCommand(),
                        LineFeedOffCommand(),
                        SpacesOffCommand(),
                        ObdRawCommand("AT SP3"),
                        DescribeProtocolCommand()
                    ).forEach {
                        sendObdCommand(it)
                        Thread.sleep(500)
                    }
                }
            }
            when {
                result.isFailure -> {
                    Log.d("OBD Init error: ${result.exceptionOrNull()?.message}")
                    obdProtocolState.postValue(ObdState.ObdNotReady)
                }
                result.isSuccess -> {
                    Log.d("OBD Init success: ${result.getOrNull()}")
                    obdProtocolState.postValue(ObdState.ObdReady)
                }
            }
        }
    }

    override fun runAsyncCommand(command: ObdCommand, resultHandler: (Result<String>) -> Unit) {
        Log.d("Command: ${command.name}")
        if (!invalidateConnection()) return

        coroutineScope.launch {
            addDeviceLog("Sending command: ${command.name}")
            val result = runCatching {
                socket!!.sendObdCommand(command)
            }
            addDeviceLog("Command [${command.name}] success: ${result.getOrNull()}, failure: ${result.exceptionOrNull()}")

            CoroutineScope(Dispatchers.Main).launch {
                resultHandler(result)
            }
        }
    }

    override fun runCommand(command: ObdCommand): Result<String> {
        Log.d("Command: ${command.name}")
        if (!invalidateConnection()) return Result.failure(IOException("Device not connected"))

        addDeviceLog("Sending command: ${command.name}")
        val result = runCatching { socket!!.sendObdCommand(command) }
        addDeviceLog("Command [${command.name}] success: ${result.getOrNull()}, failure: ${result.exceptionOrNull()}")

        return result
    }

    override fun closeOBD() {
        Log.d("")
        if (!invalidateConnection()) return

        obdProtocolState.postValue(ObdState.ObdLoading)
        coroutineScope.launch {
            val result = runCatching { socket!!.sendObdCommand(CloseCommand()) }
            when {
                result.isFailure -> {
                    Log.d("OBD Closing error: ${result.exceptionOrNull()?.message}")
                    obdProtocolState.postValue(ObdState.ObdNotReady)
                }
                result.isSuccess -> {
                    Log.d("OBD Closing success: ${result.getOrNull()}")
                    obdProtocolState.postValue(ObdState.ObdReady)
                }
            }
            addDeviceLog("OBD closing success: ${result.getOrNull()}, failure: ${result.exceptionOrNull()}")
        }
    }

    private fun invalidateConnection(): Boolean {
        Log.d("")
        val isConnected = when {
            socket == null -> {
                Log.d("Empty connection, connect to device again")
                addDeviceLog("Empty connection, connect to device again")
                false
            }
            !socket!!.isConnected -> {
                Log.d("Socket initialized but not connected")
                addDeviceLog("Socket initialized but not connected")
                false
            }
            else -> true
        }
        if (!isConnected) {
            obdProtocolState.postValue(ObdState.ObdNotReady)
            deviceConnectionState.postValue(ConnectionState.NotConnected)
        }
        return isConnected
    }

    private fun addDeviceLog(log: String) {
        logs.postValue(logs.value!!.toMutableList().apply { add(log) })
    }
}

fun BluetoothSocket.sendObdCommand(command: ObdCommand): String {
    val commandName = command.name
    Log.d("Sending: $commandName")
    val elapsedRunTime = measureTimeMillis {
        command.run(inputStream, outputStream)
    }

    val result = command.formattedResult
    Log.d("Received [$commandName] in ${elapsedRunTime}ms: ${command.result} ")
    return result
}
