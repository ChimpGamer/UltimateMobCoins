package nl.chimpgamer.ultimatemobcoins.paper.hooks

import com.willfp.ecomobs.mob.impl.ecoMob
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob

class EcoMobsHook(plugin: UltimateMobCoinsPlugin) : PluginHook(plugin, "EcoMobs") {

    override fun load() {
        if (!isLoaded && canHook()) {
            plugin.logger.info("Successfully loaded $pluginName hook!")
            isLoaded = true
        }
    }

    override fun unload() {
        isLoaded = false
    }

    fun isEcoMob(entity: LivingEntity): Boolean {
        if (!isLoaded) return false
        if (entity is Mob) {
            return entity.ecoMob != null
        }
        return false
    }

    fun getEcoMobId(entity: LivingEntity): String? {
        if (!isLoaded) return null
        if (entity is Mob) {
            return entity.ecoMob?.id
        }
        return null
    }
}