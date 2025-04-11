package nl.chimpgamer.ultimatemobcoins.paper.listeners

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
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

        if (type.equals("percentage", ignoreCase = true)) {
            // C * (V / 100)
            val toTake = user.coins.multiply(value.toBigDecimal().divide(100.toBigDecimal()))
            if (user.hasEnough(toTake)) {
                user.withdrawCoins(toTake)
            } else {
                user.coins(BigDecimal.ZERO)
            }
        } else if (type.equals("fixed", ignoreCase = true)) {
            val toTake = value.toBigDecimal(MathContext(3))
            if (user.hasEnough(toTake)) {
                user.withdrawCoins(toTake)
            } else {
                user.coins(BigDecimal.ZERO)
            }
        }
    }
}