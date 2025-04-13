package nl.chimpgamer.ultimatemobcoins.paper.models.menu.action

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.entity.Player

class MenuAction(private val plugin: UltimateMobCoinsPlugin) : ActionType() {

    override fun executeAction(player: Player, action: Any) {
        val menuName = action.toString()
        if (menuName.equals("spinnerMenu", ignoreCase = true)) {
            plugin.spinnerManager.spinnerMenu.open(player)
            return
        }
        plugin.shopMenus[menuName]?.open(player)
    }

    override val names: Array<String> = arrayOf("menu", "openmenu")
}