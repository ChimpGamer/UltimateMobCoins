package nl.chimpgamer.ultimatemobcoins.paper.hooks

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.OfflinePlayer

class PlaceholderAPIHook(private val plugin: UltimateMobCoinsPlugin) : PlaceholderExpansion() {

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (player == null) return null
        if (params.equals("balance", ignoreCase = true)) {
            val user = plugin.userManager.getByUUID(player.uniqueId) ?: return null
            return user.coinsAsDouble.toString()
        }
        if (params.equals("collected", ignoreCase = true)) {
            val user = plugin.userManager.getByUUID(player.uniqueId) ?: return null
            return user.coinsCollectedAsDouble.toString()
        }
        if (params.equals("spent", ignoreCase = true)) {
            val user = plugin.userManager.getByUUID(player.uniqueId) ?: return null
            return user.coinsSpentAsDouble.toString()
        }
        return null
    }

    override fun getIdentifier(): String = "ultimatemobcoins"

    override fun getAuthor(): String = plugin.authors.joinToString()

    override fun getVersion(): String = plugin.version

    override fun persist(): Boolean = true
}