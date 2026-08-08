package nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.actions

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.CoroutineStart
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.FlagArgument
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.api.quest.action.PlayerAction
import org.bukkit.plugin.java.JavaPlugin

class MobCoinsAction(
    private val amount: Argument<Number>,
    private val multi: FlagArgument<Boolean>
) : PlayerAction {
    private val ultimateMobCoinsPlugin = JavaPlugin.getPlugin(UltimateMobCoinsPlugin::class.java)

    override fun execute(profile: Profile) {
        val player = profile.player.player ?: return
        val user = ultimateMobCoinsPlugin.userManager.getIfLoaded(player) ?: return
        val current = user.coinsAsDouble
        val target = if (multi.getValue(profile)?.orElse(false) ?: false) {
            current * amount.getValue(profile).toDouble()
        } else {
            current + amount.getValue(profile).toDouble()
        }

        val difference = target - current
        ultimateMobCoinsPlugin.launch(ultimateMobCoinsPlugin.entityDispatcher(player), CoroutineStart.UNDISPATCHED) {
            if (difference > 0) {
                user.depositCoins(difference)
            } else if (difference < 0) {
                user.withdrawCoins(-difference)
            }
        }
    }
}