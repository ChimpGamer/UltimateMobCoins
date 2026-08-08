package nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.actions

import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.quest.action.PlayerAction
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory

class MobCoinsActionFactory : PlayerActionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerAction {
        val multi = instruction.bool().getFlag("multi", true)
        val amount: Argument<Number> = instruction.number().get()

        return MobCoinsAction(amount, multi)
    }
}