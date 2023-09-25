package nl.chimpgamer.ultimatemobcoins.paper.listeners

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class ConnectionListener(private val plugin: UltimateMobCoinsPlugin) : Listener {

    @EventHandler
    suspend fun PlayerJoinEvent.onPlayerJoin() {
        plugin.userManager.onLogin(player.uniqueId, player.name)
    }

    @EventHandler
    fun PlayerQuitEvent.onPlayerQuit() {
        plugin.userManager.houseKeeper.registerUsage(player.uniqueId)
    }
}