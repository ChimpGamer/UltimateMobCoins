package nl.chimpgamer.ultimatemobcoins.paper.listeners

import de.tr7zw.nbtapi.NBTItem
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerAttemptPickupItemEvent

class ItemPickupListener(private val plugin: UltimateMobCoinsPlugin) : Listener {

    @EventHandler(ignoreCancelled = true)
    fun PlayerAttemptPickupItemEvent.onPlayerAttemptPickupItem() {
        if (item.itemStack.type !== Material.SUNFLOWER) return
        val nbtItem = NBTItem(item.itemStack)
        if (!nbtItem.hasNBTData() &&
            !nbtItem.hasTag("isMobCoin") ||
            !nbtItem.getBoolean("isMobCoin")) return
        isCancelled = true

        var amount = nbtItem.getDouble("amount").toBigDecimal()
        amount = amount.multiply(item.itemStack.amount.toBigDecimal())
        item.remove()

        // Deposit coins

        val user = plugin.userManager.getByUUID(player.uniqueId)
        if (user == null) {
            plugin.logger.warning("Something went wrong! Could not get user ${player.name} (${player.uniqueId})")
            return
        }
        user.depositCoins(amount)
        user.addCoinsCollected(amount)
        plugin.messagesConfig.mobCoinsReceivedChat.takeIf { it.isNotEmpty() }?.let { player.sendMessage(it.parse()) }
        plugin.messagesConfig.mobCoinsReceivedActionBar.takeIf { it.isNotEmpty() }?.let { player.sendActionBar(it.parse()) }
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun InventoryPickupItemEvent.onInventoryPickupItem() {
        if (inventory.type !== InventoryType.HOPPER) return
        val itemStack = item.itemStack
        val nbtItem = NBTItem(itemStack)
        if (!nbtItem.hasNBTData() &&
            !nbtItem.hasTag("isMobCoin") ||
            !nbtItem.getBoolean("isMobCoin")) return
        isCancelled = true
    }
}