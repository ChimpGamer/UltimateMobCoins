package nl.chimpgamer.ultimatemobcoins.paper.models.menu.action

import org.bukkit.entity.Player

class CloseAction : ActionType() {
    override fun executeAction(player: Player, action: Any) {
        player.closeInventory()
    }

    override val names: Array<String> = arrayOf("close")
}