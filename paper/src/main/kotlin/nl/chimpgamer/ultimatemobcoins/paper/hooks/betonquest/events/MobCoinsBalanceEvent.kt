package nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.events

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.betonquest.betonquest.Instruction
import org.betonquest.betonquest.VariableNumber
import org.betonquest.betonquest.api.QuestEvent
import org.betonquest.betonquest.api.profiles.Profile
import org.betonquest.betonquest.exceptions.InstructionParseException
import org.bukkit.plugin.java.JavaPlugin
import kotlin.jvm.optionals.getOrNull

class MobCoinsBalanceEvent(instruction: Instruction) : QuestEvent(instruction, false) {

    private var amount: VariableNumber
    private var multi = false

    override fun execute(profile: Profile?): Void? {
        if (profile == null) return null;
        val onlineProfile = profile.onlineProfile.getOrNull() ?: return null
        val ultimateMobCoinsPlugin = JavaPlugin.getPlugin(UltimateMobCoinsPlugin::class.java)
        val player = onlineProfile.player
        val user = ultimateMobCoinsPlugin.userManager.getIfLoaded(player) ?: return null
        val current = user.coinsAsDouble

        val target = if (multi) {
            current * amount.getDouble(profile)
        } else {
            current + amount.getDouble(profile)
        }
        val difference = target - current

        if (difference > 0) {
            user.depositCoins(difference)
        } else if (difference < 0) {
            user.withdrawCoins(-difference)
        }

        return null
    }

    init {
        var string = instruction.next()
        if (string.isNotEmpty() && string[0] == '*') {
            multi = true
            string = string.replace("*", "")
        } else {
            multi = false
        }
        try {
            amount = VariableNumber(instruction.getPackage(), string)
        } catch (e: InstructionParseException) {
            throw InstructionParseException("Could not parse money amount", e)
        }
    }
}