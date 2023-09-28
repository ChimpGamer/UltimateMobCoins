package nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.objectives

import nl.chimpgamer.ultimatemobcoins.paper.events.MobCoinsRedeemEvent
import nl.chimpgamer.ultimatemobcoins.paper.extensions.registerEvents
import org.betonquest.betonquest.Instruction
import org.betonquest.betonquest.api.Objective
import org.betonquest.betonquest.api.profiles.Profile
import org.betonquest.betonquest.utils.PlayerConverter
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class MobCoinsRedeemObjective(instruction: Instruction) : Objective(instruction), Listener {
    //private val log: BetonQuestLogger = BetonQuest.getInstance().loggerFactory.create(this::class.java)
    private var cancel = false
    private var amount: Double = -1.0

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onMobCoinsReceive(event: MobCoinsRedeemEvent) {
        if (cancel) {
            event.isCancelled = true
            return
        }
        if (amount != -1.0 && amount > 0.0) {
            event.amount = amount.toBigDecimal()
        }
        val onlineProfile = PlayerConverter.getID(event.player)
        if (containsPlayer(onlineProfile) && checkConditions(onlineProfile)) {
            completeObjective(onlineProfile)
        }
    }

    init {
        template = ObjectiveData::class.java
        cancel = instruction.hasArgument("cancel")
        amount = instruction.getDouble("amount", -1.0)
    }

    override fun start() {
        JavaPlugin.getProvidingPlugin(javaClass).registerEvents(this)
    }

    override fun stop() {
        HandlerList.unregisterAll(this)
    }

    override fun getDefaultDataInstruction(): String {
        return ""
    }

    override fun getProperty(name: String?, profile: Profile?): String {
        return ""
    }
}