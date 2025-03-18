package nl.chimpgamer.ultimatemobcoins.paper.hooks

import io.lumine.mythic.bukkit.MythicBukkit
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.entity.Entity

class MythicMobsHook(plugin: UltimateMobCoinsPlugin) : PluginHook(plugin, "MythicMobs") {

    override fun load() {
        if (canHook()) {
            plugin.logger.info("Successfully loaded $pluginName hook!")
            isLoaded = true
        }
    }

    override fun unload() {
        isLoaded = false
    }

    fun isMythicMob(entity: Entity): Boolean {
        return if (isLoaded) {
            MythicBukkit.inst().mobManager.isActiveMob(entity.uniqueId)
        } else {
            false
        }
    }

    fun getMythicMobId(entity: Entity): String? {
        return if (isLoaded) {
            MythicBukkit.inst().mobManager.getMythicMobInstance(entity)?.type?.internalName
        } else null
    }
}