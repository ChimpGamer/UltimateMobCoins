package nl.chimpgamer.ultimatemobcoins.paper.listeners

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent

class ConnectionListener(private val plugin: UltimateMobCoinsPlugin) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun AsyncPlayerPreLoginEvent.onAsyncPlayerPreLogin() {
        if (loginResult !== AsyncPlayerPreLoginEvent.Result.ALLOWED) return

        plugin.userManager.loadUser(uniqueId, name)
    }

    @EventHandler
    fun PlayerQuitEvent.onPlayerQuit() {
        plugin.userManager.houseKeeper.registerUsage(player.uniqueId)
    }
}