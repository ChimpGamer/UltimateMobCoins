package nl.chimpgamer.ultimatemobcoins.paper.commands.parsers

import nl.chimpgamer.ultimatemobcoins.paper.commands.InvalidPlayerIdentifierException
import nl.chimpgamer.ultimatemobcoins.paper.utils.BrigadierUtils
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.BukkitCommandContextKeys
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.standard.StringParser.quotedStringParser
import org.incendo.cloud.suggestion.Suggestion
import java.util.UUID
import java.util.concurrent.CompletableFuture

object PlayerArgument {

    fun onlinePlayer(name: String): CommandComponent.Builder<CommandSender, Player> {
        return prepareParser<CommandSender, Player>(name)
            .parser(quotedStringParser<CommandSender>().flatMapSuccess(Player::class.java) { _, input ->
                val player: Player?
                if (input.length > 16) {
                    // Excepting an uuid now...
                    if (input.length != 32 && input.length != 36) {
                        // Neither UUID without dashes nor with dashes.
                        return@flatMapSuccess ArgumentParseResult.failureFuture(
                            InvalidPlayerIdentifierException(
                                "Expected player name/UUID"
                            )
                        )
                    }

                    val uuid = runCatching { UUID.fromString(input) }.getOrNull()
                    if (uuid == null) {
                        return@flatMapSuccess ArgumentParseResult.failureFuture(
                            InvalidPlayerIdentifierException(
                                "Invalid UUID '$input'"
                            )
                        )
                    }
                    player = Bukkit.getPlayer(uuid)
                } else {
                    // This should be a username
                    player = Bukkit.getPlayer(input)
                }
                if (player == null) {
                    return@flatMapSuccess ArgumentParseResult.failureFuture(
                        InvalidPlayerIdentifierException(
                            "Invalid player '$input'"
                        )
                    )
                }

                ArgumentParseResult.successFuture(player)
            })
    }

    fun offlinePlayer(name: String): CommandComponent.Builder<CommandSender, OfflinePlayer> {
        return prepareParser<CommandSender, OfflinePlayer>(name)
            .parser(quotedStringParser<CommandSender>().flatMapSuccess(OfflinePlayer::class.java) { _, input ->
                val player: OfflinePlayer
                if (input.length > 16) {
                    // Excepting an uuid now...
                    if (input.length != 32 && input.length != 36) {
                        // Neither UUID without dashes nor with dashes.
                        return@flatMapSuccess ArgumentParseResult.failureFuture(
                            InvalidPlayerIdentifierException(
                                "Expected player name/UUID"
                            )
                        )
                    }

                    val uuid = runCatching { UUID.fromString(input) }.getOrNull()
                    if (uuid == null) {
                        return@flatMapSuccess ArgumentParseResult.failureFuture(
                            InvalidPlayerIdentifierException(
                                "Invalid UUID '$input'"
                            )
                        )
                    }
                    player = Bukkit.getOfflinePlayer(uuid)
                } else {
                    // This should be a username
                    player = Bukkit.getOfflinePlayer(input)
                }

                ArgumentParseResult.successFuture(player)
            })
    }

    private fun <C, T> prepareParser(name: String): CommandComponent.Builder<C, T> {
        return CommandComponent.builder<C, T>()
            .name(name)
            .suggestionProvider { context, input ->
                val quoted = input.remainingInput().startsWith("\"")
                val bukkit = context.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER)
                val suggestions = Bukkit.getOnlinePlayers()
                    .filter { player -> bukkit !is Player || bukkit.canSee(player!!) }
                    .map { player -> BrigadierUtils.escapeIfRequired(player.name, quoted) }
                    .map { suggestion -> Suggestion.suggestion(suggestion) }
                    .toList()
                CompletableFuture.completedFuture(suggestions)
            }
    }
}