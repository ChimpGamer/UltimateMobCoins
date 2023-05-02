package nl.chimpgamer.ultimatemobcoins.paper.models

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
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.models.action.Action
import nl.chimpgamer.ultimatemobcoins.paper.models.action.ActionType
import nl.chimpgamer.ultimatemobcoins.paper.utils.ItemUtils
import nl.chimpgamer.ultimatemobcoins.paper.utils.LogWriter
import org.bukkit.entity.Player
import java.io.File

class RotatingShopMenu(private val plugin: UltimateMobCoinsPlugin, file: File) : ShopMenuBase(plugin, file) {

    val allShopItems = HashSet<ShopItem>()
    val currentShopItems = HashSet<ShopItem>()

    fun loadAllShopItems() {
        allShopItems.clear()
        val section = config.getSection("ShopItems")
        if (section != null) {
            for (key in section.keys) {
                val shopItem = loadShopItem(section, key.toString())
                if (shopItem != null) {
                    allShopItems.add(shopItem)
                }
            }
        }
    }

    fun loadShopItem(section: Section, name: String): ShopItem? {
        val itemSection = section.getSection(name)
        if (itemSection == null) {
            println("$name does not exist in the config")
            return null
        }
        val shopItem = ShopItem(name)
        shopItem.itemStack = ItemUtils.itemDataToItemStack(plugin, itemSection.getStringList("ItemData"))
        if (itemSection.contains("Position")) {
            shopItem.position = itemSection.getInt("Position")
        }
        if (itemSection.contains("Message")) {
            shopItem.message = itemSection.getString("Message")
        }
        if (itemSection.contains("Price")) {
            shopItem.price = itemSection.getDouble("Price")
        }
        if (itemSection.contains("Stock")) {
            shopItem.stock = itemSection.getInt("Stock")
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
                shopItem.actions.add(Action(actionType, action))
            }
        }
        return shopItem
    }

    fun refreshShopItems() {
        currentShopItems.clear()
        allShopItems.groupBy { it.position }.filter { it.value.isNotEmpty() }.values.forEach {
            val shopItem = it.random()
            currentShopItems.add(shopItem.clone())
        }
    }

    private fun buildInventory() {
        inventory = RyseInventory.builder()
            .provider(object : InventoryProvider {
                override fun init(player: Player, contents: InventoryContents) {
                    val user = plugin.userManager.getByUUID(player.uniqueId) ?: return

                    allShopItems.forEach { shopItem ->
                        val itemStack = shopItem.itemStack?.clone() ?: return@forEach
                        val position = shopItem.position

                        val price = shopItem.price
                        val stock = shopItem.stock
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
                            if (price == 0.0) return@of
                            if (stock != null && stock < 1) {
                                player.sendRichMessage("<dark_red><bold>(!)</bold> <red>Sorry, this item is out of stock!")
                                return@of
                            }

                            if (price != null) {
                                if (user.coins >= price.toBigDecimal()) {
                                    user.withdrawCoins(price)
                                    user.addCoinsSpent(price)
                                    player.sendRichMessage("<green><bold>(!)</bold> <gold>You have bought this item for <yellow>$price <gold>mobcoins!")
                                } else {
                                    player.sendRichMessage("<dark_red><bold>(!)</bold> <red>You don't have enough mobcoins to purchase this item!")
                                    return@of
                                }
                            }

                            if (stock != null) {
                                shopItem.stock = stock - 1
                            }
                            if (closeOnClick) inventory.close(player) else contents.reload()
                            shopItem.actions.forEach { action ->
                                action.actionType.executeAction(player, action.action)
                            }

                            if (!shopItem.message.isNullOrEmpty()) player.sendMessage(
                                shopItem.message!!.parse(
                                    pricePlaceholder
                                )
                            )
                            LogWriter(
                                plugin,
                                "${player.name} bought 1x ${shopItem.name} for ${shopItem.price} mobcoins."
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

    init {
        loadAllShopItems()
        refreshShopItems()

        buildInventory()
    }
}