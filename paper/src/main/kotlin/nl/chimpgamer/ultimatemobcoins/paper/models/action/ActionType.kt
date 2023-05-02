package nl.chimpgamer.ultimatemobcoins.paper.models.action

import org.bukkit.entity.Player

abstract class ActionType {

    companion object {
        var actions: List<ActionType>

        init {
            actions = listOf(
                CloseAction(),
                CommandAction()
            )
        }

        fun findActionType(str: String): ActionType? {
            val regex = Regex("^\\[[^]\\[]*]")
            val matchResult = regex.find(str, 0) ?: return null
            val type = matchResult.value.replaceFirst("[", "").replaceFirst("]", "")
            return actions.find { it.names.contains(type.lowercase()) }
        }
    }

    abstract fun executeAction(player: Player, action: Any)

    abstract val names: Array<String>
}