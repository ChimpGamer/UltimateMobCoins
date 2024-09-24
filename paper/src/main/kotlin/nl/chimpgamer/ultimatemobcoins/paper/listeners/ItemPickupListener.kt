package nl.chimpgamer.ultimatemobcoins.paper.listeners

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.events.MobCoinsReceiveEvent
import nl.chimpgamer.ultimatemobcoins.paper.extensions.getBoolean
import nl.chimpgamer.ultimatemobcoins.paper.extensions.getDouble
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.extensions.pdc
import nl.chimpgamer.ultimatemobcoins.paper.utils.NamespacedKeys
import nl.chimpgamer.ultimatemobcoins.paper.utils.NumberFormatter
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import java.math.BigDecimal

class ItemPickupListener(private val plugin: UltimateMobCoinsPlugin) : Listener {

    @EventHandler(ignoreCancelled = true)
    suspend fun PlayerAttemptPickupItemEvent.onPlayerAttemptPickupItem() {
        val itemStack = item.itemStack
        if (!itemStack.hasItemMeta()) return
        val mobCoinItem = plugin.settingsConfig.getMobCoinsItem(TagResolver.empty())
        if (itemStack.type !== mobCoinItem.type) return
        var amount = BigDecimal.ZERO
        itemStack.itemMeta.pdc {
            if (!has(NamespacedKeys.isMobCoin) || !getBoolean(NamespacedKeys.isMobCoin)) return
            amount = getDouble(NamespacedKeys.mobCoinAmount)?.toBigDecimal() ?: return
        }
        isCancelled = true

        amount = amount.multiply(itemStack.amount.toBigDecimal())
        item.remove()

        // Deposit coins

        val user = plugin.userManager.getIfLoaded(player)
        if (user == null) {
            plugin.logger.warning("Something went wrong! Could not get user ${player.name} (${player.uniqueId})")
            return
        }
        if (!MobCoinsReceiveEvent(player, user, amount).callEvent()) return
        user.depositCoins(amount)
        user.addCoinsCollected(amount)
        val amountPretty = NumberFormatter.displayCurrency(amount)
        plugin.messagesConfig.mobCoinsReceivedChat
            .takeIf { it.isNotEmpty() }
            ?.let { player.sendMessage(it.parse(mapOf("amount" to amountPretty))) }
        plugin.messagesConfig.mobCoinsReceivedActionBar
            .takeIf { it.isNotEmpty() }
            ?.let { player.sendActionBar(it.parse(mapOf("amount" to amountPretty))) }
        plugin.settingsConfig.mobCoinsSoundsPickup.play(player)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun InventoryPickupItemEvent.onInventoryPickupItem() {
        if (inventory.type !== InventoryType.HOPPER) return
        val itemStack = item.itemStack
        if (!itemStack.hasItemMeta()) return
        itemStack.itemMeta.pdc {
            if (!has(NamespacedKeys.isMobCoin) || !getBoolean(NamespacedKeys.isMobCoin)) return
        }
        isCancelled = true
    }
}