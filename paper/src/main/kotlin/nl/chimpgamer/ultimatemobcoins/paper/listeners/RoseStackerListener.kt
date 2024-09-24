package nl.chimpgamer.ultimatemobcoins.paper.listeners

import dev.rosewood.rosestacker.event.EntityStackMultipleDeathEvent
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.events.MobCoinDropEvent
import nl.chimpgamer.ultimatemobcoins.paper.events.MobCoinsReceiveEvent
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.utils.NumberFormatter
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.math.BigDecimal

class RoseStackerListener(private val plugin: UltimateMobCoinsPlugin) : Listener {

    @EventHandler
    suspend fun EntityStackMultipleDeathEvent.onEntityStackMultipleDeathEvent() {
        val entity = stack.entity
        var killer: Entity? = entity.killer
        if (entity.lastDamageCause is EntityDamageByEntityEvent) {
            killer = (entity.lastDamageCause as EntityDamageByEntityEvent).damager
        }
        if (killer == null || killer !is Player) {
            return
        }

        // Don't drop mob coins when in disabled world
        if (plugin.settingsConfig.mobCoinsDisabledWorlds.contains(entity.world.name)) return

        // Don't drop mob coins when it is not allowed by hook(s)
        if (!plugin.hookManager.isMobCoinDropsAllowed(killer, entity.location)) return

        val user = plugin.userManager.getIfLoaded(killer)
        if (user == null) {
            plugin.logger.warning("Something went wrong! Could not get user ${killer.name} (${killer.uniqueId})")
            return
        }

        var autoPickup = false
        val multiplier = plugin.getMultiplier(killer)

        entityLoop@ for (entity1 in entityDrops.keySet()) {
            for (drops in entityDrops[entity1]) {
                val entityTypeName = plugin.hookManager.getEntityName(entity1)
                val dropAmount =
                    plugin.mobCoinsManager.getCoinDropAmount(killer, entityTypeName, multiplier) ?: continue
                val mobCoinItem = plugin.mobCoinsManager.createMobCoinItem(dropAmount)
                if (drops.drops.any { drop -> drop.type == mobCoinItem.type }) continue

                val mobCoinDropEvent = MobCoinDropEvent(
                    killer,
                    user,
                    entity1,
                    dropAmount,
                    mobCoinItem,
                    plugin.settingsConfig.mobCoinsAutoPickup,
                    isAsynchronous
                )
                if (!mobCoinDropEvent.callEvent()) continue

                if (!autoPickup) {
                    autoPickup = mobCoinDropEvent.autoPickup
                } else {
                    break@entityLoop
                }

                if (!autoPickup) {
                    drops.drops.add(mobCoinItem)
                }
            }
        }

        if (autoPickup) {
            val entityTypeName = plugin.hookManager.getEntityName(entity)
            var totalDropAmount = BigDecimal.ZERO
            for (i in 0..entityKillCount) {
                val dropAmount =
                    plugin.mobCoinsManager.getCoinDropAmount(killer, entityTypeName, multiplier) ?: continue
                totalDropAmount += dropAmount
            }

            if (!MobCoinsReceiveEvent(killer, user, totalDropAmount, isAsynchronous).callEvent()) return
            user.depositCoins(totalDropAmount)
            user.addCoinsCollected(totalDropAmount)
            val dropAmountPretty = NumberFormatter.displayCurrency(totalDropAmount)
            plugin.messagesConfig.mobCoinsReceivedChat
                .takeIf { it.isNotEmpty() }
                ?.let { killer.sendMessage(it.parse(mapOf("amount" to dropAmountPretty))) }
            plugin.messagesConfig.mobCoinsReceivedActionBar
                .takeIf { it.isNotEmpty() }
                ?.let { killer.sendActionBar(it.parse(mapOf("amount" to dropAmountPretty))) }
            plugin.settingsConfig.mobCoinsSoundsPickup.play(killer)
        }
    }
}