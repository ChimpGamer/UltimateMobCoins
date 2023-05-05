package nl.chimpgamer.ultimatemobcoins.paper.hooks

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.registerEvents
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class MythicMobsHook(private val plugin: UltimateMobCoinsPlugin) : Listener {

    private val isPluginEnabled = plugin.server.pluginManager.isPluginEnabled("MythicMobs")

    fun load() {
        if (isPluginEnabled) {
            plugin.registerEvents(this)
            plugin.logger.info("Hooked into MythicMobs")
        }
    }

    fun unload() {
        HandlerList.unregisterAll(this)
    }

    @EventHandler
    fun MythicMobDeathEvent.onMythicMobsDeath() {
        if (killer is Player) {
            val mobCoinItem = plugin.mobCoinsManager.getCoin(killer as Player, mobType.internalName) ?: return

            if (drops.any { it.type === mobCoinItem.type }) return

            val newDrops = drops.toMutableList()
            newDrops.add(mobCoinItem)
            drops = newDrops
        }
    }
}