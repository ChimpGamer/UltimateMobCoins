package nl.chimpgamer.ultimatemobcoins.paper.listeners

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.getBoolean
import nl.chimpgamer.ultimatemobcoins.paper.extensions.pdc
import nl.chimpgamer.ultimatemobcoins.paper.utils.NamespacedKeys
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.metadata.FixedMetadataValue

class EntityListener(private val plugin: UltimateMobCoinsPlugin) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityDeathEvent.onEntityDeath() {
        val killer = entity.killer ?: return

        // Don't drop mob coins when in disabled world
        if (plugin.settingsConfig.mobCoinsDisabledWorlds.contains(entity.world.name)) return

        // Don't drop mob coins when it is not allowed by hook(s)
        if (!plugin.hookManager.isMobCoinDropsAllowed(killer, entity.location)) return

        // If entity is a mythic mob don't drop mob coins through this event.
        if (plugin.hookManager.mythicMobsHook.isMythicMob(entity)) return

        val mobCoinItem = plugin.mobCoinsManager.getCoin(killer, entity) ?: return
        if (drops.any { it.type === mobCoinItem.type }) return

        drops.add(mobCoinItem)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun ItemSpawnEvent.onItemSpawn() {
        val itemStack = entity.itemStack
        itemStack.itemMeta.pdc {
            if (!has(NamespacedKeys.isMobCoin) || !getBoolean(NamespacedKeys.isMobCoin)) return
        }
        entity.setMetadata("NO_PICKUP", FixedMetadataValue(plugin, true)) // UpgradableHoppers support
    }
}