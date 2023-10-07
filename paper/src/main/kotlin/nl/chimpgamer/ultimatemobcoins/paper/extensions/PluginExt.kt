package nl.chimpgamer.ultimatemobcoins.paper.extensions

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

/* Registers all of these listeners for the plugin. */
fun Plugin.registerEvents(
    vararg listeners: Listener
) = listeners.forEach { server.pluginManager.registerEvents(it, this) }

/* Registers all of these listeners for the plugin. */
fun Plugin.registerSuspendingEvents(vararg listeners: Listener) =
    listeners.forEach { server.pluginManager.registerSuspendingEvents(it, this) }

fun Plugin.runSync(runnable: Runnable) = server.scheduler.runTask(this, runnable)