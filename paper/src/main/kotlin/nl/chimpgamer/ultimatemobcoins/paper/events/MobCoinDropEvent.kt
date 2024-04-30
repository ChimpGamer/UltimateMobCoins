package nl.chimpgamer.ultimatemobcoins.paper.events

import nl.chimpgamer.ultimatemobcoins.paper.models.User
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal

class MobCoinDropEvent(
    val player: Player,
    val user: User,
    var amount: BigDecimal,
    val mobCoinItemStack: ItemStack,
    var autoPickup: Boolean
) : Event(), Cancellable {
    private var cancelled = false
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}