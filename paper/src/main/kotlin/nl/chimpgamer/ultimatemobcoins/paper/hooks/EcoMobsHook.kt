package nl.chimpgamer.ultimatemobcoins.paper.hooks

import com.willfp.ecomobs.mob.impl.ecoMob
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob

class EcoMobsHook(private val plugin: UltimateMobCoinsPlugin) {
    private val name = "EcoMobs"
    private val isPluginEnabled get() = plugin.server.pluginManager.isPluginEnabled(name)

    private var hookLoaded = false

    fun load() {
        if (!hookLoaded && isPluginEnabled && plugin.hooksConfig.isHookEnabled(name)) {
            plugin.logger.info("Successfully loaded $name hook!")
            hookLoaded = true
        }
    }

    fun unload() {

    }

    fun isEcoMob(entity: LivingEntity): Boolean {
        if (!hookLoaded) return false
        if (entity is Mob) {
            return entity.ecoMob != null
        }
        return false
    }

    fun getEcoMobId(entity: LivingEntity): String? {
        if (!hookLoaded) return null
        if (entity is Mob) {
            return entity.ecoMob?.id
        }
        return null
    }
}