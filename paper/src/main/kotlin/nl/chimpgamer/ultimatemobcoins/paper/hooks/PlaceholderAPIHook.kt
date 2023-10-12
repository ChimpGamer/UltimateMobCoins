package nl.chimpgamer.ultimatemobcoins.paper.hooks

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.entity.Player

class PlaceholderAPIHook(private val plugin: UltimateMobCoinsPlugin) : PlaceholderExpansion() {

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (player == null) return null
        val user = plugin.userManager.getIfLoaded(player) ?: return null
        if (params.equals("balance", ignoreCase = true)) {
            return user.coinsAsDouble.toString()
        }
        if (params.equals("collected", ignoreCase = true)) {
            return user.coinsCollectedAsDouble.toString()
        }
        if (params.equals("spent", ignoreCase = true)) {
            return user.coinsSpentAsDouble.toString()
        }
        if (params.equals("spinner_price", ignoreCase = true)) {
            return plugin.spinnerManager.usageCosts.toString()
        }
        return null
    }

    override fun getIdentifier(): String = "ultimatemobcoins"

    override fun getAuthor(): String = plugin.authors.joinToString()

    override fun getVersion(): String = plugin.version

    override fun persist(): Boolean = true
}