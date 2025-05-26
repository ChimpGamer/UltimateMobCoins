package nl.chimpgamer.ultimatemobcoins.paper

import com.github.shynixn.mccoroutine.folia.*
import io.github.rysefoxx.inventory.plugin.pagination.InventoryManager
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatemobcoins.paper.configurations.HooksConfig
import nl.chimpgamer.ultimatemobcoins.paper.configurations.MessagesConfig
import nl.chimpgamer.ultimatemobcoins.paper.configurations.SettingsConfig
import nl.chimpgamer.ultimatemobcoins.paper.extensions.registerEvents
import nl.chimpgamer.ultimatemobcoins.paper.extensions.registerSuspendingEvents
import nl.chimpgamer.ultimatemobcoins.paper.listeners.*
import nl.chimpgamer.ultimatemobcoins.paper.managers.CloudCommandManager
import nl.chimpgamer.ultimatemobcoins.paper.managers.SQLDatabaseManager
import nl.chimpgamer.ultimatemobcoins.paper.managers.MobCoinManager
import nl.chimpgamer.ultimatemobcoins.paper.managers.UserManager
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import java.math.BigDecimal
import nl.chimpgamer.ultimatemobcoins.paper.managers.*
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.Menu
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.action.ActionType
import nl.chimpgamer.ultimatemobcoins.paper.utils.LogWriter
import nl.chimpgamer.ultimatemobcoins.paper.utils.updatechecker.ModrinthUpdateChecker
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.player.*
import java.io.File
import java.nio.file.Files
import java.time.Duration
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.minutes

class UltimateMobCoinsPlugin : SuspendingJavaPlugin() {
    private val bstatsId = 19914

    val shopsFolder = dataFolder.resolve("shops")
    val shopMenus = HashMap<String, Menu>()

    val settingsConfig = SettingsConfig(this)
    val messagesConfig = MessagesConfig(this)
    val hooksConfig = HooksConfig(this)

    val databaseManager = if (settingsConfig.storageType == "mongodb") MongoDBManager(this) else SQLDatabaseManager(this)
    val userManager = UserManager(this)
    val mobCoinsManager = MobCoinManager(this)
    val spinnerManager = SpinnerManager(this)
    val cloudCommandManager = CloudCommandManager(this)
    val hookManager = HookManager(this)
    val leaderboardManager = LeaderboardManager(this)
    private val inventoryManager = InventoryManager(this)

    val logWriter = LogWriter(this)

    val lootingEnchantment: Enchantment = try {
        Enchantment.LOOT_BONUS_MOBS
    } catch (ex: NoSuchFieldError) {
        Enchantment.getByKey(NamespacedKey.minecraft("looting"))!!
    }

    val modrinthUpdateChecker = ModrinthUpdateChecker(this)

    var buildNumber: String = ""
    var buildDate: String = ""

    override fun onLoad() {
        loadPluginInfo()
        hookManager.loadWorldGuard()
    }

    override fun onEnable() {
        inventoryManager.invoke()

        databaseManager.initialize()
        mobCoinsManager.loadMobCoins()
        userManager.initialize()

        cloudCommandManager.initialize()
        cloudCommandManager.loadCommands()

        val plugin = this
        val eventDispatcher = mapOf<Class<out Event>, (event: Event) -> CoroutineContext>(
            Pair(MCCoroutineExceptionEvent::class.java) {
                require(it is MCCoroutineExceptionEvent)
                plugin.globalRegionDispatcher
            },
            Pair(PlayerQuitEvent::class.java) {
                require(it is PlayerQuitEvent)
                entityDispatcher(it.player)
            },
            Pair(PlayerAttemptPickupItemEvent::class.java) {
                require(it is PlayerAttemptPickupItemEvent)
                entityDispatcher(it.player)
            },
            Pair(InventoryPickupItemEvent::class.java) {
                require(it is InventoryPickupItemEvent)
                entityDispatcher(it.item)
            },
            Pair(PlayerInteractEvent::class.java) {
                require(it is PlayerInteractEvent)
                entityDispatcher(it.player)
            },
            Pair(EntityDeathEvent::class.java) {
                require(it is EntityDeathEvent)
                entityDispatcher(it.entity)
            },
            Pair(ItemSpawnEvent::class.java) {
                require(it is ItemSpawnEvent)
                entityDispatcher(it.entity)
            },
            Pair(AsyncPlayerPreLoginEvent::class.java) {
                require(it is AsyncPlayerPreLoginEvent)
                asyncDispatcher
            },
            Pair(EntitySpawnEvent::class.java) {
                require(it is EntitySpawnEvent)
                entityDispatcher(it.entity)
            },
            Pair(PlayerDeathEvent::class.java) {
                require(it is PlayerDeathEvent)
                entityDispatcher(it.player)
            },
            Pair(PlayerJoinEvent::class.java) {
                require(it is PlayerJoinEvent)
                entityDispatcher(it.player)
            },
        )

        registerEvents(
            FireworkListener(),
            hookManager
        )
        registerSuspendingEvents(
            EntityListener(this),
            ConnectionListener(this),
            ItemPickupListener(this),
            PlayerInteractListener(this),
            PlayerDeathListener(this),
            eventDispatcher = eventDispatcher
        )

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

        loadMenus()

        val metrics = Metrics(this, bstatsId)
        metrics.addCustomChart(SimplePie("storage_type") { settingsConfig.storageType.lowercase() })

        plugin.launch(plugin.asyncDispatcher, CoroutineStart.UNDISPATCHED) {
            modrinthUpdateChecker.checkUpdate()

            delay(1.ticks)
            modrinthUpdateChecker.notifyAboutUpdate()

            while (true) {
                delay(5.minutes)
                saveShopItemsData()
            }
        }
        if (settingsConfig.mobCoinsLeaderboardEnabled) {
            leaderboardManager.start()
        }
    }

    override fun onDisable() {
        closeMenus()
        saveShopItemsData()

        hookManager.unload()
        leaderboardManager.stop()
        HandlerList.unregisterAll(this)
        databaseManager.close()
    }

    suspend fun reload() {
        withContext(globalRegionDispatcher) {
            closeMenus()
        }

        settingsConfig.reload()
        messagesConfig.config.reload()
        mobCoinsManager.reload()
        spinnerManager.reload()

        if (settingsConfig.mobCoinsLeaderboardEnabled) {
            leaderboardManager.start()
        }
    }

    fun loadMenus() {
        val loadedMenus = HashMap<String, Menu>()
        shopsFolder.listFiles { _, name -> name.endsWith(".yml") }
            ?.forEach { file -> loadMenu(file)?.let { loadedMenus[file.nameWithoutExtension] = it } }
        shopMenus.clear()
        shopMenus.putAll(loadedMenus)

        logger.info("Loaded ${loadedMenus.size} shop menus!")
    }

    private fun loadMenu(file: File): Menu? {
        try {
            return Menu(this, file)
        } catch (ex: Exception) {
            logger.log(Level.SEVERE, "Invalid Configuration! '${file.absolutePath}' has a invalid configuration.", ex)
        }
        return null
    }

    private fun getDropAmountPermissionMultiplier(player: Player): Double {
        val permission = "ultimatemobcoins.multiplier.dropamount."
        val multipliers = player.effectivePermissions
            .filter { it.permission.startsWith(permission, ignoreCase = true) && it.value }
            .mapNotNull { it.permission.substring(permission.length).toDoubleOrNull() }
        return multipliers.maxOrNull() ?: 0.0
    }

    private fun getDropChanceMultiplierFromPermission(player: Player): Double {
        val permission = "ultimatemobcoins.multiplier.dropchance."
        val multipliers = player.effectivePermissions
            .filter { it.permission.startsWith(permission, ignoreCase = true) && it.value }
            .mapNotNull { it.permission.substring(permission.length).toDoubleOrNull() }
        return multipliers.maxOrNull() ?: 0.0
    }

    fun applyDropChanceMultiplier(player: Player, dropChance: Double): Double {
        val multiplier = hookManager.getMobCoinDropChanceMultiplier(player) + getDropChanceMultiplierFromPermission(player)
        return dropChance + (dropChance * multiplier)
    }

    fun applyMultiplier(dropAmount: BigDecimal, multiplier: Double) = dropAmount.plus(dropAmount.multiply(multiplier.toBigDecimal()))

    fun getMobCoinDropsMultiplier(player: Player) = hookManager.getMobCoinDropsMultiplier(player) + getDropAmountPermissionMultiplier(player)

    fun closeMenus() = shopMenus.values.forEach { it.inventory.closeAll() }

    fun saveShopItemsData() = shopMenus.values.forEach { it.saveShopItemsData() }

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

    fun getRemainingTimeTagResolver(): TagResolver {
        return TagResolver.resolver("shop_refresh_time") { argumentQueue: ArgumentQueue, _: Context? ->
            val shopName = argumentQueue.popOr("shop_refresh_time tag requires a valid rotating shop name.").value()
            val menu = shopMenus[shopName] ?: return@resolver null

            Tag.preProcessParsed(formatDuration(menu.getTimeRemaining()))
        }
    }

    fun debug(message: () -> Any) {
        if (settingsConfig.debug) {
            message().run {
                if (this is Component) {
                    componentLogger.info(this)
                } else {
                    logger.info(this.toString())
                }
            }
        }
    }

    private fun loadPluginInfo() {
        getResource("plugin.yml")?.let {
            it.reader().use { reader ->
                val pluginYml = YamlConfiguration.loadConfiguration(reader)
                buildNumber = pluginYml.getString("build-number") ?: ""
                buildDate = pluginYml.getString("build-date") ?: ""
            }
        }
    }

    @Suppress("DEPRECATION")
    val version get() = description.version

    @Suppress("DEPRECATION")
    val authors: List<String> get() = description.authors
}