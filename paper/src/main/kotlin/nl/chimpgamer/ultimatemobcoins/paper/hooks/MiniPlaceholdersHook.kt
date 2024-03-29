package nl.chimpgamer.ultimatemobcoins.paper.hooks

import io.github.miniplaceholders.api.Expansion
import net.kyori.adventure.text.minimessage.tag.Tag
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.entity.Player

class MiniPlaceholdersHook(private val plugin: UltimateMobCoinsPlugin) {
    private val name = "MiniPlaceholders"
    private val isPluginEnabled = plugin.server.pluginManager.isPluginEnabled(name)
    private var isLoaded = false

    private lateinit var expansion: Expansion

    fun load() {
        if (isLoaded || !isPluginEnabled) return

        expansion = Expansion.builder("ultimatemobcoins")
            .filter(Player::class.java)

            .globalPlaceholder("shop_refresh_time") { argumentQueue, _ ->
                val shopName = argumentQueue.popOr("shop_refresh_time tag requires a valid rotating shop name.").value()
                val menu = plugin.shopMenus[shopName] ?: return@globalPlaceholder null

                Tag.preProcessParsed(plugin.formatDuration(menu.getTimeRemaining()))
            }
            .globalPlaceholder("spinner_prize") { _, _ ->
                Tag.preProcessParsed(plugin.spinnerManager.usageCosts.toString())
            }
            .audiencePlaceholder("balance") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(user.coinsAsDouble.toString())
            }
            .audiencePlaceholder("collected") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(user.coinsCollectedAsDouble.toString())
            }
            .audiencePlaceholder("spent") { audience, _, _ ->
                audience as Player
                val user = plugin.userManager.getIfLoaded(audience) ?: return@audiencePlaceholder null
                Tag.preProcessParsed(user.coinsSpentAsDouble.toString())
            }
            .build()
        expansion.register()
        isLoaded = true
        plugin.logger.info("Successfully loaded $name hook!")
    }

    fun unload() {
        if (this::expansion.isInitialized && expansion.registered()) {
            expansion.unregister()
        }
        isLoaded = false
    }
}