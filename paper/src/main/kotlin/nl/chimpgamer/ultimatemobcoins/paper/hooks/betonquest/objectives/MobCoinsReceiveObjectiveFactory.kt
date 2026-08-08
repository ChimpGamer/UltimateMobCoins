package nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.objectives

import nl.chimpgamer.ultimatemobcoins.paper.events.MobCoinsReceiveEvent
import org.betonquest.betonquest.api.config.Localizations
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.quest.objective.Objective
import org.betonquest.betonquest.api.quest.objective.ObjectiveFactory
import org.betonquest.betonquest.api.quest.objective.service.ObjectiveService
import org.betonquest.betonquest.quest.action.IngameNotificationSender
import org.betonquest.betonquest.quest.action.NotificationLevel

class MobCoinsReceiveObjectiveFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
    private val localizations: Localizations
) : ObjectiveFactory {

    override fun parseInstruction(instruction: Instruction, service: ObjectiveService): Objective {
        val targetAmount = instruction.number().atLeast(1).get()
        val log = loggerFactory.create(MobCoinsReceiveObjective::class.java)
        val paymentSender = IngameNotificationSender(log, localizations, instruction.`package`, instruction.id.full,
            NotificationLevel.INFO, "payment_to_receive")

        val objective = MobCoinsReceiveObjective(service, targetAmount, paymentSender)
        service.request(MobCoinsReceiveEvent::class.java).handler(objective::onMobCoinsReceive)
            .offlinePlayer(MobCoinsReceiveEvent::player).subscribe(true)

        return objective
    }
}