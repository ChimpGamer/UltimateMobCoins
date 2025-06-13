package nl.chimpgamer.ultimatemobcoins.paper.hooks

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.MenuType
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.RefreshableShopMenu
import nl.chimpgamer.ultimatemobcoins.paper.utils.NumberFormatter
import org.bukkit.entity.Player

class PlaceholderAPIHook(private val plugin: UltimateMobCoinsPlugin) : PlaceholderExpansion() {
    private val name = "PlaceholderAPI"
    private val isPluginEnabled = plugin.server.pluginManager.isPluginEnabled(name)

    fun load() {
        if (isPluginEnabled && plugin.hooksConfig.isHookEnabled(name)) {
            if (isRegistered) {
                unregister()
            }
            register()
            plugin.logger.info("Successfully loaded $name hook!")
        }
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (params.startsWith("shop_refresh_time_")) {
            val shopName = params.replace("shop_refresh_time_", "")
            val menu = plugin.shopMenus[shopName]
            if (menu != null && menu is RefreshableShopMenu) {
                val remainingTime = menu.getTimeRemaining()
                return plugin.formatDuration(remainingTime)
            }
        }
        if (params.equals("spinner_price", ignoreCase = true)) {
            return plugin.spinnerConfig.usageCosts.toString()
        }

        if (params.startsWith("leaderboard_mobcoins_", ignoreCase = true)) {
            val newParams = params.replaceFirst("leaderboard_mobcoins_", "")
            val position = newParams.substring(0, newParams.indexOfFirst { it == '_' }).toInt()

            val type = newParams.replaceFirst("${position}_", "")
            val user = plugin.leaderboardManager.getTopMobCoinsPosition(position)
            if (user == null) {
                return "..."
            }
            return when (type.lowercase()) {
                "name" -> user.username
                "value" -> user.coins.toString()
                "value_formatted" -> user.coinsPretty
                else -> null
            }
        }
        if (params.startsWith("leaderboard_mobcoins_grind_", ignoreCase = true)) {
            val newParams = params.replaceFirst("leaderboard_mobcoins_grind_", "")
            val position = newParams.substring(0, newParams.indexOfFirst { it == '_' }).toInt()

            val type = newParams.replaceFirst("${position}_", "")
            val user = plugin.leaderboardManager.getTopMobCoinsGrindPosition(position)
            if (user == null) {
                return "..."
            }
            return when (type.lowercase()) {
                "name" -> user.username
                "value" -> user.coins.toString()
                "value_formatted" -> user.coinsPretty
                else -> null
            }
        }

        if (player == null) return null
        val user = plugin.userManager.getIfLoaded(player) ?: return null
        if (params.equals("balance", ignoreCase = true)) {
            return user.coinsAsDouble.toString()
        }
        if (params.equals("balance_formatted", ignoreCase = true)) {
            return user.coinsPretty
        }
        if (params.equals("balance_fixed", ignoreCase = true)) {
            return NumberFormatter.FIXED_FORMAT.format(user.coinsAsDouble)
        }
        if (params.equals("balance_commas", ignoreCase = true)) {
            return NumberFormatter.COMMAS_FORMAT.format(user.coinsAsDouble)
        }
        if (params.equals("balance_formatted_compact", ignoreCase = true)) {
            return NumberFormatter.compactDecimalFormat(user.coins)
        }

        if (params.equals("collected", ignoreCase = true)) {
            return user.coinsCollectedAsDouble.toString()
        }
        if (params.equals("collected_formatted", ignoreCase = true)) {
            return user.coinsCollectedPretty
        }
        if (params.equals("collected_fixed", ignoreCase = true)) {
            return NumberFormatter.FIXED_FORMAT.format(user.coinsCollectedAsDouble)
        }
        if (params.equals("collected_commas", ignoreCase = true)) {
            return NumberFormatter.COMMAS_FORMAT.format(user.coinsCollectedAsDouble)
        }
        if (params.equals("collected_formatted_compact", ignoreCase = true)) {
            return NumberFormatter.compactDecimalFormat(user.coinsCollected)
        }

        if (params.equals("spent", ignoreCase = true)) {
            return user.coinsSpentAsDouble.toString()
        }
        if (params.equals("spent_formatted", ignoreCase = true)) {
            return user.coinsSpentPretty
        }
        if (params.equals("spent_fixed", ignoreCase = true)) {
            return NumberFormatter.FIXED_FORMAT.format(user.coinsSpentAsDouble)
        }
        if (params.equals("spent_commas", ignoreCase = true)) {
            return NumberFormatter.COMMAS_FORMAT.format(user.coinsSpentAsDouble)
        }
        if (params.equals("spent_formatted_compact", ignoreCase = true)) {
            return NumberFormatter.compactDecimalFormat(user.coinsSpent)
        }
        return null
    }

    override fun getIdentifier(): String = "ultimatemobcoins"

    override fun getAuthor(): String = plugin.authors.joinToString()

    override fun getVersion(): String = plugin.version

    override fun persist(): Boolean = true
}