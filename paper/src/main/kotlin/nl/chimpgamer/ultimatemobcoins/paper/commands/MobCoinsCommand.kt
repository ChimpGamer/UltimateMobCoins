package nl.chimpgamer.ultimatemobcoins.paper.commands

import cloud.commandframework.CommandManager
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.extensions.toComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MobCoinsCommand(private val plugin: UltimateMobCoinsPlugin) {

    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val basePermission = "ultimatemobcoins.command.mobcoins"

        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermission)

        val offlinePlayerArgument = OfflinePlayerArgument.of<CommandSender>("player")

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
                sender.sendMessage(
                    "<gold>You have <yellow><coins><gold>, you collected <yellow><coins_collected><gold> coins and you spent <yellow><coins_spent><gold> coins!".parse(
                        replacements
                    )
                )
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
                sender.sendMessage(
                    "<gold>You have <yellow><coins><gold> coins, you collected <yellow><coins_collected><gold> coins and you spent <yellow><coins_spent><gold> coins!".parse(
                        replacements
                    )
                )
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
                val displayName = targetPlayer.player?.displayName() ?: targetPlayer.name?.toComponent() ?: return@handler
                val replacements = mapOf(
                    "displayname" to displayName,
                    "coins" to user.coinsAsDouble,
                    "coins_collected" to user.coinsCollectedAsDouble,
                    "coins_spent" to user.coinsSpentAsDouble
                )
                sender.sendMessage("<gold><displayname> has <yellow><coins><gold> coins, collected <yellow><coins_collected><gold> coins and spent <yellow><coins_spent><gold> coins!".parse(replacements))
            }
        )
    }
}