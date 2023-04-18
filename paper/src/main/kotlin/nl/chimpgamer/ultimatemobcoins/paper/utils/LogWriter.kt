package nl.chimpgamer.ultimatemobcoins.paper.utils

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

class LogWriter(
    private val plugin: UltimateMobCoinsPlugin,
    private val logEntry: String
) : Runnable {
    private val file: File = plugin.dataFolder.resolve("mobcoins.log")
    private val time: String

    override fun run() {
        try {
            PrintWriter(FileWriter(file, true)).use { writer ->
                writer.write("$time $logEntry")
                writer.write(System.getProperty("line.separator"))
                writer.flush()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    fun runAsync() = plugin.server.scheduler.runTaskAsynchronously(plugin, this)

    private fun createFileIfNotExist() {
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    init {
        // To make sure that a new file will be created when they remove the log file during runtime.
        createFileIfNotExist()

        val dateFormat = SimpleDateFormat("[dd-MM-yyyy HH:mm:ss]")
        time = dateFormat.format(Date())
    }
}