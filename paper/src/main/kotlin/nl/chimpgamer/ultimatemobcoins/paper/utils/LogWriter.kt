package nl.chimpgamer.ultimatemobcoins.paper.utils

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class LogWriter(
    private val plugin: UltimateMobCoinsPlugin,
) {
    private val dateFormat = SimpleDateFormat("[dd-MM-yyyy HH:mm:ss]")
    private val file: File = plugin.dataFolder.resolve("mobcoins.log")

    suspend fun write(logEntry: String) {
        coroutineScope {
            val time = dateFormat.format(Date())
            try {
                file.appendText("$time $logEntry" + System.lineSeparator())
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    fun writeAsync(logEntry: String) {
        plugin.launch(plugin.asyncDispatcher, CoroutineStart.UNDISPATCHED) {
            write(logEntry)
        }
    }

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
    }
}