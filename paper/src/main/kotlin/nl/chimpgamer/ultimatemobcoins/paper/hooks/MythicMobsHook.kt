package nl.chimpgamer.ultimatemobcoins.paper.hooks

import io.lumine.mythic.bukkit.MythicBukkit
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.entity.Entity

class MythicMobsHook(private val plugin: UltimateMobCoinsPlugin) {

    private val isPluginEnabled get() = plugin.server.pluginManager.isPluginEnabled("MythicMobs")

    fun load() {
        if (isPluginEnabled) {
            plugin.logger.info("Successfully loaded MythicMobs hook!")
        }
    }

    fun unload() {

    }

    fun isMythicMob(entity: Entity): Boolean {
        return if (isPluginEnabled) {
            MythicBukkit.inst().mobManager.isActiveMob(entity.uniqueId)
        } else {
            false
        }
    }

    fun getMythicMobId(entity: Entity): String? {
        return if (isPluginEnabled) {
            MythicBukkit.inst().mobManager.getMythicMobInstance(entity)?.type?.internalName
        } else null
    }
}