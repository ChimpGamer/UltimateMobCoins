package nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.objectives

import nl.chimpgamer.ultimatemobcoins.paper.events.MobCoinsReceiveEvent
import nl.chimpgamer.ultimatemobcoins.paper.extensions.registerEvents
import org.betonquest.betonquest.Instruction
import org.betonquest.betonquest.VariableNumber
import org.betonquest.betonquest.api.Objective
import org.betonquest.betonquest.api.profiles.Profile
import org.betonquest.betonquest.utils.PlayerConverter
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

open class MobCoinsReceiveObjective(instruction: Instruction) : Objective(instruction), Listener {
    private var targetAmount: VariableNumber

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onMobCoinsReceive(event: MobCoinsReceiveEvent) {
        val onlineProfile = PlayerConverter.getID(event.player)
        if (containsPlayer(onlineProfile) && checkConditions(onlineProfile)) {
            val data = dataMap[onlineProfile] as MobCoinData
            data.add(event.amount.toDouble())

            if (data.isCompleted) {
                completeObjective(onlineProfile)
            }
        }
    }

    init {
        template = MobCoinData::class.java
        targetAmount = instruction.varNum
    }

    override fun start() {
        JavaPlugin.getProvidingPlugin(javaClass).registerEvents(this)
    }

    override fun stop() {
        HandlerList.unregisterAll(this)
    }

    override fun getDefaultDataInstruction(): String {
        return targetAmount.toString()
    }

    override fun getDefaultDataInstruction(profile: Profile?): String {
        val value = targetAmount.getDouble(profile)
        return if (value > 0) value.toString() else "1"
    }

    override fun getProperty(name: String, profile: Profile): String {
        return when (name.lowercase()) {
            "amount" -> (dataMap[profile] as MobCoinData).amount.toString()
            "left" -> {
                val data = dataMap[profile] as MobCoinData
                (data.targetAmount - data.amount).toString()
            }
            "total" -> (dataMap[profile] as MobCoinData).targetAmount.toString()
            else -> ""
        }
    }


    protected class MobCoinData(instruction: String, profile: Profile?, objID: String?) :
        ObjectiveData(instruction, profile, objID) {
        val targetAmount: Double = instruction.toDouble()
        var amount = 0.0

        fun add(amount: Double) {
            this.amount += amount
            update()
        }

        val isCompleted: Boolean
            get() = amount >= targetAmount

        override fun toString(): String {
            return "$amount/$targetAmount"
        }
    }

}