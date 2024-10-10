package nl.chimpgamer.ultimatemobcoins.paper.listeners

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.events.MobCoinDropEvent
import nl.chimpgamer.ultimatemobcoins.paper.events.MobCoinsReceiveEvent
import nl.chimpgamer.ultimatemobcoins.paper.events.PrepareMobCoinDropEvent
import nl.chimpgamer.ultimatemobcoins.paper.extensions.getBoolean
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.extensions.pdc
import nl.chimpgamer.ultimatemobcoins.paper.utils.NamespacedKeys
import nl.chimpgamer.ultimatemobcoins.paper.utils.NumberFormatter
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.metadata.FixedMetadataValue

class EntityListener(private val plugin: UltimateMobCoinsPlugin) : Listener {

    @EventHandler(ignoreCancelled = true)
    suspend fun EntityDeathEvent.onEntityDeath() {
        val killer = entity.killer ?: return
        var entityTypeName = entity.type.name.lowercase()

        if (plugin.hookManager.roseStackerHook.shouldIgnoreNormalDeathEvent(entity)) return // Listen to the RoseStacker EntityStackMultipleDeathEvent

        // Don't drop mob coins when in disabled world
        if (plugin.settingsConfig.mobCoinsDisabledWorlds.contains(entity.world.name)) return

        if (!killer.hasPermission("ultimatemobcoins.dropcoin")) return

        // Don't drop mob coins when it is not allowed by hook(s)
        if (!plugin.hookManager.isMobCoinDropsAllowed(killer, entity.location)) return

        val user = plugin.userManager.getIfLoaded(killer)
        if (user == null) {
            plugin.logger.warning("Something went wrong! Could not get user ${killer.name} (${killer.uniqueId})")
            return
        }

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

        val mobCoin = plugin.mobCoinsManager.getMobCoin(entityTypeName) ?: return
        mobCoin.applyDropChanceMultiplier(killer)
        val dropsMultiplier = plugin.getMobCoinDropsMultiplier(killer)

        val prepareMobCoinDropEvent = PrepareMobCoinDropEvent(
            killer,
            user,
            entity,
            mobCoin,
            plugin.settingsConfig.mobCoinsAutoPickup,
            isAsynchronous
        )
        if (!prepareMobCoinDropEvent.callEvent()) return

        val dropAmount = plugin.mobCoinsManager.getCoinDropAmount(killer, mobCoin, dropsMultiplier) ?: return
        val mobCoinItem = plugin.mobCoinsManager.createMobCoinItem(dropAmount)
        if (drops.any { it.type === mobCoinItem.type }) return

        val mobCoinDropEvent = MobCoinDropEvent(killer, user, entity, dropAmount, mobCoinItem, isAsynchronous)
        if (!mobCoinDropEvent.callEvent()) return

        if (prepareMobCoinDropEvent.autoPickup) {
            if (!MobCoinsReceiveEvent(killer, user, dropAmount).callEvent()) return
            user.depositCoins(dropAmount)
            user.addCoinsCollected(dropAmount)
            val dropAmountPretty = NumberFormatter.displayCurrency(dropAmount)
            plugin.messagesConfig.mobCoinsReceivedChat
                .takeIf { it.isNotEmpty() }
                ?.let { killer.sendMessage(it.parse(mapOf("amount" to dropAmountPretty))) }
            plugin.messagesConfig.mobCoinsReceivedActionBar
                .takeIf { it.isNotEmpty() }
                ?.let { killer.sendActionBar(it.parse(mapOf("amount" to dropAmountPretty))) }
            plugin.settingsConfig.mobCoinsSoundsPickup.play(killer)
            return
        }
        drops.add(mobCoinItem)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun ItemSpawnEvent.onItemSpawn() {
        val itemStack = entity.itemStack
        if (!itemStack.hasItemMeta()) return
        itemStack.itemMeta.pdc {
            if (!has(NamespacedKeys.isMobCoin) || !getBoolean(NamespacedKeys.isMobCoin)) return
        }
        entity.setMetadata("NO_PICKUP", FixedMetadataValue(plugin, true)) // UpgradableHoppers support
    }
}