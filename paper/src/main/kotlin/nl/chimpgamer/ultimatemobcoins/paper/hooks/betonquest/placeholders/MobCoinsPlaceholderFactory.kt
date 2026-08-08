package nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.placeholders

import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.quest.placeholder.PlayerPlaceholder
import org.betonquest.betonquest.api.quest.placeholder.PlayerPlaceholderFactory

class MobCoinsPlaceholderFactory : PlayerPlaceholderFactory {

    override fun parsePlayer(instruction: Instruction): PlayerPlaceholder {
        return MobCoinsPlaceholder()
    }
}