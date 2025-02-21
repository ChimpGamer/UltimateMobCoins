package nl.chimpgamer.ultimatemobcoins.paper.utils

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object Utils {
    val actionTypeRegex = Regex("^\\[[^]\\[]*]")
    private val placeholderRegex = Regex("%([^%]+)%")

    fun containsPlaceholder(text: String) = placeholderRegex.containsMatchIn(text)

    fun executeCommands(commands: List<String>) =
        commands.forEach { command -> executeCommand(command) }

    fun executeCommand(command: String) = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)

    fun applyPlaceholders(text: String, player: Player?): String {
        var result = text
        if (player != null) {
            result = result
                .replace("%player_name%", player.name)
                .replace("%player_uuid%", player.uniqueId.toString())
                .replace("%player_world%", player.world.name)
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            result = PlaceholderAPI.setPlaceholders(player, result)
        }

        return result
    }
}