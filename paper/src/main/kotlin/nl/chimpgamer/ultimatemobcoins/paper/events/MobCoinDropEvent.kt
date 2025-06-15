package nl.chimpgamer.ultimatemobcoins.paper.events

import nl.chimpgamer.ultimatemobcoins.paper.models.User
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal

/**
 * Represents an event triggered when a mob coin is dropped as a result of a player
 * killing a living entity.
 *
 * @constructor
 * @param player The player who triggered the mob coin drop.
 * @param user The user associated with the player.
 * @param entity The living entity that was killed to trigger the drop.
 * @param amount The amount of mob coins to be dropped. This value can be modified.
 * @param mobCoinItemStack The ItemStack representing the dropped mob coin.
 * @param async Whether this event is asynchronous.
 */
class MobCoinDropEvent(
    val player: Player,
    val user: User,
    val entity: LivingEntity,
    amount: BigDecimal,
    val mobCoinItemStack: ItemStack,
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