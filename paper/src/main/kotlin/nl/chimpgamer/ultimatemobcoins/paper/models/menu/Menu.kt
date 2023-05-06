package nl.chimpgamer.ultimatemobcoins.paper.models.menu

import dev.dejvokep.boostedyaml.block.implementation.Section
import io.github.rysefoxx.inventory.plugin.content.IntelligentItem
import io.github.rysefoxx.inventory.plugin.content.InventoryContents
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider
import io.github.rysefoxx.inventory.plugin.enums.TimeSetting
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.configurations.AbstractMenuConfig
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.action.Action
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.action.ActionType
import nl.chimpgamer.ultimatemobcoins.paper.utils.ItemUtils
import nl.chimpgamer.ultimatemobcoins.paper.utils.LogWriter
import nl.chimpgamer.ultimatemobcoins.paper.utils.Utils
import org.bukkit.entity.Player
import java.io.File
import java.time.Duration
import java.time.Instant

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

    private var openingSound: MenuSound? = null
    private var closingSound: MenuSound? = null

    lateinit var inventory: RyseInventory

    val allMenuItems = HashSet<MenuItem>()

    private fun loadAllItems() {
        allMenuItems.clear()
        val section = config.getSection("Items")
        if (section != null) {
            for (key in section.keys) {
                val shopItem = loadMenuItem(section, key.toString())
                if (shopItem != null) {
                    allMenuItems.add(shopItem)
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
        menuitem.itemStack = ItemUtils.itemDataToItemStack(plugin, itemSection.getStringList("ItemData"))
        if (itemSection.contains("Position")) {
            menuitem.position = itemSection.getInt("Position")
        }
        if (itemSection.contains("Message")) {
            menuitem.message = itemSection.getString("Message")
        }
        if (itemSection.contains("Permission")) {
            menuitem.permission = itemSection.getString("Permission")
        }
        if (itemSection.contains("Price")) {
            menuitem.price = itemSection.getDouble("Price")
        }
        if (itemSection.contains("Stock")) {
            menuitem.stock = itemSection.getInt("Stock")
        }
        if (itemSection.contains("Actions")) {
            val actionsList = itemSection.getStringList("Actions")
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

    // When Shop is a rotating shop
    lateinit var shopItems: MutableSet<MenuItem>
    private lateinit var refreshTime: Instant

    private fun getTimeRemaining(): Duration {
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
                    val user = plugin.userManager.getByUUID(player.uniqueId) ?: return

                    val menuItems = if (menuType === MenuType.ROTATING_SHOP) {
                        buildSet {
                            addAll(allMenuItems.filterNot { shopItems.contains(it) })
                            addAll(shopItems)
                        }
                    } else {
                        allMenuItems
                    }

                    menuItems.forEach { item ->
                        val itemStack = item.itemStack?.clone() ?: return@forEach
                        val position = item.position

                        val price = item.price
                        val stock = item.stock
                        val pricePlaceholder = Placeholder.unparsed("price", price.toString())
                        val stockPlaceholder = Placeholder.unparsed("stock", stock.toString())
                        val balancePlaceholder = Placeholder.unparsed("balance", user.coinsAsDouble.toString())

                        val tagResolverBuilder = TagResolver.builder()
                        if (menuType === MenuType.ROTATING_SHOP) {
                            val remainingTime = getTimeRemaining()
                            tagResolverBuilder.resolver(
                                Placeholder.unparsed(
                                    "remaining_time",
                                    Utils.formatDuration(remainingTime)
                                )
                            )
                        }
                        tagResolverBuilder.resolvers(pricePlaceholder, stockPlaceholder, balancePlaceholder)
                        val tagResolver = tagResolverBuilder.build()

                        itemStack.editMeta { meta ->
                            val displayName = meta.displayName.parse(tagResolver)
                                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                            val lore = meta.lore?.map {
                                it.parse(tagResolver)
                                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                            }
                            meta.displayName(displayName)
                            meta.lore(lore)
                        }

                        val intelligentItem = IntelligentItem.of(itemStack) {
                            if (item.permission != null && !player.hasPermission(item.permission!!)) {
                                player.sendRichMessage(plugin.messagesConfig.menusNoPermission)
                                return@of
                            }

                            if (price != null && price > 0.0) {
                                if (stock != null && stock < 1) {
                                    player.sendRichMessage(plugin.messagesConfig.menusOutOfStock)
                                    return@of
                                }

                                if (user.coins >= price.toBigDecimal()) {
                                    user.withdrawCoins(price)
                                    user.addCoinsSpent(price)
                                    player.sendMessage(plugin.messagesConfig.menusItemBought.parse(pricePlaceholder))
                                } else {
                                    player.sendRichMessage(plugin.messagesConfig.menusNotEnoughMobCoins)
                                    return@of
                                }

                                if (stock != null) {
                                    item.stock = stock - 1
                                }

                                LogWriter(
                                    plugin,
                                    "${player.name} bought 1x ${item.name} for $price mobcoins."
                                ).runAsync()
                            }

                            if (closeOnClick) inventory.close(player) else contents.reload()
                            item.actions.forEach { action ->
                                action.actionType.executeAction(player, action.action)
                            }

                            if (!item.message.isNullOrEmpty()) player.sendMessage(
                                item.message!!.parse(
                                    pricePlaceholder
                                )
                            )
                        }
                        if (position != -1) {
                            contents.set(position - 1, intelligentItem)
                        } else {
                            contents.add(intelligentItem)
                        }
                    }
                }

                override fun update(player: Player, contents: InventoryContents) {
                    if (menuType === MenuType.ROTATING_SHOP) {
                        if (Instant.now().isAfter(refreshTime)) {
                            refreshShopItems()
                            refreshTime = Instant.now().plusSeconds(config.getLong("ResetTime"))
                        }
                    }
                    val user = plugin.userManager.getByUUID(player.uniqueId) ?: return

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

                        val price = item.price
                        val stock = item.stock
                        val pricePlaceholder = Placeholder.unparsed("price", price.toString())
                        val stockPlaceholder = Placeholder.unparsed("stock", stock.toString())
                        val balancePlaceholder = Placeholder.unparsed("balance", user.coinsAsDouble.toString())

                        val tagResolverBuilder = TagResolver.builder()
                        if (menuType === MenuType.ROTATING_SHOP) {
                            val remainingTime = getTimeRemaining()
                            tagResolverBuilder.resolver(
                                Placeholder.unparsed(
                                    "remaining_time",
                                    Utils.formatDuration(remainingTime)
                                )
                            )
                        }
                        tagResolverBuilder.resolvers(pricePlaceholder, stockPlaceholder, balancePlaceholder)
                        val tagResolver = tagResolverBuilder.build()

                        itemStack.editMeta { meta ->
                            val displayName = meta.displayName.parse(tagResolver)
                                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                            val lore = meta.lore?.map {
                                it.parse(tagResolver)
                                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                            }
                            meta.displayName(displayName)
                            meta.lore(lore)
                        }

                        val intelligentItem = IntelligentItem.of(itemStack) {
                            if (item.permission != null && !player.hasPermission(item.permission!!)) {
                                player.sendRichMessage(plugin.messagesConfig.menusNoPermission)
                                return@of
                            }

                            if (price != null && price > 0.0) {
                                if (stock != null && stock < 1) {
                                    player.sendRichMessage(plugin.messagesConfig.menusOutOfStock)
                                    return@of
                                }

                                if (user.coins >= price.toBigDecimal()) {
                                    user.withdrawCoins(price)
                                    user.addCoinsSpent(price)
                                    player.sendMessage(plugin.messagesConfig.menusItemBought.parse(pricePlaceholder))
                                } else {
                                    player.sendRichMessage(plugin.messagesConfig.menusNotEnoughMobCoins)
                                    return@of
                                }

                                if (stock != null) {
                                    item.stock = stock - 1
                                }

                                LogWriter(
                                    plugin,
                                    "${player.name} bought 1x ${item.name} for $price mobcoins."
                                ).runAsync()
                            }

                            if (closeOnClick) inventory.close(player) else contents.reload()
                            item.actions.forEach { action ->
                                action.actionType.executeAction(player, action.action)
                            }

                            if (!item.message.isNullOrEmpty()) player.sendMessage(
                                item.message!!.parse(
                                    pricePlaceholder
                                )
                            )
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

    fun refreshShopItems() {
        shopItems.clear()
        val shopSlots = config.getIntList("ShopSlots")
        val shopItems = allMenuItems.filter { it.price != null && it.position == -1 }.map { it.clone() }.toMutableList()
        for (slot in shopSlots) {
            if (shopItems.isEmpty()) break // If there are no shopItems left anymore break the loop
            val shopItem = shopItems.random()
            shopItem.position = slot
            shopItems.remove(shopItem)
            this.shopItems.add(shopItem)
        }
    }

    fun open(player: Player) {
        if (permission != null && !player.hasPermission(permission)) {
            player.sendRichMessage(plugin.messagesConfig.noPermission)
            return
        }
        inventory.open(player)
        openingSound?.play(player)
    }

    init {
        title = config.getString("Title", "MobCoin Shop")
        menuType = config.getEnum("Type", MenuType::class.java, MenuType.NORMAL)
        permission = config.getString("Permission", null)
        closeOnClick = config.getBoolean("CloseOnClick")

        updateInterval = config.getInt("UpdateInterval", 20)
        if (updateInterval > 0) updateInterval * 50

        inventorySize = config.getInt("Size", 54)
        if (inventorySize < 9) {
            inventorySize = 54
        }

        val soundsSection = config.getSection("Sounds")
        if (soundsSection != null) {
            if (soundsSection.contains("Opening")) {
                openingSound = MenuSound.deserialize(soundsSection.getSection("Opening").getStringRouteMappedValues(false))
            }
            if (soundsSection.contains("Closing")) {
                closingSound = MenuSound.deserialize(soundsSection.getSection("Closing").getStringRouteMappedValues(false))
            }
        }

        loadAllItems()

        if (menuType === MenuType.ROTATING_SHOP) {
            refreshTime = Instant.now().plusSeconds(config.getLong("RefreshTime"))
            this.shopItems = HashSet()
            refreshShopItems()
        }

        buildInventory()
    }
}