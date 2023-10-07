package nl.chimpgamer.ultimatemobcoins.paper.events

import nl.chimpgamer.ultimatemobcoins.paper.storage.user.UserEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.math.BigDecimal

class MobCoinsRedeemEvent(
    val player: Player,
    val user: UserEntity,
    var amount: BigDecimal
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