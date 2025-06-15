package nl.chimpgamer.ultimatemobcoins.paper.hooks

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.effects.templates.MultiplierEffect
import com.willfp.libreforge.getDoubleFromExpression
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import kotlinx.coroutines.delay
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.events.MobCoinsReceiveEvent
import nl.chimpgamer.ultimatemobcoins.paper.events.PrepareMobCoinDropEvent
import org.bukkit.event.EventHandler
import java.math.MathContext

class LibreforgeHook(plugin: UltimateMobCoinsPlugin) : PluginHook(plugin, "libreforge") {

    override fun load() {
        if (!isPluginLoaded() || !shouldHook()) return
        plugin.launch {
            delay(10.ticks)

            Effects.register(EffectMobCoinsDropMultiplier)
            Effects.register(EffectMobCoinsChanceMultiplier)

            Effects.register(EffectGiveMobCoins(plugin))
            Effects.register(EffectTakeMobCoins(plugin))

            isLoaded = true
            plugin.logger.info("Successfully loaded $pluginName hook!")
        }
    }
}

class EffectGiveMobCoins(private val plugin: UltimateMobCoinsPlugin) : Effect<NoCompileData>("give_mob_coins") {

    override val parameters = setOf(
        TriggerParameter.PLAYER
    )

    override val arguments = arguments {
        require("amount", "You must specify the amount of mobcoins to give!")
    }

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false
        val user = plugin.userManager.getIfLoaded(player) ?: return false
        val amount = config.getDoubleFromExpression("amount", data)
        plugin.launch(plugin.asyncDispatcher) {
            user.depositCoins(amount.toBigDecimal(MathContext(3)))
        }
        return true
    }
}

class EffectTakeMobCoins(private val plugin: UltimateMobCoinsPlugin) : Effect<NoCompileData>("take_mob_coins") {

    override val parameters = setOf(
        TriggerParameter.PLAYER
    )

    override val arguments = arguments {
        require("amount", "You must specify the amount of mobcoins to take!")
    }

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false
        val user = plugin.userManager.getIfLoaded(player) ?: return false
        val amount = config.getDoubleFromExpression("amount", data)
        plugin.launch(plugin.asyncDispatcher) {
            user.withdrawCoins(amount.toBigDecimal(MathContext(3)))
        }
        return true
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