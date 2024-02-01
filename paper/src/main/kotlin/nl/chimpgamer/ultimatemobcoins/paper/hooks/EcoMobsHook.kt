package nl.chimpgamer.ultimatemobcoins.paper.hooks

import com.willfp.ecomobs.mob.impl.ecoMob
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob

class EcoMobsHook(private val plugin: UltimateMobCoinsPlugin) {
    private val isPluginEnabled get() = plugin.server.pluginManager.isPluginEnabled("EcoMobs")

    private var hookEnabled: Boolean = false

    fun load() {
        if (!hookEnabled && isPluginEnabled) {
            plugin.logger.info("Successfully loaded EcoMobs hook!")
            hookEnabled = true
        }
    }

    fun unload() {

    }

    fun isEcoMob(entity: LivingEntity): Boolean {
        if (entity is Mob) {
            return entity.ecoMob != null
        }
        return false
    }

    fun getEcoMobId(entity: LivingEntity): String? {
        if (entity is Mob) {
            return entity.ecoMob?.id
        }
        return null
    }
}