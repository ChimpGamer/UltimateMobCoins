package nl.chimpgamer.ultimatemobcoins.paper.hooks

import io.github.miniplaceholders.api.Expansion
import net.kyori.adventure.text.minimessage.tag.Tag
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.RefreshableShopMenu
import nl.chimpgamer.ultimatemobcoins.paper.utils.NumberFormatter
import org.bukkit.entity.Player

class MiniPlaceholdersHook(plugin: UltimateMobCoinsPlugin) : PluginHook(plugin, "MiniPlaceholders") {
    private lateinit var expansion: Expansion

    override fun load() {
        if (!canHook()) return

        expansion = Expansion.builder("ultimatemobcoins")
            .filter(Player::class.java)

            .globalPlaceholder("shop_refresh_time") { argumentQueue, _ ->
                val shopName = argumentQueue.popOr("shop_refresh_time tag requires a valid rotating shop name.").value()
                val menu = plugin.shopMenus[shopName] ?: return@globalPlaceholder null
                if (menu !is RefreshableShopMenu) return@globalPlaceholder null

                Tag.preProcessParsed(plugin.formatDuration(menu.getTimeRemaining()))
            }
            .globalPlaceholder("spinner_price") { _, _ ->
                Tag.preProcessParsed(plugin.spinnerConfig.usageCosts.toString())
            }
            .globalPlaceholder("leaderboard_mobcoins") { argumentQueue, _ ->
                val position = argumentQueue.popOr("need position").value()
                val type = argumentQueue.popOr("need type").value()

                plugin.leaderboardManager.getTopMobCoinsPosition(position.toInt())?.let { user ->
                    Tag.preProcessParsed(
                        when (type.lowercase()) {
                            "name" -> user.username
                            "value" -> user.coins.toString()
                            "value_formatted" -> user.coinsPretty
                            else -> "null"
                        }
                    )
                } ?: Tag.preProcessParsed("...")
            }
            .globalPlaceholder("leaderboard_mobcoins_grind") { argumentQueue, _ ->
                val position = argumentQueue.popOr("need position").value()
                val type = argumentQueue.popOr("need type").value()

                plugin.leaderboardManager.getTopMobCoinsGrindPosition(position.toInt())?.let { user ->
                    Tag.preProcessParsed(
                        when (type.lowercase()) {
                            "name" -> user.username
                            "value" -> user.coins.toString()
                            "value_formatted" -> user.coinsPretty
                            else -> "null"
                        }
                    )
                } ?: Tag.preProcessParsed("...")
            }
            .audiencePlaceholder("balance") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(user.coinsAsDouble.toString())
            }
            .audiencePlaceholder("balance_formatted") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(user.coinsPretty)
            }
            .audiencePlaceholder("balance_fixed") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(NumberFormatter.FIXED_FORMAT.format(user.coinsAsDouble))
            }
            .audiencePlaceholder("balance_commas") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(NumberFormatter.COMMAS_FORMAT.format(user.coinsAsDouble))
            }
            .audiencePlaceholder("balance_formatted_compact") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(NumberFormatter.compactDecimalFormat(user.coins))
            }
            .audiencePlaceholder("collected") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(user.coinsCollectedAsDouble.toString())
            }
            .audiencePlaceholder("collected_formatted") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(user.coinsCollectedPretty)
            }
            .audiencePlaceholder("collected_fixed") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(NumberFormatter.FIXED_FORMAT.format(user.coinsCollectedAsDouble))
            }
            .audiencePlaceholder("collected_commas") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(NumberFormatter.COMMAS_FORMAT.format(user.coinsCollectedAsDouble))
            }
            .audiencePlaceholder("collected_formatted_compact") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(NumberFormatter.compactDecimalFormat(user.coinsCollected))
            }
            .audiencePlaceholder("spent") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(user.coinsSpentAsDouble.toString())
            }
            .audiencePlaceholder("spent_formatted") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(user.coinsSpentPretty)
            }
            .audiencePlaceholder("spent_fixed") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(NumberFormatter.FIXED_FORMAT.format(user.coinsSpentAsDouble))
            }
            .audiencePlaceholder("spent_commas") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(NumberFormatter.COMMAS_FORMAT.format(user.coinsSpentAsDouble))
            }
            .audiencePlaceholder("spent_formatted_compact") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(NumberFormatter.compactDecimalFormat(user.coinsSpent))
            }
            .build()
        expansion.register()
        isLoaded = true
        plugin.logger.info("Successfully loaded $pluginName hook!")
    }

    override fun unload() {
        if (this::expansion.isInitialized && expansion.registered()) {
            expansion.unregister()
        }
        isLoaded = false
    }
}