package nl.chimpgamer.ultimatemobcoins.paper.hooks

import io.lumine.mythic.bukkit.MythicBukkit
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.entity.Entity

class MythicMobsHook(private val plugin: UltimateMobCoinsPlugin) {
    private val name = "MythicMobs"
    private val isPluginEnabled get() = plugin.server.pluginManager.isPluginEnabled(name)
    private var hookLoaded = false

    fun load() {
        if (isPluginEnabled && plugin.hooksConfig.isHookEnabled(name)) {
            plugin.logger.info("Successfully loaded $name hook!")
            hookLoaded = true
        }
    }

    fun unload() {

    }

    fun isMythicMob(entity: Entity): Boolean {
        return if (hookLoaded) {
            MythicBukkit.inst().mobManager.isActiveMob(entity.uniqueId)
        } else {
            false
        }
    }

    fun getMythicMobId(entity: Entity): String? {
        return if (hookLoaded) {
            MythicBukkit.inst().mobManager.getMythicMobInstance(entity)?.type?.internalName
        } else null
    }
}