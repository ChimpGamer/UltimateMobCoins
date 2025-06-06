package nl.chimpgamer.ultimatemobcoins.paper.models.menu

import io.github.rysefoxx.inventory.plugin.content.InventoryContents
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.configurations.MenuConfig
import nl.chimpgamer.ultimatemobcoins.paper.utils.ItemUtils
import org.bukkit.entity.Player

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

    override fun saveShopItemsData() {
        saveShopItemsData(currentShopItems)
    }

    init {
        loadAllItems()

        if (!getItemsFromLastShopData(currentShopItems)) {
            refreshShopItems()
        }

        buildInventory()
    }
}