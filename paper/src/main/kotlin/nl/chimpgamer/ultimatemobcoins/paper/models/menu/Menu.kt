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
import org.bukkit.entity.Player
import java.io.File

class Menu(private val plugin: UltimateMobCoinsPlugin, private val file: File) : AbstractMenuConfig(plugin, file) {

    var title: String? = null
        get() = if (field == null) file.nameWithoutExtension else field
        set(value) {
            field = value ?: file.nameWithoutExtension
        }
    val menuType: MenuType

    var closeOnClick: Boolean = false
    private var updateInterval: Int
    private var inventorySize: Int

    lateinit var inventory: RyseInventory

    val allMenuItems = HashSet<MenuItem>()

    fun loadAllItems() {
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
                val action = actionStr.replaceFirst(Regex("^\\[[^]\\[]*]"), "").trim()
                menuitem.actions.add(Action(actionType, action))
            }
        }
        return menuitem
    }

    // When Shop is a rotating shop
    lateinit var shopItems: MutableSet<MenuItem>

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
                        val tagResolver = TagResolver.resolver(pricePlaceholder, stockPlaceholder)

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
                            if (menuType === MenuType.NORMAL) {
                                if (closeOnClick) inventory.close(player) else contents.reload()
                                item.actions.forEach { action ->
                                    action.actionType.executeAction(player, action.action)
                                }

                                if (!item.message.isNullOrEmpty()) player.sendMessage(
                                    item.message!!.parse(
                                        pricePlaceholder
                                    )
                                )
                                return@of
                            }
                            if (price != null && price > 0.0) {
                                if (stock != null && stock < 1) {
                                    player.sendRichMessage("<dark_red><bold>(!)</bold> <red>Sorry, this item is out of stock!")
                                    return@of
                                }

                                if (user.coins >= price.toBigDecimal()) {
                                    user.withdrawCoins(price)
                                    user.addCoinsSpent(price)
                                    player.sendRichMessage("<green><bold>(!)</bold> <gold>You have bought this item for <yellow>$price <gold>mobcoins!")
                                } else {
                                    player.sendRichMessage("<dark_red><bold>(!)</bold> <red>You don't have enough mobcoins to purchase this item!")
                                    return@of
                                }

                                if (stock != null) {
                                    item.stock = stock -1
                                }
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
                            LogWriter(
                                plugin,
                                "${player.name} bought 1x ${item.name} for $price mobcoins."
                            ).runAsync()
                        }
                        if (position != -1) {
                            contents.set(position - 1, intelligentItem)
                        } else {
                            contents.add(intelligentItem)
                        }
                    }
                }
            })
            .run { if (updateInterval == -1) this.disableUpdateTask() else this }
            .period(updateInterval, TimeSetting.MILLISECONDS)
            .title(title!!.parse())
            .size(inventorySize)
            .build(plugin)
    }

    fun refreshShopItems() {
        shopItems.clear()
        val shopSlots = config.getIntList("ShopSlots")
        val shopItems = allMenuItems.filter { it.price != null && it.position == -1 }.toMutableList()
        for (slot in shopSlots) {
            val shopItem = shopItems.random().clone()
            shopItem.position = slot
            shopItems.remove(shopItem)
            this.shopItems.add(shopItem)
        }
    }

    init {
        title = config.getString("Title", "MobCoin Shop")
        menuType = config.getEnum("Type", MenuType::class.java, MenuType.NORMAL)
        closeOnClick = config.getBoolean("CloseOnClick")

        updateInterval = config.getInt("UpdateInterval", 20)
        if (updateInterval > 0) updateInterval * 50

        inventorySize = config.getInt("Size", 54)
        if (inventorySize < 9) {
            inventorySize = 54
        }

        loadAllItems()

        if (menuType === MenuType.ROTATING_SHOP) {
            this.shopItems = HashSet()
            refreshShopItems()
        }

        buildInventory()
    }
}