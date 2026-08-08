package nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.conditions

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.bukkit.plugin.java.JavaPlugin

class MobCoinsSpentCondition(private val amount: Argument<Number>) : PlayerCondition {
    private val ultimateMobCoinsPlugin = JavaPlugin.getPlugin(UltimateMobCoinsPlugin::class.java)

    override fun check(profile: Profile): Boolean {
        val player = profile.player.player ?: return false
        val user = ultimateMobCoinsPlugin.userManager.getIfLoaded(player) ?: return false
        val expectedMobCoins = amount.getValue(profile).toDouble()
        return user.coinsSpentAsDouble >= expectedMobCoins
    }
}