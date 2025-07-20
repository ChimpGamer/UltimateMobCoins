package nl.chimpgamer.ultimatemobcoins.paper.events

import nl.chimpgamer.ultimatemobcoins.paper.models.User
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.math.BigDecimal

/**
 * Represents an event triggered when a player redeems mob coins.
 *
 * @constructor
 * @param player The player who is redeeming the mob coins.
 * @param user The user associated with the player.
 * @param amount The amount of mob coins being redeemed. This value can be modified.
 */
class MobCoinsRedeemEvent(
    val player: Player,
    val user: User,
    amount: BigDecimal
) : Event(), Cancellable {

    var amount: BigDecimal = amount
        set(value) {
            require(value >= BigDecimal.ZERO) { "Amount cannot be negative: $value" }
            field = value
        }

    init {
        require(amount >= BigDecimal.ZERO) { "Amount cannot be negative: $amount" }
    }

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