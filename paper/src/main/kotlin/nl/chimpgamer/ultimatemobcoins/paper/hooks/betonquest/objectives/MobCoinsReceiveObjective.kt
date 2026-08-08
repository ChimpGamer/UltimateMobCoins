package nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.objectives

import nl.chimpgamer.ultimatemobcoins.paper.events.MobCoinsReceiveEvent
import nl.chimpgamer.ultimatemobcoins.paper.extensions.toComponent
import org.betonquest.betonquest.api.DefaultObjective
import org.betonquest.betonquest.api.QuestException
import org.betonquest.betonquest.api.common.component.VariableReplacement
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.argument.parser.NumberParser
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.api.quest.objective.service.ObjectiveService
import org.betonquest.betonquest.quest.action.IngameNotificationSender

open class MobCoinsReceiveObjective(service: ObjectiveService,
                                    private val targetAmount: Argument<Number>, private val paymentSender: IngameNotificationSender) : DefaultObjective(service){

    @Throws(QuestException::class)
    fun onMobCoinsReceive(event: MobCoinsReceiveEvent, profile: Profile) {
        val previousAmount = getMobCoinData(profile).current
        add(profile, event.amount.toDouble())

        if (isCompleted(profile)) {
            service.complete(profile)
            return
        }

        val interval = service.serviceDataProvider.getNotificationInterval(profile)
        if (interval > 0 && (getMobCoinData(profile).current.toInt()) / interval != (previousAmount.toInt()) / interval && profile.onlineProfile
                .isPresent
        ) {
            paymentSender.sendNotification(
                profile,
                VariableReplacement("amount", getRemainingAmount(profile).toComponent())
            )
        }
    }

    @Throws(QuestException::class)
    private fun getRemainingAmount(profile: Profile): Double {
        val amount = getMobCoinData(profile)
        return amount.target - amount.current
    }

    @Throws(QuestException::class)
    private fun isCompleted(profile: Profile): Boolean {
        val amount = getMobCoinData(profile)
        return amount.current >= amount.target
    }

    @Throws(QuestException::class)
    private fun add(profile: Profile, toAdd: Double) {
        val amount = getMobCoinData(profile)
        val newAmount: Double = amount.current + toAdd
        service.data[profile] = newAmount.toString() + "/" + amount.target
        service.updateData(profile)
    }

    @Throws(QuestException::class)
    private fun getMobCoinData(profile: Profile): MobCoinData {
        val stringData = service.data[profile] ?: throw QuestException("Profile should have data!")
        val split = stringData.split('/')
        val amount: Double
        val targetAmount: Double
        val initLength = 1
        if (split.count() == initLength) {
            amount = 0.0
            targetAmount = NumberParser.DEFAULT.apply(split[0]).toDouble()
        } else {
            amount = NumberParser.DEFAULT.apply(split[0]).toDouble()
            targetAmount = NumberParser.DEFAULT.apply(split[1]).toDouble()
        }

        return MobCoinData(amount, targetAmount)
    }

    init {
        service.setDefaultData {
            targetAmount.getValue(it!!).toDouble().toString()
        }

        val properties = service.properties
        properties.apply {
            setProperty("amount") { profile -> getMobCoinData(profile).current.toString() }
            setProperty("left") { profile -> getRemainingAmount(profile).toString() }
            setProperty("total") { profile -> getMobCoinData(profile).target.toString() }
        }
    }

    protected data class MobCoinData(val current: Double, val target: Double)
}