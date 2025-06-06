package nl.chimpgamer.ultimatemobcoins.paper.models.menu

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.block.implementation.Section
import io.github.rysefoxx.inventory.plugin.content.InventoryContents
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.configurations.MenuConfig
import nl.chimpgamer.ultimatemobcoins.paper.utils.ItemUtils
import org.bukkit.entity.Player
import java.io.IOException
import java.time.Instant
import java.util.UUID
import java.util.logging.Level
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists

class RotatingShopMenu(plugin: UltimateMobCoinsPlugin, config: MenuConfig) : RefreshableShopMenu(plugin, config) {
    val allRotatingShopItems by lazy { menuItems.filter { it.isRotatingShopItem }.toSet() }
    val currentShopItems = HashSet<MenuItem>()

    override val provider = object : InventoryProvider {
        override fun init(player: Player, contents: InventoryContents) {
            val user = plugin.userManager.getIfLoaded(player) ?: return
            val vaultHook = plugin.hookManager.vaultHook

            (menuItems.filterNot { it.isRotatingShopItem } + currentShopItems).forEach { item ->
                val itemStack = item.itemStack?.clone() ?: return@forEach
                val position = item.position

                val tagResolver = getItemPlaceholders(user, item)

                ItemUtils.updateItem(itemStack, player, tagResolver)

                val intelligentItem = createIntelligentItem(player, user, item, vaultHook, contents, itemStack)
                if (position != -1) {
                    contents.set(position - 1, intelligentItem)
                } else {
                    contents.add(intelligentItem)
                }
            }

            applyFillerItem(contents, player)
        }

        override fun update(player: Player, contents: InventoryContents) {
            if (timeToRefresh()) {
                refresh()
            }
            val user = plugin.userManager.getIfLoaded(player) ?: return
            val vaultHook = plugin.hookManager.vaultHook

            val items = (menuItems.filterNot { item -> item.position == -1 && allRotatingShopItems.contains(item) } + currentShopItems)
            updateItemsInMenu(contents, items, player, user, vaultHook)
        }

        override fun close(player: Player, inventory: RyseInventory) {
            closingSound?.play(player)
        }
    }

    override fun refresh() {
        refreshShopItems()
        resetTimeRemaining()
    }

    override fun refreshShopItems() {
        currentShopItems.clear()
        val shopSlots = config.getIntList("shop_slots")
        plugin.debug { "[${file.name}] shopSlots=$shopSlots" }
        val shopItems = this.allRotatingShopItems.filter { it.success }.map { it.clone() }.toMutableList()
        for (slot in shopSlots) {
            if (shopItems.isEmpty()) break // If there are no shopItems left anymore, break the loop
            val shopItem = shopItems.random()
            shopItem.position = slot
            shopItems.remove(shopItem)
            this.currentShopItems.add(shopItem)
        }
    }

    fun saveShopItemsData() {
        if (currentShopItems.isEmpty()) return
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

            currentShopItems.forEach { shopItem ->
                shopItem.stock?.let { itemsDataSection.set("${shopItem.name}.stock", it) }
                itemsDataSection.set("${shopItem.name}.purchaseLimits", shopItem.purchaseLimits)
                itemsDataSection.set("${shopItem.name}.position", shopItem.position)
            }

            config.save()
        } catch (_: IOException) {
            plugin.logger.log(Level.SEVERE, "Something went wrong trying to create data file for shop ${file.name}")
        }
    }

    private fun getItemsFromLastShopData(): Boolean {
        if (currentShopItems.isNotEmpty()) return false
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
            currentShopItems.clear()
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
                currentShopItems.add(item)
            }

            plugin.logger.info("Loaded ${currentShopItems.size} items from ${shopDataFile.fileName}")
            return true
        } catch (ex: IOException) {
            plugin.logger.log(Level.SEVERE, "Something went wrong trying to create data file for shop ${file.name}", ex)
            return false
        }
    }

    init {
        loadAllItems()

        if (!getItemsFromLastShopData()) {
            refreshShopItems()
        }

        buildInventory()
    }
}