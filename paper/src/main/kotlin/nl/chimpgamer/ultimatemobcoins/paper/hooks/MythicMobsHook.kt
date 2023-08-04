package nl.chimpgamer.ultimatemobcoins.paper.hooks

import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.registerEvents
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class MythicMobsHook(private val plugin: UltimateMobCoinsPlugin) : Listener {

    private val isPluginEnabled get() = plugin.server.pluginManager.isPluginEnabled("MythicMobs")

    fun load() {
        if (isPluginEnabled) {
            plugin.registerEvents(this)
            plugin.logger.info("Successfully loaded MythicMobs hook!")
        }
    }

    fun unload() {
        HandlerList.unregisterAll(this)
    }

    fun isMythicMob(entity: Entity): Boolean {
        return if (isPluginEnabled) {
            MythicBukkit.inst().mobManager.isActiveMob(entity.uniqueId)
        } else {
            false
        }
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