package nl.chimpgamer.ultimatemobcoins.paper.hooks

import com.willfp.ecobosses.events.BossKillEvent
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.registerEvents
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

class EcoBossesHook(private val plugin: UltimateMobCoinsPlugin) : Listener {
    private val isPluginEnabled = plugin.server.pluginManager.isPluginEnabled("EcoBosses")

    fun load() {
        if (isPluginEnabled) {
            plugin.registerEvents(this)
            plugin.logger.info("Successfully loaded EcoBosses hook!")
        }
    }

    fun unload() {
        HandlerList.unregisterAll(this)
    }

    @EventHandler
    fun BossKillEvent.onBossKill() {
        if (killer is Player) {
            // Dirty way to get the boss name since Methods from the Boss class do require libreforge
            val bossName = "${boss.boss}".replaceFirst("EcoBoss{", "").dropLast(1)
            val mobCoinItem = plugin.mobCoinsManager.getCoin(killer as Player, bossName) ?: return

            if (event.drops.any { it.type === mobCoinItem.type }) return

            event.drops.add(mobCoinItem)
        }
    }
}