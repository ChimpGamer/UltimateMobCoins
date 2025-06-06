package nl.chimpgamer.ultimatemobcoins.paper.models.menu

import io.github.rysefoxx.inventory.plugin.content.InventoryContents
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.configurations.MenuConfig
import nl.chimpgamer.ultimatemobcoins.paper.utils.ItemUtils
import org.bukkit.entity.Player
import kotlin.collections.filterNot

class ShopMenu(plugin: UltimateMobCoinsPlugin, config: MenuConfig) : RefreshableShopMenu(plugin, config) {
    private val shopItems = HashSet<MenuItem>()

    override val provider = object: InventoryProvider {
        override fun init(player: Player, contents: InventoryContents) {
            val user = plugin.userManager.getIfLoaded(player) ?: return
            val vaultHook = plugin.hookManager.vaultHook

            (menuItems.filterNot { it.isShopItem } + shopItems).forEach { item ->
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

            updateItemsInMenu(contents, menuItems.filterNot { menuItem -> menuItem.position == -1 && shopItems.any { it.name == menuItem.name } } + shopItems, player, user, vaultHook)
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
        shopItems.clear()
        shopItems.addAll(menuItems.filter { it.isShopItem }.map { it.clone() }.toSet())
    }

    init {
        loadAllItems()

        refreshShopItems()

        buildInventory()
    }
}