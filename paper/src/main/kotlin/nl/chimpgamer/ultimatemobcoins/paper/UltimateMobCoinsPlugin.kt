package nl.chimpgamer.ultimatemobcoins.paper

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import io.github.rysefoxx.inventory.plugin.pagination.InventoryManager
import nl.chimpgamer.ultimatemobcoins.paper.configurations.MessagesConfig
import nl.chimpgamer.ultimatemobcoins.paper.configurations.SettingsConfig
import nl.chimpgamer.ultimatemobcoins.paper.extensions.registerEvents
import nl.chimpgamer.ultimatemobcoins.paper.extensions.runSync
import nl.chimpgamer.ultimatemobcoins.paper.listeners.*
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
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
import java.io.File
import java.nio.file.Files
import java.time.Duration

class UltimateMobCoinsPlugin : JavaPlugin() {
    private val bstatsId = 19914

    val shopsFolder = dataFolder.resolve("shops")
    val shopMenus = HashMap<String, Menu>()

    val settingsConfig = SettingsConfig(this)
    val messagesConfig = MessagesConfig(this)

    val databaseManager = DatabaseManager(this)
    val userManager = UserManager(this)
    val mobCoinsManager = MobCoinManager(this)
    val spinnerManager = SpinnerManager(this)
    val cloudCommandManager = CloudCommandManager(this)

    val hookManager = HookManager(this)
    private val inventoryManager = InventoryManager(this)

    override fun onLoad() {
        hookManager.loadWorldGuard()
    }

    override fun onEnable() {
        inventoryManager.invoke()

        databaseManager.initialize()
        mobCoinsManager.loadMobCoins()
        userManager.initialize()

        cloudCommandManager.initialize()
        cloudCommandManager.loadCommands()

        registerEvents(
            EntityListener(this),
            FireworkListener(),
        )
        server.pluginManager.registerSuspendingEvents(ConnectionListener(this), this)
        server.pluginManager.registerSuspendingEvents(ItemPickupListener(this), this)
        server.pluginManager.registerSuspendingEvents(PlayerInteractListener(this), this)

        hookManager.load()

        if (!Files.isDirectory(shopsFolder.toPath())) {
            Files.createDirectory(shopsFolder.toPath())
            val shopFiles = listOf(
                "main_menu.yml",
                "rotating_shop.yml",
                "shop.yml"
            )
            for (shopFile in shopFiles) {
                val inJarPath = "shops/$shopFile"
                getResource(inJarPath)?.let { Files.copy(it, shopsFolder.resolve(shopFile).toPath()) }
            }
        }

        ActionType.initialize(this)

        val loadedMenus = HashMap<String, Menu>()
        shopsFolder.listFiles { _, name -> name.endsWith(".yml") }
            ?.forEach { file -> loadMenu(file)?.let { loadedMenus[file.nameWithoutExtension] = it } }
        shopMenus.clear()
        shopMenus.putAll(loadedMenus)

        val metrics = Metrics(this, bstatsId)
        metrics.addCustomChart(SimplePie("storage_type") { settingsConfig.storageType.lowercase() })
    }

    override fun onDisable() {
        closeMenus()
        hookManager.unload()
        HandlerList.unregisterAll(this)
        databaseManager.close()
    }

    fun reload() {
        runSync { closeMenus() }

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

    private fun getPermissionMultiplier(player: Player): Double {
        val permission = "ultimatemobcoins.multiplier."
        val multipliers = player.effectivePermissions
            .filter { it.permission.startsWith(permission, ignoreCase = true) && it.value }
            .mapNotNull { it.permission.substring(permission.length).toDoubleOrNull() }
        return multipliers.maxOrNull() ?: 0.0
    }

    fun applyMultiplier(player: Player, dropAmount: BigDecimal): BigDecimal {
        val multiplier = hookManager.getMobCoinMultiplier(player) + getPermissionMultiplier(player)
        return dropAmount.plus(dropAmount.multiply(multiplier.toBigDecimal()))
    }

    fun closeMenus() = shopMenus.values.forEach { it.inventory.closeAll() }

    fun formatDuration(duration: Duration): String {
        var result = ""
        val daysPart = duration.toDaysPart()
        val hoursPart = duration.toHoursPart()
        val minutesPart = duration.toMinutesPart()
        val secondsPart = duration.toSecondsPart()
        if (daysPart > 0) {
            result += if (daysPart > 1) {
                "$daysPart ${messagesConfig.timeUnitDays} "
            } else {
                "$daysPart ${messagesConfig.timeUnitDay} "
            }
        }
        if (hoursPart > 0) {
            result += if (hoursPart > 1) {
                "$hoursPart ${messagesConfig.timeUnitHours} "
            } else {
                "$hoursPart ${messagesConfig.timeUnitHour} "
            }
        }
        if (minutesPart > 0) {
            result += if (minutesPart > 1) {
                "$minutesPart ${messagesConfig.timeUnitMinutes} "
            } else {
                "$minutesPart ${messagesConfig.timeUnitMinute} "
            }
        }
        if (secondsPart > 0) {
            result += if (secondsPart > 1) {
                "$secondsPart ${messagesConfig.timeUnitSeconds} "
            } else {
                "$secondsPart ${messagesConfig.timeUnitSecond} "
            }
        }
        return result.trim().ifEmpty { "0 ${messagesConfig.timeUnitSeconds}" }
    }


    @Suppress("DEPRECATION")
    val version get() = description.version

    @Suppress("DEPRECATION")
    val authors: List<String> get() = description.authors
}