package nl.chimpgamer.ultimatemobcoins.paper.commands

import cloud.commandframework.CommandManager
import cloud.commandframework.arguments.standard.DoubleArgument
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.extensions.toComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.math.MathContext

class MobCoinsCommand(private val plugin: UltimateMobCoinsPlugin) {

    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val basePermission = "ultimatemobcoins.command.mobcoins"

        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermission)

        val offlinePlayerArgument = OfflinePlayerArgument.of<CommandSender>("player")
        val amountArgument = DoubleArgument.of<CommandSender>("amount")

        commandManager.command(builder
            .senderType(Player::class.java)
            .handler { context ->
                val sender = context.sender as Player
                val user = plugin.userManager.getByUUID(sender.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${sender.name} (${sender.uniqueId})")
                    return@handler
                }
                val replacements = mapOf(
                    "coins" to user.coinsAsDouble,
                    "coins_collected" to user.coinsCollectedAsDouble,
                    "coins_spent" to user.coinsSpentAsDouble
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsBalance.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("reload")
            .handler { context ->
                val sender = context.sender
                plugin.settingsConfig.config.reload()
                plugin.messagesConfig.config.reload()
                plugin.mobCoinsManager.reload()
                sender.sendRichMessage("<green>Successfully reloaded configs!")
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .literal("balance")
            .permission("$basePermission.balance")
            .handler { context ->
                val sender = context.sender as Player
                val user = plugin.userManager.getByUUID(sender.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${sender.name} (${sender.uniqueId})")
                    return@handler
                }
                val replacements = mapOf(
                    "coins" to user.coinsAsDouble,
                    "coins_collected" to user.coinsCollectedAsDouble,
                    "coins_spent" to user.coinsSpentAsDouble
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsBalance.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("balance")
            .permission("$basePermission.balance.others")
            .argument(offlinePlayerArgument.copy())
            .handler { context ->
                val sender = context.sender
                val targetPlayer = context[offlinePlayerArgument]
                val user = plugin.userManager.getByUUID(targetPlayer.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@handler
                }
                val displayName =
                    targetPlayer.player?.displayName() ?: targetPlayer.name?.toComponent() ?: return@handler
                val replacements = mapOf(
                    "displayname" to displayName,
                    "coins" to user.coinsAsDouble,
                    "coins_collected" to user.coinsCollectedAsDouble,
                    "coins_spent" to user.coinsSpentAsDouble
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsBalanceOthers.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("set")
            .argument(offlinePlayerArgument.copy())
            .argument(amountArgument.copy())
            .handler { context ->
                val sender = context.sender
                val targetPlayer = context[offlinePlayerArgument]
                val user = plugin.userManager.getByUUID(targetPlayer.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@handler
                }
                val amount = context[amountArgument]
                user.setCoins(amount.toBigDecimal(MathContext(3)))
                val replacements = mapOf(
                    "displayname" to (if (sender is Player) sender.displayName() else sender.name()),
                    "amount" to amount
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsSetSender.parse(replacements))
                targetPlayer.player?.sendMessage(plugin.messagesConfig.mobCoinsSetTarget.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("give")
            .argument(offlinePlayerArgument.copy())
            .argument(amountArgument.copy())
            .handler { context ->
                val sender = context.sender
                val targetPlayer = context[offlinePlayerArgument]
                val user = plugin.userManager.getByUUID(targetPlayer.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@handler
                }
                val amount = context[amountArgument]
                user.depositCoins(amount.toBigDecimal(MathContext(3)))
                val replacements = mapOf(
                    "displayname" to (if (sender is Player) sender.displayName() else sender.name()),
                    "amount" to amount
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsGiveSender.parse(replacements))
                targetPlayer.player?.sendMessage(plugin.messagesConfig.mobCoinsGiveTarget.parse(replacements))
            }
        )

        commandManager.command(builder
            .literal("take")
            .argument(offlinePlayerArgument.copy())
            .argument(amountArgument.copy())
            .handler { context ->
                val sender = context.sender
                val targetPlayer = context[offlinePlayerArgument]
                val user = plugin.userManager.getByUUID(targetPlayer.uniqueId)
                if (user == null) {
                    plugin.logger.warning("Something went wrong! Could not get user ${targetPlayer.name} (${targetPlayer.uniqueId})")
                    return@handler
                }
                val amount = context[amountArgument]
                user.withdrawCoins(amount.toBigDecimal(MathContext(3)))
                val replacements = mapOf(
                    "displayname" to (if (sender is Player) sender.displayName() else sender.name()),
                    "amount" to amount
                )
                sender.sendMessage(plugin.messagesConfig.mobCoinsTakeSender.parse(replacements))
                targetPlayer.player?.sendMessage(plugin.messagesConfig.mobCoinsTakeTarget.parse(replacements))
            }
        )
    }
}