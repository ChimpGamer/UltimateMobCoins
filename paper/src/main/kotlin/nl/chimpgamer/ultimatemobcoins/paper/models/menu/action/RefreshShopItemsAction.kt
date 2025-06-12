package nl.chimpgamer.ultimatemobcoins.paper.models.menu.action

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.RotatingShopMenu
import org.bukkit.entity.Player

class RefreshShopItemsAction(private val plugin: UltimateMobCoinsPlugin) : ActionType() {

    override fun executeAction(player: Player, action: Any) {
        val menuName = action.toString()
        val menu = plugin.shopMenus[menuName] ?: return
        if (menu is RotatingShopMenu) menu.refreshShopItems()
    }

    override val names: Array<String> = arrayOf("refresh-items", "refresh-shop-items")
}