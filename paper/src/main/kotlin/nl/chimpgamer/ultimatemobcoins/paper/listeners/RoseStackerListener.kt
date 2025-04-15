package nl.chimpgamer.ultimatemobcoins.paper.listeners

import dev.rosewood.rosestacker.event.EntityStackMultipleDeathEvent
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.events.PrepareMobCoinDropEvent
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

        // Don't drop mob coins when in disabled world
        if (plugin.settingsConfig.mobCoinsDisabledWorlds.contains(entity.world.name)) return

        var killer: Entity? = entity.killer
        if (entity.lastDamageCause is EntityDamageByEntityEvent) {
            killer = (entity.lastDamageCause as EntityDamageByEntityEvent).damager
        }
        if (killer == null || killer !is Player) {
            return
        }

        if (!killer.hasPermission("ultimatemobcoins.dropcoin")) return

        // Don't drop mob coins when it is not allowed by hook(s)
        if (!plugin.hookManager.isMobCoinDropsAllowed(killer, entity.location)) return

        val user = plugin.userManager.getIfLoaded(killer)
        if (user == null) {
            plugin.logger.warning("Something went wrong! Could not get user ${killer.name} (${killer.uniqueId})")
            return
        }

        // Since it is not possible to have multiple entity types in the same stack
        // We can just assume that the entity is the right entity type.
        val entityTypeName = plugin.hookManager.getEntityName(entity)
        val mobCoin = plugin.mobCoinsManager.getMobCoin(entityTypeName) ?: return
        mobCoin.applyDropChanceMultiplier(killer)
        val autoPickup = plugin.settingsConfig.mobCoinsAutoPickup && killer.hasPermission("ultimatemobcoins.autopickup")
        val dropsMultiplier = plugin.getMobCoinDropsMultiplier(killer)

        val prepareMobCoinDropEvent = PrepareMobCoinDropEvent(
            killer,
            user,
            entity,
            mobCoin,
            autoPickup,
            isAsynchronous
        )
        if (!prepareMobCoinDropEvent.callEvent()) return

        if (!prepareMobCoinDropEvent.autoPickup) {
            for (entity1 in entityDrops.keySet()) {
                for (drops in entityDrops[entity1]) {
                    val dropAmount =
                        plugin.mobCoinsManager.getCoinDropAmount(killer, mobCoin, dropsMultiplier) ?: continue
                    val mobCoinItem = plugin.mobCoinsManager.createMobCoinItem(dropAmount)
                    if (drops.drops.any { drop -> drop.type == mobCoinItem.type }) continue

                    val mobCoinDropEvent = MobCoinDropEvent(
                        killer,
                        user,
                        entity1,
                        dropAmount,
                        mobCoinItem,
                        isAsynchronous
                    )
                    if (!mobCoinDropEvent.callEvent()) continue

                    drops.drops.add(mobCoinItem)
                }
            }
            plugin.settingsConfig.mobCoinsSoundsDrop.play(killer)
            return
        }

        // Auto pickup is enabled...
        var totalDropAmount = BigDecimal.ZERO
        for (i in 0..entityKillCount) {
            val dropAmount =
                plugin.mobCoinsManager.getCoinDropAmount(killer, mobCoin, dropsMultiplier) ?: continue
            totalDropAmount += dropAmount
        }
        if (totalDropAmount == BigDecimal.ZERO) return

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