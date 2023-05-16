package nl.chimpgamer.ultimatemobcoins.paper

import io.github.rysefoxx.inventory.plugin.pagination.InventoryManager
import nl.chimpgamer.ultimatemobcoins.paper.configurations.MessagesConfig
import nl.chimpgamer.ultimatemobcoins.paper.configurations.SettingsConfig
import nl.chimpgamer.ultimatemobcoins.paper.extensions.registerEvents
import nl.chimpgamer.ultimatemobcoins.paper.listeners.EntityListener
import nl.chimpgamer.ultimatemobcoins.paper.listeners.FireworkListener
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
import nl.chimpgamer.ultimatemobcoins.paper.managers.*
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.Menu
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.action.ActionType
import java.io.File

class UltimateMobCoinsPlugin : JavaPlugin() {
    val shopsFolder = dataFolder.resolve("shops")
    val shopMenus: MutableMap<String, Menu> = HashMap()

    val settingsConfig = SettingsConfig(this)
    val messagesConfig = MessagesConfig(this)

    val databaseManager = DatabaseManager(this)
    val userManager = UserManager(this)
    val mobCoinsManager = MobCoinManager(this)
    val spinnerManager = SpinnerManager(this)
    val cloudCommandManager = CloudCommandManager(this)

    private val hookManager = HookManager(this)
    private val inventoryManager = InventoryManager(this)

    override fun onEnable() {
        inventoryManager.invoke()

        databaseManager.initialize()
        mobCoinsManager.loadMobCoins()

        cloudCommandManager.initialize()
        cloudCommandManager.loadCommands()

        registerEvents(
            EntityListener(this),
            FireworkListener(),
            ItemPickupListener(this),
            PlayerListener(this)
        )

        ActionType.initialize(this)

        val loadedMenus = HashMap<String, Menu>()
        shopsFolder.listFiles { _, name -> name.endsWith(".yml") }
            ?.forEach { file -> loadMenu(file)?.let { loadedMenus[file.nameWithoutExtension] = it } }
        shopMenus.clear()
        shopMenus.putAll(loadedMenus)
        hookManager.load()
    }

    override fun onDisable() {
        HandlerList.unregisterAll(this)
        closeMenus()
        hookManager.unload()
    }

    fun reload() {
        closeMenus()

        settingsConfig.config.reload()
        messagesConfig.config.reload()
        mobCoinsManager.reload()
        spinnerManager.reload()

        val loadedShopMenus = HashMap<String, Menu>()
        shopsFolder.listFiles { _, name -> name.endsWith(".yml") }
            ?.forEach { file -> loadMenu(file)?.let { loadedShopMenus[file.nameWithoutExtension] = it } }
        shopMenus.clear()
        shopMenus.putAll(loadedShopMenus)
    }

    private fun loadMenu(file: File): Menu? {
        try {
            return Menu(this, file)
        } catch (ex: Exception) {
            logger.severe("Invalid Configuration! '${file.absolutePath}' has a invalid configuration. Cause: ${ex.localizedMessage}")
        }
        return null
    }

    private fun getMultiplier(player: Player): Double {
        val permission = "ultimatemobcoins.multiplier."
        val multipliers = player.effectivePermissions
            .filter { it.permission.startsWith(permission, ignoreCase = true) && it.value }
            .mapNotNull { it.permission.substring(permission.length).toDoubleOrNull() }
        return multipliers.maxOrNull() ?: 0.0
    }

    fun applyMultiplier(player: Player, dropAmount: BigDecimal): BigDecimal {
        val multiplier = getMultiplier(player).toBigDecimal()
        return dropAmount.plus(dropAmount.multiply(multiplier.divide(BigDecimal(100))))
    }

    fun closeMenus() = shopMenus.values.forEach { it.inventory.closeAll() }

    @Suppress("DEPRECATION")
    val version get() = description.version

    @Suppress("DEPRECATION")
    val authors: List<String> get() = description.authors
}