package nl.chimpgamer.ultimatemobcoins.paper.hooks

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.MenuType
import nl.chimpgamer.ultimatemobcoins.paper.utils.NumberFormatter
import org.bukkit.entity.Player

class PlaceholderAPIHook(private val plugin: UltimateMobCoinsPlugin) : PlaceholderExpansion() {

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (params.startsWith("shop_refresh_time_")) {
            val shopName = params.replace("shop_refresh_time_", "")
            val menu = plugin.shopMenus[shopName]
            if (menu != null && menu.menuType === MenuType.ROTATING_SHOP) {
                val remainingTime = menu.getTimeRemaining()
                return plugin.formatDuration(remainingTime)
            }
        }
        if (params.equals("spinner_price", ignoreCase = true)) {
            return plugin.spinnerManager.usageCosts.toString()
        }

        if (player == null) return null
        val user = plugin.userManager.getIfLoaded(player) ?: return null
        if (params.equals("balance", ignoreCase = true)) {
            return user.coinsAsDouble.toString()
        }
        if (params.equals("balance_fixed", ignoreCase = true)) {
            return NumberFormatter.FIXED_FORMAT.format(user.coinsAsDouble)
        }
        if (params.equals("balance_commas", ignoreCase = true)) {
            return NumberFormatter.COMMAS_FORMAT.format(user.coinsAsDouble)
        }

        if (params.equals("collected", ignoreCase = true)) {
            return user.coinsCollectedAsDouble.toString()
        }
        if (params.equals("collected_fixed", ignoreCase = true)) {
            return NumberFormatter.FIXED_FORMAT.format(user.coinsCollectedAsDouble)
        }
        if (params.equals("collected_commas", ignoreCase = true)) {
            return NumberFormatter.COMMAS_FORMAT.format(user.coinsCollectedAsDouble)
        }

        if (params.equals("spent", ignoreCase = true)) {
            return user.coinsSpentAsDouble.toString()
        }
        if (params.equals("spent_fixed", ignoreCase = true)) {
            return NumberFormatter.FIXED_FORMAT.format(user.coinsSpentAsDouble)
        }
        if (params.equals("spent_commas", ignoreCase = true)) {
            return NumberFormatter.COMMAS_FORMAT.format(user.coinsSpentAsDouble)
        }
        return null
    }

    override fun getIdentifier(): String = "ultimatemobcoins"

    override fun getAuthor(): String = plugin.authors.joinToString()

    override fun getVersion(): String = plugin.version

    override fun persist(): Boolean = true
}