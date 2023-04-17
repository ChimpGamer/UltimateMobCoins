package nl.chimpgamer.ultimatemobcoins.paper.extensions

import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

/* Registers all of these listeners for the plugin. */
fun Plugin.registerEvents(
    vararg listeners: Listener
) = listeners.forEach { server.pluginManager.registerEvents(it, this) }