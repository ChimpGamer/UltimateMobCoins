package nl.chimpgamer.ultimatemobcoins.paper.models.action

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.entity.Player

class MenuAction(private val plugin: UltimateMobCoinsPlugin) : ActionType() {

    override fun executeAction(player: Player, action: Any) {
        val menuName = action.toString()

    }

    override val names: Array<String> = arrayOf("menu", "openmenu")
}