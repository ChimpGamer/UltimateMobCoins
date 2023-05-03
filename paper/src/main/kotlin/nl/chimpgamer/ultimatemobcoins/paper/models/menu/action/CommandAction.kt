package nl.chimpgamer.ultimatemobcoins.paper.models.menu.action

import nl.chimpgamer.ultimatemobcoins.paper.utils.Utils
import org.bukkit.entity.Player

class CommandAction : ActionType() {
    override fun executeAction(player: Player, action: Any) {
        val command = action.toString()
        Utils.executeCommand(Utils.applyPlaceholders(command, player))
    }

    override val names: Array<String> = arrayOf("command", "consolecommand")
}