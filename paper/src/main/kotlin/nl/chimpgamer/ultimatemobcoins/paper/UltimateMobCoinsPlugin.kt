package nl.chimpgamer.ultimatemobcoins.paper

import nl.chimpgamer.ultimatemobcoins.paper.configurations.MessagesConfig
import nl.chimpgamer.ultimatemobcoins.paper.configurations.SettingsConfig
import nl.chimpgamer.ultimatemobcoins.paper.extensions.registerEvents
import nl.chimpgamer.ultimatemobcoins.paper.hooks.PlaceholderAPIHook
import nl.chimpgamer.ultimatemobcoins.paper.listeners.EntityListener
import nl.chimpgamer.ultimatemobcoins.paper.listeners.ItemPickupListener
import nl.chimpgamer.ultimatemobcoins.paper.listeners.PlayerListener
import nl.chimpgamer.ultimatemobcoins.paper.managers.CloudCommandManager
import nl.chimpgamer.ultimatemobcoins.paper.managers.DatabaseManager
import nl.chimpgamer.ultimatemobcoins.paper.managers.MobCoinManager
import nl.chimpgamer.ultimatemobcoins.paper.managers.UserManager
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import java.math.BigDecimal

class UltimateMobCoinsPlugin : JavaPlugin() {

    val settingsConfig = SettingsConfig(this)
    val messagesConfig = MessagesConfig(this)

    val databaseManager = DatabaseManager(this)
    val userManager = UserManager(this)
    val mobCoinsManager = MobCoinManager(this)
    val cloudCommandManager = CloudCommandManager(this)

    private lateinit var placeholderAPIHook: PlaceholderAPIHook

    override fun onEnable() {
        databaseManager.initialize()
        mobCoinsManager.loadMobCoins()

        cloudCommandManager.initialize()
        cloudCommandManager.loadCommands()

        registerEvents(
            EntityListener(this),
            ItemPickupListener(this),
            PlayerListener(this)
        )

        if (server.pluginManager.isPluginEnabled("PlaceholderAPI'")) {
            placeholderAPIHook = PlaceholderAPIHook(this)
            placeholderAPIHook.register()
        }
    }

    override fun onDisable() {
        HandlerList.unregisterAll(this)
        if (this::placeholderAPIHook.isInitialized) {
            placeholderAPIHook.unregister()
        }
    }

    private fun getMultiplier(player: Player): Double {
        val multipliers = player.effectivePermissions
            .filter { it.permission.lowercase().startsWith("ultimatemobcoins.multiplier.") && it.value }
            .mapNotNull { it.permission.lowercase().replace("ultimatemobcoins.multiplier.", "").toDoubleOrNull() }
        return multipliers.maxOrNull() ?: 0.0
    }

    fun applyMultiplier(player: Player, dropAmount: BigDecimal): BigDecimal {
        val multiplier = getMultiplier(player).toBigDecimal()
        return dropAmount.plus(dropAmount.multiply(multiplier.divide(BigDecimal(100))))
    }

    @Suppress("DEPRECATION")
    val version get() = description.version

    @Suppress("DEPRECATION")
    val authors: List<String> get() = description.authors
}