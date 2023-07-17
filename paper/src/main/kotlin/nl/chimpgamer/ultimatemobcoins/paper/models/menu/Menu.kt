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
import nl.chimpgamer.ultimatemobcoins.paper.models.ConfigurableSound
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.action.Action
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.action.ActionType
import nl.chimpgamer.ultimatemobcoins.paper.utils.ItemUtils
import nl.chimpgamer.ultimatemobcoins.paper.utils.LogWriter
import nl.chimpgamer.ultimatemobcoins.paper.utils.Utils
import org.bukkit.entity.Player
import java.io.File
import java.math.BigDecimal
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

    private var openingSound: ConfigurableSound? = null
    private var closingSound: ConfigurableSound? = null

    lateinit var inventory: RyseInventory

    val allMenuItems = HashSet<MenuItem>()

    private fun loadAllItems() {
        allMenuItems.clear()
        val section = config.getSection("items")
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
        menuitem.itemStack = ItemUtils.itemDataToItemStack(plugin, itemSection.getStringList("item"))
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
                    val vaultHook = plugin.hookManager.vaultHook

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
                        val priceVault = item.priceVault
                        val stock = item.stock
                        val pricePlaceholder = Placeholder.unparsed("price", price.toString())
                        val priceVaultPlaceholder = Placeholder.unparsed("price_vault", priceVault.toString())
                        val stockPlaceholder = Placeholder.unparsed("stock", stock.toString())
                        val balancePlaceholder = Placeholder.unparsed("balance", user.coinsAsDouble.toString())

                        val tagResolverBuilder = TagResolver.builder()
                        if (menuType === MenuType.ROTATING_SHOP) {
                            val remainingTime = getTimeRemaining()
                            tagResolverBuilder.resolver(
                                Placeholder.unparsed(
                                    "remaining_time",
                                    plugin.formatDuration(remainingTime)
                                )
                            )
                        }
                        tagResolverBuilder.resolvers(pricePlaceholder, priceVaultPlaceholder, stockPlaceholder, balancePlaceholder)
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

                            if (priceVault != null && priceVault > 0.0) {
                                if (stock != null && stock < 1) {
                                    player.sendRichMessage(plugin.messagesConfig.menusOutOfStock)
                                    return@of
                                }

                                vaultHook.take(player, BigDecimal(priceVault)).handle({ player.sendRichMessage("<green>$priceVault has been taken from your account!") }) { reason -> player.sendRichMessage(reason); return@handle}

                                if (stock != null) {
                                    item.stock = stock - 1
                                }

                                LogWriter(
                                    plugin,
                                    "${player.name} bought 1x ${item.name} for $priceVault money."
                                ).runAsync()
                            }

                            if (closeOnClick) inventory.close(player) else contents.reload()
                            item.actions.forEach { action ->
                                action.actionType.executeAction(player, action.action)
                            }

                            if (!item.message.isNullOrEmpty()) player.sendMessage(
                                item.message!!.parse(
                                    TagResolver.resolver(pricePlaceholder, priceVaultPlaceholder)
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

                        val price = item.price
                        val priceVault = item.priceVault
                        val stock = item.stock
                        val pricePlaceholder = Placeholder.unparsed("price", price.toString())
                        val priceVaultPlaceholder = Placeholder.unparsed("price_vault", priceVault.toString())
                        val stockPlaceholder = Placeholder.unparsed("stock", stock.toString())
                        val balancePlaceholder = Placeholder.unparsed("balance", user.coinsAsDouble.toString())

                        val tagResolverBuilder = TagResolver.builder()
                        if (menuType === MenuType.ROTATING_SHOP) {
                            val remainingTime = getTimeRemaining()
                            tagResolverBuilder.resolver(
                                Placeholder.unparsed(
                                    "remaining_time",
                                    plugin.formatDuration(remainingTime)
                                )
                            )
                        }
                        tagResolverBuilder.resolvers(pricePlaceholder, priceVaultPlaceholder, stockPlaceholder, balancePlaceholder)
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

                            if (priceVault != null && priceVault > 0.0) {
                                if (stock != null && stock < 1) {
                                    player.sendRichMessage(plugin.messagesConfig.menusOutOfStock)
                                    return@of
                                }

                                vaultHook.take(player, BigDecimal(priceVault)).handle({ player.sendRichMessage("<green>$priceVault has been taken from your account!") }) { reason -> player.sendRichMessage(reason); return@handle}

                                if (stock != null) {
                                    item.stock = stock - 1
                                }

                                LogWriter(
                                    plugin,
                                    "${player.name} bought 1x ${item.name} for $priceVault money."
                                ).runAsync()
                            }

                            if (closeOnClick) inventory.close(player) else contents.reload()
                            item.actions.forEach { action ->
                                action.actionType.executeAction(player, action.action)
                            }
                            item.message?.takeIf { it.isNotEmpty() }?.let { player.sendMessage(it.parse(TagResolver.resolver(pricePlaceholder, priceVaultPlaceholder))) }
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
        val shopSlots = config.getIntList("shop_slots")
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
                openingSound = ConfigurableSound.deserialize(soundsSection.getSection("opening").getStringRouteMappedValues(false))
            }
            if (soundsSection.contains("closing")) {
                closingSound = ConfigurableSound.deserialize(soundsSection.getSection("closing").getStringRouteMappedValues(false))
            }
        }

        loadAllItems()

        if (menuType === MenuType.ROTATING_SHOP) {
            refreshTime = Instant.now().plusSeconds(config.getLong("refresh_time"))
            this.shopItems = HashSet()
            refreshShopItems()
        }

        buildInventory()
    }
}