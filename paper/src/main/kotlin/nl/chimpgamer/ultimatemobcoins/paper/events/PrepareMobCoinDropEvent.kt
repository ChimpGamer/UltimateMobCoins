package nl.chimpgamer.ultimatemobcoins.paper.events

import nl.chimpgamer.ultimatemobcoins.paper.models.MobCoin
import nl.chimpgamer.ultimatemobcoins.paper.models.User
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Represents an event triggered before a mob coin drop is processed.
 * This event allows developers to modify or cancel the mob coin drop behavior.
 *
 * @constructor
 * @param player The player associated with the event, typically the one who killed the entity.
 * @param user The user object representing the player.
 * @param entity The living entity that triggered the potential mob coin drop.
 * @param mobCoin The MobCoin instance related to the drop.
 * @param autoPickup Whether the player will automatically pick up the dropped mob coins.
 *                   This value can be modified to control the behavior.
 * @param async Whether this event is asynchronous.
 */
class PrepareMobCoinDropEvent(
    val player: Player,
    val user: User,
    val entity: LivingEntity,
    val mobCoin: MobCoin,
    var autoPickup: Boolean,
    async: Boolean = false
) : Event(async), Cancellable {
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