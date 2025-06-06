package nl.chimpgamer.ultimatemobcoins.paper.models.menu

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.block.implementation.Section
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.configurations.MenuConfig
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.logging.Level
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists

abstract class RefreshableShopMenu(plugin: UltimateMobCoinsPlugin, config: MenuConfig) : Menu(plugin, config) {

    // Used when Shop is a rotating shop
    protected lateinit var refreshTime: Instant

    fun hasResetTimer(): Boolean = ::refreshTime.isInitialized

    fun timeToRefresh(): Boolean = hasResetTimer() && Instant.now().isAfter(refreshTime)

    fun getTimeRemaining(): Duration {
        val now = Instant.now()
        return if (now.isBefore(refreshTime)) {
            Duration.between(now, refreshTime)
        } else {
            Duration.ZERO
        }
    }

    fun resetTimeRemaining() {
        val refreshTime = config.getLong("refresh_time")
        if (refreshTime == null || refreshTime <= 0) return
        this.refreshTime = Instant.now().plusSeconds(refreshTime)
    }

    abstract fun refresh()
    abstract fun refreshShopItems()
    abstract fun saveShopItemsData()

    fun saveShopItemsData(items: Collection<MenuItem>) {
        if (items.isEmpty()) return
        if (!hasResetTimer()) return
        val shopDataFolder = plugin.dataFolder.toPath().resolve("data")
        if (!shopDataFolder.isDirectory()) {
            shopDataFolder.createDirectory()
        }
        val shopDataFile = shopDataFolder.resolve("${file.nameWithoutExtension}-data.yml")
        if (shopDataFile.notExists()) {
            shopDataFile.createFile()
        }
        try {
            val config = YamlDocument.create(shopDataFile.toFile())

            config.set("refresh-time", refreshTime.toEpochMilli())

            val itemsDataSection = config.createSection("items-data")

            items.forEach { shopItem ->
                shopItem.stock?.let { itemsDataSection.set("${shopItem.name}.stock", it) }
                itemsDataSection.set("${shopItem.name}.purchaseLimits", shopItem.purchaseLimits)
                itemsDataSection.set("${shopItem.name}.position", shopItem.position)
            }

            config.save()
        } catch (_: IOException) {
            plugin.logger.log(Level.SEVERE, "Something went wrong trying to create data file for shop ${file.name}")
        }
    }

    protected fun getItemsFromLastShopData(items: MutableCollection<MenuItem>): Boolean {
        if (items.isNotEmpty()) return false
        val shopDataFolder = plugin.dataFolder.toPath().resolve("data")
        if (!shopDataFolder.isDirectory()) {
            return false
        }
        val shopDataFile = shopDataFolder.resolve("${file.nameWithoutExtension}-data.yml")
        if (shopDataFile.notExists()) {
            return false
        }
        try {
            val config = YamlDocument.create(shopDataFile.toFile())

            val refreshTime = Instant.ofEpochMilli(config.getLong("refresh-time"))
            if (refreshTime.isBefore(Instant.now())) {
                return false
            }
            this.refreshTime = refreshTime

            val itemsDataSection = config.getSection("items-data")
            if (itemsDataSection.isEmpty(false)) return false
            items.clear()
            itemsDataSection.getStringRouteMappedValues(false).forEach { (itemName, section) ->
                val item = getItem(itemName)?.clone() ?: return@forEach
                if (section is Section) {
                    val stock = section.getInt("stock", null)
                    val purchaseLimits = section.getSection("purchaseLimits")
                        .getStringRouteMappedValues(false).entries
                        .associate { UUID.fromString(it.key) to it.value as Int }.toMutableMap()
                    val position = section.getInt("position")

                    item.stock = stock
                    item.purchaseLimits = purchaseLimits
                    item.position = position
                }
                items.add(item)
            }

            plugin.logger.info("Loaded ${items.size} items from ${shopDataFile.fileName}")
            return true
        } catch (ex: IOException) {
            plugin.logger.log(Level.SEVERE, "Something went wrong trying to create data file for shop ${file.name}", ex)
            return false
        }
    }

    init {
        resetTimeRemaining()
    }
}