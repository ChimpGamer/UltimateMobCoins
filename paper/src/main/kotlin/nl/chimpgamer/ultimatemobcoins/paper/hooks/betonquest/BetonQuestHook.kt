package nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.conditions.MobCoinsBalanceCondition
import nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.conditions.MobCoinsCollectedCondition
import nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.conditions.MobCoinsSpentCondition
import nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.events.MobCoinsBalanceEvent
import nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.objectives.MobCoinsReceiveObjective
import org.betonquest.betonquest.BetonQuest

class BetonQuestHook(private val plugin: UltimateMobCoinsPlugin) {
    private val name = "BetonQuest"
    private val isPluginEnabled get() = plugin.server.pluginManager.isPluginEnabled(name)

    private var hookEnabled: Boolean = false

    fun load() {
        if (!hookEnabled && isPluginEnabled) {
            val betonQuest = BetonQuest.getInstance()
            betonQuest.registerObjectives("mobcoinsreceive", MobCoinsReceiveObjective::class.java)

            betonQuest.registerEvents("mobcoinsbalance", MobCoinsBalanceEvent::class.java)

            betonQuest.registerConditions("mobcoinsbalance", MobCoinsBalanceCondition::class.java)
            betonQuest.registerConditions("mobcoinscollected", MobCoinsCollectedCondition::class.java)
            betonQuest.registerConditions("mobcoinsspent", MobCoinsSpentCondition::class.java)
            plugin.logger.info("Successfully loaded $name hook!")
            hookEnabled = true
        }
    }
}