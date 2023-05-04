package nl.chimpgamer.ultimatemobcoins.paper.utils

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.Duration

object Utils {
    fun executeCommands(commands: List<String>) =
        commands.forEach { command -> executeCommand(command) }

    fun executeCommand(command: String) = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)

    fun applyPlaceholders(text: String, player: Player?): String {
        var result = text
        if (player != null) {
            result = result
                .replace("%player_name%", player.name)
                .replace("%player_displayname%", player.displayName)
                .replace("%player_uuid%", player.uniqueId.toString())
                .replace("%player_world%", player.world.name)
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            result = PlaceholderAPI.setPlaceholders(player, result)
        }

        return result
    }

    fun formatDuration(duration: Duration): String {
        var result = ""
        val hoursPart = duration.toHoursPart()
        val minutesPart = duration.toMinutesPart()
        val secondsPart = duration.toSecondsPart()
        if (hoursPart > 0) {
            result += if (hoursPart > 1) {
                "$hoursPart hours "
            } else {
                "$hoursPart hour "
            }
        }
        if (minutesPart > 0) {
            result += if (minutesPart > 1) {
                "$minutesPart minutes "
            } else {
                "$minutesPart minute "
            }
        }
        if (secondsPart > 0) {
            result += if (secondsPart > 1) {
                "$secondsPart seconds "
            } else {
                "$secondsPart second "
            }
        }
        return result.trim().ifEmpty { "0 seconds" }
    }
}