package com.szklimek.auto.connection

import com.github.pires.obd.commands.ObdCommand

interface ObdDeviceManager {

    fun initOBD(callback: (Result<String>) -> Unit)

    fun runCommand(command: ObdCommand, callback: (Result<String>) -> Unit)

    fun closeOBD(callback: (Result<String>) -> Unit)
}
