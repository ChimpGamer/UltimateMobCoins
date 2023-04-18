package nl.chimpgamer.ultimatemobcoins.paper.listeners

import de.tr7zw.nbtapi.NBTItem
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class PlayerListener(private val plugin: UltimateMobCoinsPlugin) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun AsyncPlayerPreLoginEvent.onAsyncPlayerPreLogin() {
        plugin.userManager.onLogin(uniqueId, name)
    }

    @EventHandler
    fun PlayerInteractEvent.onPlayerInteract() {
        if (hand !== EquipmentSlot.HAND) return
        if (!(action === Action.RIGHT_CLICK_BLOCK || action === Action.RIGHT_CLICK_AIR)) return
        val itemInHand = item ?: return
        val nbtItem = NBTItem(itemInHand)
        if (!nbtItem.hasNBTData() &&
            !nbtItem.hasTag("isMobCoin") ||
            !nbtItem.getBoolean("isMobCoin")) return
        isCancelled = true

        // Deposit amount
        var amount = nbtItem.getDouble("amount")
        amount *= itemInHand.amount

        val user = plugin.userManager.getByUUID(player.uniqueId)
        if (user == null) {
            plugin.logger.warning("Something went wrong! Could not get user ${player.name} (${player.uniqueId})")
            return
        }
        user.depositCoins(amount.toBigDecimal())
        player.inventory.setItemInMainHand(null)
        player.sendActionBar("<green>+<mobcoins> MobCoin(s)".parse(mapOf("mobcoins" to amount)))
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
    }
}