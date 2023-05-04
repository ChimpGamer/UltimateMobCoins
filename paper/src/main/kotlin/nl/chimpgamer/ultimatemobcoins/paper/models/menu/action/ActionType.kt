package nl.chimpgamer.ultimatemobcoins.paper.models.menu.action

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.utils.Utils
import org.bukkit.entity.Player

abstract class ActionType {

    companion object {
        private var actions = ArrayList<ActionType>()

        fun initialize(plugin: UltimateMobCoinsPlugin) {
            actions.addAll(
                listOf(
                    CloseAction(),
                    CommandAction(),
                    MenuAction(plugin)
                )
            )
        }

        fun findActionType(str: String): ActionType? {
            val matchResult = Utils.actionTypeRegex.find(str, 0) ?: return null
            val type = matchResult.value.replaceFirst("[", "").replaceFirst("]", "")
            return actions.find { it.names.contains(type.lowercase()) }
        }
    }

    abstract fun executeAction(player: Player, action: Any)

    abstract val names: Array<String>
}