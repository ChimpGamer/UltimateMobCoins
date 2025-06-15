package nl.chimpgamer.ultimatemobcoins.paper.events

import nl.chimpgamer.ultimatemobcoins.paper.models.User
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.math.BigDecimal

/**
 * Represents an event triggered when a player receives mob coins.
 *
 * @constructor
 * @param player The player who is receiving the mob coins.
 * @param user The user associated with the player.
 * @param amount The amount of mob coins being received. This value can be modified.
 * @param async Whether this event is asynchronous.
 */
class MobCoinsReceiveEvent(
    val player: Player,
    val user: User,
    amount: BigDecimal,
    async: Boolean = false
) : Event(async), Cancellable {

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