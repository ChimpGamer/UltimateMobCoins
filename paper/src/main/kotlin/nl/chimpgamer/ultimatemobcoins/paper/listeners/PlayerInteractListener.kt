package nl.chimpgamer.ultimatemobcoins.paper.listeners

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.events.MobCoinsRedeemEvent
import nl.chimpgamer.ultimatemobcoins.paper.extensions.getBoolean
import nl.chimpgamer.ultimatemobcoins.paper.extensions.getDouble
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.extensions.pdc
import nl.chimpgamer.ultimatemobcoins.paper.utils.NamespacedKeys
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import java.math.BigDecimal

class PlayerInteractListener(private val plugin: UltimateMobCoinsPlugin) : Listener {

    @EventHandler
    suspend fun PlayerInteractEvent.onPlayerInteract() {
        if (hand !== EquipmentSlot.HAND) return
        if (!(action === Action.RIGHT_CLICK_BLOCK || action === Action.RIGHT_CLICK_AIR)) return
        val itemInHand = item ?: return
        var amount = BigDecimal.ZERO
        itemInHand.itemMeta.pdc {
            if (!has(NamespacedKeys.isMobCoin) || !getBoolean(NamespacedKeys.isMobCoin)) return
            amount = getDouble(NamespacedKeys.mobCoinAmount)?.toBigDecimal() ?: return
        }
        isCancelled = true

        // Deposit amount
        amount = amount.multiply(itemInHand.amount.toBigDecimal())

        val user = plugin.userManager.getIfLoaded(player)
        if (user == null) {
            plugin.logger.warning("Something went wrong! Could not get user ${player.name} (${player.uniqueId})")
            return
        }
        if (!MobCoinsRedeemEvent(player, user, amount).callEvent()) return
        user.depositCoins(amount)
        player.inventory.setItemInMainHand(null)
        player.sendActionBar("<green>+<mobcoins> MobCoin(s)".parse(mapOf("mobcoins" to amount)))
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
    }
}