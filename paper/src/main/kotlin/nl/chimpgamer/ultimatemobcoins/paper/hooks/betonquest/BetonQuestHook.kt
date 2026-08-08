package nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.hooks.PluginHook
import nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.actions.MobCoinsActionFactory
import nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.conditions.MobCoinsBalanceConditionFactory
import nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.conditions.MobCoinsCollectedConditionFactory
import nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.conditions.MobCoinsSpentConditionFactory
import nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.objectives.MobCoinsReceiveObjectiveFactory
import org.betonquest.betonquest.api.BetonQuestApiService

class BetonQuestHook(plugin: UltimateMobCoinsPlugin) : PluginHook(plugin, "BetonQuest") {

    override fun load() {
        if (canHook()) {
            val servicesManager = plugin.server.servicesManager
            val apiService = servicesManager.load(BetonQuestApiService::class.java)
            if (apiService == null) {
                plugin.logger.warning("Could not load service for BetonQuest")
                return
            }

            val betonQuestApi = apiService.api(plugin)

            betonQuestApi.actions().registry().register("mobcoin", MobCoinsActionFactory())
            betonQuestApi.conditions().registry()
                .apply {
                    register("mobcoinsbalance", MobCoinsBalanceConditionFactory())
                    register("mobcoinscollected", MobCoinsCollectedConditionFactory())
                    register("mobcoinsspent", MobCoinsSpentConditionFactory())
                }

            betonQuestApi.objectives().registry().register("mobcoinsreceive", MobCoinsReceiveObjectiveFactory(betonQuestApi.loggerFactory(), betonQuestApi.localizations()))



            plugin.logger.info("Successfully loaded $pluginName hook!")
            isLoaded = true
        }
    }

    override fun unload() {
        isLoaded = false
    }
}