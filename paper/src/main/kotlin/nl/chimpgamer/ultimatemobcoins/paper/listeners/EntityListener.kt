package nl.chimpgamer.ultimatemobcoins.paper.listeners

import de.tr7zw.nbtapi.NBTItem
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
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

        val mobCoinItem = plugin.mobCoinsManager.getCoin(killer, entity) ?: return
        if (drops.any { it.type === mobCoinItem.type }) return

        drops.add(mobCoinItem)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun ItemSpawnEvent.onItemSpawn() {
        val itemStack = entity.itemStack
        val nbtItem = NBTItem(itemStack)
        if (!nbtItem.hasNBTData()
            && !nbtItem.hasTag("isMobCoin") ||
            !nbtItem.getBoolean("isMobCoin")) return
        entity.setMetadata("NO_PICKUP", FixedMetadataValue(plugin, true)) // UpgradableHoppers support
    }
}