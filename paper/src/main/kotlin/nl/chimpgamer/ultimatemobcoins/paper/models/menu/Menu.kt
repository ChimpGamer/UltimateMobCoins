package nl.chimpgamer.ultimatemobcoins.paper.models.menu

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
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
import nl.chimpgamer.ultimatemobcoins.paper.configurations.MenuConfig
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.hooks.VaultHook
import nl.chimpgamer.ultimatemobcoins.paper.models.ConfigurableSound
import nl.chimpgamer.ultimatemobcoins.paper.models.User
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.action.Action
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.action.ActionType
import nl.chimpgamer.ultimatemobcoins.paper.utils.ItemUtils
import nl.chimpgamer.ultimatemobcoins.paper.utils.Utils
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File
import java.math.BigDecimal
import java.util.*
import java.util.logging.Level
import kotlin.collections.HashSet

abstract class Menu(protected val plugin: UltimateMobCoinsPlugin, protected val config: MenuConfig) {
    companion object {
        private const val CLICK_DELAY_MS = 300 //ms
        private const val DEFAULT_INVENTORY_SIZE = 54
        private const val MIN_INVENTORY_SIZE = 9
        private const val DEFAULT_UPDATE_INTERVAL = 20
    }

    val file: File get() = config.file

    private var title: String? = null
        get() = if (field == null) file.nameWithoutExtension else field
        set(value) {
            field = value ?: file.nameWithoutExtension
        }
    val menuType: MenuType = config.menuType
    val permission: String?

    var closeOnClick: Boolean = false
    private var updateInterval: Int
    private var inventorySize: Int

    private var openingSound: ConfigurableSound? = null
    protected var closingSound: ConfigurableSound? = null

    lateinit var inventory: RyseInventory

    private val lastClicks = WeakHashMap<Player, Long>()

    val menuItems = HashSet<MenuItem>()

    val filler by lazy { getItem("filler") }

    abstract val provider: InventoryProvider

    protected fun getItem(name: String) = menuItems.find { it.name.equals(name, ignoreCase = true) }

    protected fun loadAllItems() {
        menuItems.clear()
        menuItems.addAll(getItemsFromConfig())
    }

    protected fun getItemsFromConfig(): Set<MenuItem> {
        val items = HashSet<MenuItem>()
        val section = config.getSection("items")
        if (section != null) {
            for (key in section.keys) {
                val menuItem = loadMenuItem(section, key.toString())
                if (menuItem != null) {
                    items.add(menuItem)
                }
            }
        }
        return items
    }

    fun loadMenuItem(section: Section, name: String): MenuItem? {
        val itemSection = section.getSection(name)
        if (itemSection == null) {
            println("$name does not exist in the config")
            return null
        }
        val menuitem = MenuItem(
            name,
            itemStack = try {
                ItemUtils.itemDataToItemStack(plugin, itemSection.getStringList("item"))
            } catch (ex: Exception) {
                plugin.logger.log(Level.SEVERE, "Invalid Configuration! Menu item $name in '${file.absolutePath}' is invalid.", ex)
                return null
            },
            position = itemSection.getInt("position", -1),
            message = itemSection.getString("message"),
            permission = itemSection.getString("permission"),
            price = itemSection.getDouble("price", null),
            priceVault = itemSection.getDouble("price-vault", itemSection.getDouble("price_vault", null),),
            stock = itemSection.getInt("stock", null),
            purchaseLimit = itemSection.getInt("purchase-limit", itemSection.getInt("purchase_limit", null),),
            chance = itemSection.getInt("chance", 0)
        )
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

    protected fun buildInventory() {
        inventory = RyseInventory.builder()
            .provider(provider)
            .run { if (updateInterval < 1) this.disableUpdateTask() else this }
            .period(updateInterval, TimeSetting.MILLISECONDS)
            .title(title!!.parse())
            .size(inventorySize)
            .build(plugin)
    }

    protected fun createIntelligentItem(
        player: Player,
        user: User,
        item: MenuItem,
        vaultHook: VaultHook,
        contents: InventoryContents,
        itemStack: ItemStack
    ) = IntelligentItem.of(itemStack) {
        if (checkClickSpam(player)) return@of
        purchaseItem(player, user, item, vaultHook, contents)
    }

    private fun purchaseItem(
        player: Player,
        user: User,
        item: MenuItem,
        vaultHook: VaultHook,
        contents: InventoryContents
    ) {
        if (!checkItemPermission(player, item)) return
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

    protected fun applyFillerItem(contents: InventoryContents, player: Player) {
        filler?.let { item ->
            val itemStack = item.itemStack?.clone() ?: return@let

            if (itemStack.hasItemMeta()) {
                ItemUtils.updateItem(itemStack, player)
            }
            contents.fillEmpty(itemStack)
        }
    }

    protected fun updateItemsInMenu(contents: InventoryContents, menuItems: Collection<MenuItem>, player: Player, user: User, vaultHook: VaultHook) {
        menuItems.forEach { item ->
            val itemStack = item.itemStack?.clone() ?: return@forEach
            val position = item.position

            val tagResolver = getItemPlaceholders(user, item)

            ItemUtils.updateItem(itemStack, player, tagResolver)

            val intelligentItem = createIntelligentItem(player, user, item, vaultHook, contents, itemStack)
            contents.update(position - 1, intelligentItem)
        }
    }

    protected fun getItemPlaceholders(user: User, item: MenuItem): TagResolver {
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
            Placeholder.unparsed("spinner_price", plugin.spinnerConfig.usageCosts.toString()),
            plugin.getRemainingTimeTagResolver()
        )
        if (this is RefreshableShopMenu && hasResetTimer()) {
            val remainingTime = getTimeRemaining()
            tags.add(Placeholder.unparsed("remaining_time", plugin.formatDuration(remainingTime)))
        }

        return TagResolver.resolver(tags)
    }

    private fun checkItemPermission(player: Player, item: MenuItem): Boolean {
        if (item.hasPermission(player)) return true
        player.sendMessage(plugin.messagesConfig.menusNoPermission.parse(Placeholder.parsed("permission", item.permission!!)))
        return false
    }

    fun open(player: Player) {
        if (permission != null && !player.hasPermission(permission)) {
            player.sendMessage(plugin.messagesConfig.noPermission.parse(Placeholder.parsed("permission", permission)))
            return
        }
        inventory.newInstance().open(player)
        openingSound?.play(player)
    }

    fun checkClickSpam(player: Player): Boolean {
        if (player in lastClicks) {
            val lastClick = lastClicks[player]!!
            if (System.currentTimeMillis() < lastClick + CLICK_DELAY_MS) {
                player.sendRichMessage("<red>You are clicking too fast!")
                return true
            }
        }
        lastClicks[player] = System.currentTimeMillis()
        return false
    }

    init {
        title = config.getString("title", "MobCoin Shop")
        permission = config.getString("permission")
        closeOnClick = config.getBoolean("close_on_click")

        updateInterval = config.getInt("update_interval", DEFAULT_UPDATE_INTERVAL)
        if (updateInterval > 0) updateInterval * 50

        inventorySize = config.getInt("size", DEFAULT_INVENTORY_SIZE)
        if (inventorySize < MIN_INVENTORY_SIZE) {
            inventorySize = DEFAULT_INVENTORY_SIZE
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
    }
}