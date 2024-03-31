package nl.chimpgamer.ultimatemobcoins.paper.extensions

import com.github.shynixn.mccoroutine.folia.registerSuspendingEvents
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

/* Registers all of these listeners for the plugin. */
fun Plugin.registerEvents(
    vararg listeners: Listener
) = listeners.forEach { server.pluginManager.registerEvents(it, this) }

/* Registers all of these listeners for the plugin. */
fun Plugin.registerSuspendingEvents(vararg listeners: Listener, eventDispatcher: Map<Class<out Event>, (event: Event) -> CoroutineContext>) =
    listeners.forEach { server.pluginManager.registerSuspendingEvents(it, this, eventDispatcher) }