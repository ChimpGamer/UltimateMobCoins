package nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.conditions

import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory

class MobCoinsSpentConditionFactory : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val amount = instruction.number().atLeast(1).get()
        return MobCoinsSpentCondition(amount)
    }
}