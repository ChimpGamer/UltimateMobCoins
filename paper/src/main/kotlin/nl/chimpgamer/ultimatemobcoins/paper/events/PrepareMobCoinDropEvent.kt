package nl.chimpgamer.ultimatemobcoins.paper.events

import nl.chimpgamer.ultimatemobcoins.paper.models.MobCoin
import nl.chimpgamer.ultimatemobcoins.paper.models.User
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

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