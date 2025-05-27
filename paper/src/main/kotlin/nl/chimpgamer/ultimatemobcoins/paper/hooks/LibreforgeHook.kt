package nl.chimpgamer.ultimatemobcoins.paper.hooks

import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.effects.templates.MultiplierEffect
import com.willfp.libreforge.toDispatcher
import kotlinx.coroutines.delay
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.events.MobCoinsReceiveEvent
import nl.chimpgamer.ultimatemobcoins.paper.events.PrepareMobCoinDropEvent
import org.bukkit.event.EventHandler

class LibreforgeHook(plugin: UltimateMobCoinsPlugin) : PluginHook(plugin, "libreforge") {

    override fun load() {
        if (!isPluginLoaded() || !shouldHook()) return
        plugin.launch {
            delay(10.ticks)

            Effects.register(EffectMobCoinsDropMultiplier)
            Effects.register(EffectMobCoinsChanceMultiplier)

            isLoaded = true
            plugin.logger.info("Successfully loaded $pluginName hook!")
        }
    }
}

object EffectMobCoinsChanceMultiplier : MultiplierEffect("mob_coins_chance_multiplier") {
    @EventHandler(ignoreCancelled = true)
    fun handle(event: PrepareMobCoinDropEvent) {
        event.mobCoin.dropChance *= getMultiplier(event.player.toDispatcher())
    }
}

object EffectMobCoinsDropMultiplier : MultiplierEffect("mob_coins_drop_multiplier") {
    @EventHandler(ignoreCancelled = true)
    fun handle(event: MobCoinsReceiveEvent) {
        event.amount = event.amount.multiply(getMultiplier(event.player.toDispatcher()).toBigDecimal())
    }
}