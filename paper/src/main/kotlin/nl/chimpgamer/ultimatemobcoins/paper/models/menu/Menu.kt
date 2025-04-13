package nl.chimpgamer.ultimatemobcoins.paper.models.menu

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.block.implementation.Section
import io.github.rysefoxx.inventory.plugin.content.IntelligentItem
import io.github.rysefoxx.inventory.plugin.content.InventoryContents
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider
import io.github.rysefoxx.inventory.plugin.enums.TimeSetting
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import kotlinx.coroutines.CoroutineStart
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.configurations.AbstractMenuConfig
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.hooks.VaultHook
import nl.chimpgamer.ultimatemobcoins.paper.models.ConfigurableSound
import nl.chimpgamer.ultimatemobcoins.paper.models.User
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.action.Action
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.action.ActionType
import nl.chimpgamer.ultimatemobcoins.paper.utils.ItemUtils
import nl.chimpgamer.ultimatemobcoins.paper.utils.Utils
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.logging.Level
import kotlin.collections.HashSet
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists

class Menu(private val plugin: UltimateMobCoinsPlugin, private val file: File) : AbstractMenuConfig(plugin, file) {

    private var title: String? = null
        get() = if (field == null) file.nameWithoutExtension else field
        set(value) {
            field = value ?: file.nameWithoutExtension
        }
    val menuType: MenuType
    val permission: String?

    var closeOnClick: Boolean = false
    private var updateInterval: Int
    private var inventorySize: Int

    private var openingSound: ConfigurableSound? = null
    private var closingSound: ConfigurableSound? = null

    lateinit var inventory: RyseInventory

    val allMenuItems = HashSet<MenuItem>()

    val filler by lazy { getItem("filler") }

    // Used hen Shop is a rotating shop
    val allShopItems by lazy { HashSet<MenuItem>() }
    val shopItems by lazy { HashSet<MenuItem>() }
    private lateinit var refreshTime: Instant

    private fun getItem(name: String) = allMenuItems.find { it.name.equals(name, ignoreCase = true) }

    private fun loadAllItems() {
        allMenuItems.clear()
        val section = config.getSection("items")
        if (section != null) {
            for (key in section.keys) {
                val menuItem = loadMenuItem(section, key.toString())
                if (menuItem != null) {
                    allMenuItems.add(menuItem)
                }
            }
        }
    }

    fun loadMenuItem(section: Section, name: String): MenuItem? {
        val itemSection = section.getSection(name)
        if (itemSection == null) {
            println("$name does not exist in the config")
            return null
        }
        val menuitem = MenuItem(name)
        menuitem.itemStack = try {
            ItemUtils.itemDataToItemStack(plugin, itemSection.getStringList("item"))
        } catch (ex: Exception) {
            plugin.logger.log(Level.SEVERE, "Invalid Configuration! Menu item $name in '${file.absolutePath}' is invalid.", ex)
            return null
        }
        if (itemSection.contains("position")) {
            menuitem.position = itemSection.getInt("position")
        }
        if (itemSection.contains("message")) {
            menuitem.message = itemSection.getString("message")
        }
        if (itemSection.contains("permission")) {
            menuitem.permission = itemSection.getString("permission")
        }
        if (itemSection.contains("price")) {
            menuitem.price = itemSection.getDouble("price")
        }
        if (itemSection.contains("price_vault")) {
            menuitem.priceVault = itemSection.getDouble("price_vault")
        }
        if (itemSection.contains("stock")) {
            menuitem.stock = itemSection.getInt("stock")
        }
        if (itemSection.contains("purchase_limit")) {
            menuitem.purchaseLimit = itemSection.getInt("purchase_limit")
        }
        if (itemSection.contains("chance")) {
            menuitem.chance = itemSection.getInt("chance")
        }
        if (itemSection.contains("actions")) {
            val actionsList = itemSection.getStringList("actions")
            actionsList.forEach { actionStr ->
                val actionType = ActionType.findActionType(actionStr)
                if (actionType == null) {
                    plugin.logger.severe("$actionStr does not have a valid action type!")
                    return@forEach
                }
                val action = actionStr.replaceFirst(Utils.actionTypeRegex, "").trim()
                menuitem.actions.add(Action(actionType, action))
            }
        }
        return menuitem
    }

    fun getTimeRemaining(): Duration {
        val now = Instant.now()
        return if (now.isBefore(refreshTime)) {
            Duration.between(now, refreshTime)
        } else {
            Duration.ZERO
        }
    }

    private fun buildInventory() {
        inventory = RyseInventory.builder()
            .provider(object : InventoryProvider {
                override fun init(player: Player, contents: InventoryContents) {
                    val user = plugin.userManager.getIfLoaded(player) ?: return
                    val vaultHook = plugin.hookManager.vaultHook

                    val menuItems = if (menuType === MenuType.ROTATING_SHOP) {
                        buildSet {
                            addAll(allMenuItems.filterNot { allShopItems.contains(it) })
                            addAll(shopItems)
                        }
                    } else {
                        allMenuItems
                    }

                    menuItems.forEach { item ->
                        val itemStack = item.itemStack?.clone() ?: return@forEach
                        val position = item.position

                        val tagResolver = getItemPlaceholders(user, item)

                        ItemUtils.updateItem(itemStack, player, tagResolver)

                        val intelligentItem = IntelligentItem.of(itemStack) {
                            purchaseItem(player, user, item, vaultHook, contents)
                        }
                        if (position != -1) {
                            contents.set(position - 1, intelligentItem)
                        } else {
                            contents.add(intelligentItem)
                        }
                    }

                    filler?.let { item ->
                        val itemStack = item.itemStack?.clone() ?: return@let

                        if (itemStack.hasItemMeta()) {
                            ItemUtils.updateItem(itemStack, player)
                        }
                        contents.fillEmpty(itemStack)
                    }
                }

                override fun update(player: Player, contents: InventoryContents) {
                    if (menuType === MenuType.ROTATING_SHOP) {
                        if (Instant.now().isAfter(refreshTime)) {
                            refreshShopItems()
                            refreshTime = Instant.now().plusSeconds(config.getLong("refresh_time"))
                        }
                    }
                    val user = plugin.userManager.getIfLoaded(player) ?: return
                    val vaultHook = plugin.hookManager.vaultHook

                    // Only update the items that have a static position.
                    val menuItems = if (menuType === MenuType.ROTATING_SHOP) {
                        buildSet {
                            addAll(allMenuItems.filterNot { item -> shopItems.any { shopItem -> item.name == shopItem.name } && item.position == -1 })
                            addAll(shopItems)
                        }
                    } else {
                        allMenuItems.filterNot { it.position == -1 }
                    }

                    menuItems.forEach { item ->
                        val itemStack = item.itemStack?.clone() ?: return@forEach
                        val position = item.position

                        val tagResolver = getItemPlaceholders(user, item)

                        ItemUtils.updateItem(itemStack, player, tagResolver)

                        val intelligentItem = IntelligentItem.of(itemStack) {
                            purchaseItem(player, user, item, vaultHook, contents)
                        }
                        contents.update(position - 1, intelligentItem)
                    }
                }

                override fun close(player: Player, inventory: RyseInventory) {
                    closingSound?.play(player)
                }
            })
            .run { if (updateInterval < 1) this.disableUpdateTask() else this }
            .period(updateInterval, TimeSetting.MILLISECONDS)
            .title(title!!.parse())
            .size(inventorySize)
            .build(plugin)
    }

    fun initializeShopItems() {
        allShopItems.clear()
        this.allShopItems.addAll(allMenuItems.filter { it.name != "filler" && (it.price != null || it.priceVault != null) && it.position == -1})
    }

    fun refreshShopItems() {
        shopItems.clear()
        val shopSlots = config.getIntList("shop_slots")
        plugin.debug { "[${file.name}] shopSlots=$shopSlots" }
        val shopItems = this.allShopItems.filter { it.success }.map { it.clone() }.toMutableList()
        for (slot in shopSlots) {
            if (shopItems.isEmpty()) break // If there are no shopItems left anymore break the loop
            val shopItem = shopItems.random()
            shopItem.position = slot
            shopItems.remove(shopItem)
            this.shopItems.add(shopItem)
        }
    }

    private fun purchaseItem(
        player: Player,
        user: User,
        item: MenuItem,
        vaultHook: VaultHook,
        contents: InventoryContents
    ) {
        if (!checkItemPermission(player, item.permission)) return
        val stock = item.stock
        val purchaseLimit = item.purchaseLimit
        val price = item.price
        val priceVault = item.priceVault

        val pricePlaceholder = Placeholder.unparsed("price", price.toString())
        val priceVaultPlaceholder = Placeholder.unparsed("price_vault", priceVault.toString())

        if (stock != null && stock < 1) {
            player.sendRichMessage(plugin.messagesConfig.menusOutOfStock)
            return
        }

        if (purchaseLimit != null && purchaseLimit > 0) {
            if (item.hasReachedPlayerPurchaseLimit(player.uniqueId, purchaseLimit)) {
                player.sendRichMessage(plugin.messagesConfig.menusLimitReached)
                return
            }
        }

        if (price != null && price > 0.0) {
            if (user.hasEnough(price.toBigDecimal())) {
                plugin.launch(plugin.entityDispatcher(player), CoroutineStart.UNDISPATCHED) {
                    user.withdrawCoins(price)
                    user.addCoinsSpent(price)
                    player.sendMessage(plugin.messagesConfig.menusItemPurchased.parse(pricePlaceholder))
                }
            } else {
                player.sendRichMessage(plugin.messagesConfig.menusNotEnoughMobCoins)
                return
            }
        }

        if (priceVault != null && priceVault > 0.0) {
            val response = vaultHook.take(player, BigDecimal(priceVault))
            response.handle({
                player.sendMessage(plugin.messagesConfig.menusItemPurchasedVault.parse(priceVaultPlaceholder))
            }
            ) { reason ->
                player.sendRichMessage(reason)
            }
            if (response.isFailing) return
        }

        if (stock != null) {
            item.stock = stock - 1
        }

        if (purchaseLimit != null && purchaseLimit > 0) {
            item.increasePlayerPurchaseLimit(player.uniqueId)
        }

        plugin.logWriter.writeAsync("${player.name} purchased 1x ${item.name} for $price mobcoins.")

        if (closeOnClick) inventory.close(player) else contents.reload()
        item.actions.forEach { action ->
            action.actionType.executeAction(player, action.action)
        }
        item.message?.takeIf { it.isNotEmpty() }?.let {
            player.sendMessage(it.parse(player, TagResolver.resolver(pricePlaceholder, priceVaultPlaceholder)))
        }
    }

    private fun getItemPlaceholders(user: User, item: MenuItem): TagResolver {
        val tags = mutableListOf(
            Placeholder.unparsed("price", item.price.toString()),
            Placeholder.unparsed("price_vault", item.priceVault.toString()),
            Placeholder.unparsed("stock", item.stock.toString()),
            Placeholder.unparsed("balance", user.coinsPretty),
            Placeholder.unparsed("mobcoins", user.coinsPretty),
            Placeholder.unparsed("mobcoins_collected", user.coinsCollectedPretty),
            Placeholder.unparsed("mobcoins_spent", user.coinsSpentPretty),
            Placeholder.unparsed("permission", item.permission ?: ""),
            Placeholder.unparsed("purchase_limit", item.purchaseLimit.toString()),
            Placeholder.unparsed("player_purchase_limit", item.getPlayerPurchaseLimit(user.uuid).toString()),
            Placeholder.unparsed("spinner_prize", plugin.spinnerManager.usageCosts.toString()),
            plugin.getRemainingTimeTagResolver()
        )
        if (menuType === MenuType.ROTATING_SHOP) {
            val remainingTime = getTimeRemaining()
            tags.add(Placeholder.unparsed("remaining_time", plugin.formatDuration(remainingTime)))
        }

        return TagResolver.resolver(tags)
    }

    private fun checkItemPermission(player: Player, itemPermission: String?): Boolean {
        if (itemPermission != null && !player.hasPermission(itemPermission)) {
            player.sendMessage(plugin.messagesConfig.menusNoPermission.parse(Placeholder.parsed("permission", itemPermission)))
            return false
        }
        return true
    }

    fun open(player: Player) {
        if (permission != null && !player.hasPermission(permission)) {
            player.sendMessage(plugin.messagesConfig.noPermission.parse(Placeholder.parsed("permission", permission)))
            return
        }
        inventory.newInstance().open(player)
        openingSound?.play(player)
    }

    fun saveShopItemsData() {
        if (shopItems.isEmpty()) return
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

            shopItems.forEach { shopItem ->
                shopItem.stock?.let { itemsDataSection.set("${shopItem.name}.stock", it) }
                itemsDataSection.set("${shopItem.name}.purchaseLimits", shopItem.purchaseLimits)
                itemsDataSection.set("${shopItem.name}.position", shopItem.position)
            }

            config.save()
        } catch (ex: IOException) {
            plugin.logger.log(Level.SEVERE, "Something went wrong trying to create data file for shop ${file.name}")
        }
    }

    private fun updateFromLastShopData(): Boolean {
        if (shopItems.isNotEmpty()) return false
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
            shopItems.clear()
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
                shopItems.add(item)
            }

            plugin.logger.info("Loaded ${shopItems.size} items from ${shopDataFile.fileName}")
            return true
        } catch (ex: IOException) {
            plugin.logger.log(Level.SEVERE, "Something went wrong trying to create data file for shop ${file.name}")
            return false
        }
    }

    init {
        title = config.getString("title", "MobCoin Shop")
        menuType = config.getEnum("type", MenuType::class.java, MenuType.NORMAL)
        permission = config.getString("permission", null)
        closeOnClick = config.getBoolean("close_on_click")

        updateInterval = config.getInt("update_interval", 20)
        if (updateInterval > 0) updateInterval * 50

        inventorySize = config.getInt("size", 54)
        if (inventorySize < 9) {
            inventorySize = 54
        }

        val soundsSection = config.getSection("sounds")
        if (soundsSection != null) {
            if (soundsSection.contains("opening")) {
                openingSound =
                    ConfigurableSound.deserialize(soundsSection.getSection("opening").getStringRouteMappedValues(false))
            }
            if (soundsSection.contains("closing")) {
                closingSound =
                    ConfigurableSound.deserialize(soundsSection.getSection("closing").getStringRouteMappedValues(false))
            }
        }

        loadAllItems()

        if (menuType === MenuType.ROTATING_SHOP) {
            refreshTime = Instant.now().plusSeconds(config.getLong("refresh_time"))
            initializeShopItems()
            if (!updateFromLastShopData()) {
                refreshShopItems()
            }
        }

        buildInventory()
    }
}