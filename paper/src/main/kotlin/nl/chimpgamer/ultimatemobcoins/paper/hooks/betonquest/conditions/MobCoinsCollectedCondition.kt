package nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.conditions

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.betonquest.betonquest.Instruction
import org.betonquest.betonquest.api.Condition
import org.betonquest.betonquest.api.profiles.Profile
import org.bukkit.plugin.java.JavaPlugin
import kotlin.jvm.optionals.getOrNull

class MobCoinsCollectedCondition(instruction: Instruction) : Condition(instruction, false) {
    private val ultimateMobCoinsPlugin by lazy { JavaPlugin.getPlugin(UltimateMobCoinsPlugin::class.java) }

    private val mobcoins = instruction.varNum
    override fun execute(profile: Profile?): Boolean {
        if (profile == null) return false
        val expectedMobCoins = mobcoins.getValue(profile).toDouble()
        val onlineProfile = profile.onlineProfile.getOrNull() ?: return false
        val player = onlineProfile.player
        val user = ultimateMobCoinsPlugin.userManager.getIfLoaded(player) ?: return false
        return user.coinsCollectedAsDouble >= expectedMobCoins
    }
}