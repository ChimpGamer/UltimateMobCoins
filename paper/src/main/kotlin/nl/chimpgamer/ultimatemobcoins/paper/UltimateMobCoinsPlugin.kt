package nl.chimpgamer.ultimatemobcoins.paper

import nl.chimpgamer.ultimatemobcoins.paper.configurations.MessagesConfig
import nl.chimpgamer.ultimatemobcoins.paper.configurations.SettingsConfig
import nl.chimpgamer.ultimatemobcoins.paper.extensions.registerEvents
import nl.chimpgamer.ultimatemobcoins.paper.listeners.EntityListener
import nl.chimpgamer.ultimatemobcoins.paper.listeners.ItemPickupListener
import nl.chimpgamer.ultimatemobcoins.paper.listeners.PlayerListener
import nl.chimpgamer.ultimatemobcoins.paper.managers.CloudCommandManager
import nl.chimpgamer.ultimatemobcoins.paper.managers.DatabaseManager
import nl.chimpgamer.ultimatemobcoins.paper.managers.MobCoinManager
import nl.chimpgamer.ultimatemobcoins.paper.managers.UserManager
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

class UltimateMobCoinsPlugin : JavaPlugin() {

    val settingsConfig = SettingsConfig(this)
    val messagesConfig = MessagesConfig(this)

    val databaseManager = DatabaseManager(this)
    val userManager = UserManager(this)
    val mobCoinsManager = MobCoinManager(this)
    val cloudCommandManager = CloudCommandManager(this)

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
    }

    override fun onDisable() {
        HandlerList.unregisterAll()
    }
}