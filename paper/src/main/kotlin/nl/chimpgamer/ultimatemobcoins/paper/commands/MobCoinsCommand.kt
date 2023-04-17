package nl.chimpgamer.ultimatemobcoins.paper.commands

import cloud.commandframework.CommandManager
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MobCoinsCommand(private val plugin: UltimateMobCoinsPlugin) {

    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val basePermission = "ultimatemobcoins.command.mobcoins"

        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermission)

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
                sender.sendMessage("<gold>You have <yellow><coins><gold>, you collected <yellow><coins_collected><gold> coins and you spent <yellow><coins_spent><gold> coins!".parse(replacements))
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
    }
}