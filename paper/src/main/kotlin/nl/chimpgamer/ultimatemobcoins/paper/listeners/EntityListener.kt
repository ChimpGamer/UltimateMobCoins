package nl.chimpgamer.ultimatemobcoins.paper.listeners

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.events.MobCoinsReceiveEvent
import nl.chimpgamer.ultimatemobcoins.paper.extensions.getBoolean
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.extensions.pdc
import nl.chimpgamer.ultimatemobcoins.paper.utils.NamespacedKeys
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.metadata.FixedMetadataValue

class EntityListener(private val plugin: UltimateMobCoinsPlugin) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    suspend fun EntityDeathEvent.onEntityDeath() {
        val killer = entity.killer ?: return
        var entityTypeName = entity.type.name.lowercase()

        // Don't drop mob coins when in disabled world
        if (plugin.settingsConfig.mobCoinsDisabledWorlds.contains(entity.world.name)) return

        // Don't drop mob coins when it is not allowed by hook(s)
        if (!plugin.hookManager.isMobCoinDropsAllowed(killer, entity.location)) return

        // If entity is a mythic mob don't drop mob coins through this event.
        if (plugin.hookManager.mythicMobsHook.isMythicMob(entity)) {
            val mythicMobId = plugin.hookManager.mythicMobsHook.getMythicMobId(entity)
            if (mythicMobId != null) {
                entityTypeName = mythicMobId
            }
        }

        // If entity is a EcoMobs mob then we have to alter the entityTypeName.
        if (plugin.hookManager.ecoMobsHook.isEcoMob(entity)) {
            val ecoMobId = plugin.hookManager.ecoMobsHook.getEcoMobId(entity)
            if (ecoMobId != null) {
                entityTypeName = ecoMobId
            }
        }

        val dropAmount = plugin.mobCoinsManager.getCoinDropAmount(killer, entityTypeName) ?: return
        val mobCoinItem = plugin.mobCoinsManager.createMobCoinItem(dropAmount)
        if (drops.any { it.type === mobCoinItem.type }) return

        if (plugin.settingsConfig.mobCoinsAutoPickup) {
            val user = plugin.userManager.getIfLoaded(killer)
            if (user == null) {
                plugin.logger.warning("Something went wrong! Could not get user ${killer.name} (${killer.uniqueId})")
                return
            }
            if (!MobCoinsReceiveEvent(killer, user, dropAmount).callEvent()) return
            user.depositCoins(dropAmount)
            user.addCoinsCollected(dropAmount)
            plugin.messagesConfig.mobCoinsReceivedChat.takeIf { it.isNotEmpty() }?.let { killer.sendMessage(it.parse(mapOf("amount" to dropAmount))) }
            plugin.messagesConfig.mobCoinsReceivedActionBar.takeIf { it.isNotEmpty() }?.let { killer.sendActionBar(it.parse(mapOf("amount" to dropAmount))) }
            plugin.settingsConfig.mobCoinsSoundsPickup.play(killer)
            return
        }
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