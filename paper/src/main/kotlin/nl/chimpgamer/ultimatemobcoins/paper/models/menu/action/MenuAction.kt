package nl.chimpgamer.ultimatemobcoins.paper.models.menu.action

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.entity.Player

class MenuAction(private val plugin: UltimateMobCoinsPlugin) : ActionType() {

    override fun executeAction(player: Player, action: Any) {
        val menuName = action.toString()
        plugin.shopMenus[menuName]?.inventory?.open(player)
    }

    override val names: Array<String> = arrayOf("menu", "openmenu")
}