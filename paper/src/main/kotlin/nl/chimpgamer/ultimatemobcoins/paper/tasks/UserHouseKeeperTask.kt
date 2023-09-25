package nl.chimpgamer.ultimatemobcoins.paper.tasks

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.utils.ExpiringSet
import java.util.UUID
import java.util.concurrent.TimeUnit

class UserHouseKeeperTask(private val plugin: UltimateMobCoinsPlugin) : Runnable {
    private val recentlyUsed: ExpiringSet<UUID> = ExpiringSet(1L, TimeUnit.MINUTES)
    fun registerUsage(uuid: UUID) = recentlyUsed.add(uuid)

    override fun run() {
        plugin.userManager.cache.keys.forEach { cleanup(it) }
    }

    private fun cleanup(uuid: UUID) {
        if (recentlyUsed.contains(uuid) || plugin.server.getPlayer(uuid)?.isOnline == true) return
        plugin.userManager.unload(uuid)
    }
}