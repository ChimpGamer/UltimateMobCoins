package nl.chimpgamer.ultimatemobcoins.paper.listeners

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.utils.NumberFormatter
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import java.math.BigDecimal
import java.math.MathContext

class PlayerDeathListener(private val plugin: UltimateMobCoinsPlugin) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    suspend fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        val user = plugin.userManager.getIfLoaded(player) ?: return
        val type = plugin.settingsConfig.mobCoinsLossOnDeathType
        val value = plugin.settingsConfig.mobCoinsLossOnDeathValue
        if (value <= 0) return

        val toTake = if (type.equals("percentage", ignoreCase = true)) {
            // C * (V / 100)
            user.coins.multiply(value.toBigDecimal().divide(100.toBigDecimal()))
        } else if (type.equals("fixed", ignoreCase = true)) {
            value.toBigDecimal(MathContext(3))
        } else {
            return
        }
        if (user.hasEnough(toTake)) {
            user.withdrawCoins(toTake)
        } else {
            user.coins(BigDecimal.ZERO)
        }
        player.sendMessage(plugin.messagesConfig.mobCoinsLostCoins.parse(
            mapOf(
                "value" to value,
                "mobcoins_lost" to NumberFormatter.displayCurrency(toTake),
                "mobcoins" to user.coinsPretty
            )
        ))
    }
}