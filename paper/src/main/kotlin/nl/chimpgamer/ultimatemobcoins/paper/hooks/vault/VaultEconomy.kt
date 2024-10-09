package nl.chimpgamer.ultimatemobcoins.paper.hooks.vault

import com.github.shynixn.mccoroutine.folia.launch
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.utils.NumberFormatter
import org.bukkit.OfflinePlayer
import java.math.MathContext

class VaultEconomy(private val plugin: UltimateMobCoinsPlugin) : Economy {
    private val banksNotSupportedResponse = EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "UltimateMobCoins does not support bank accounts!");

    override fun isEnabled(): Boolean {
        return plugin.isEnabled && plugin.hooksConfig.vaultEconomy
    }

    override fun getName(): String {
        return plugin.name
    }

    override fun hasBankSupport(): Boolean {
        return false
    }

    override fun fractionalDigits(): Int {
        return -1
    }

    override fun format(amount: Double): String {
        return NumberFormatter.displayCurrency(amount.toBigDecimal())
    }

    override fun currencyNamePlural(): String {
        return "MobCoins"
    }

    override fun currencyNameSingular(): String {
        return "MobCoin"
    }

    @Deprecated("Deprecated in Java")
    override fun hasAccount(playerName: String): Boolean {
        return plugin.userManager.getIfLoaded(playerName) != null
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return plugin.userManager.getIfLoaded(player.uniqueId) != null
    }

    @Deprecated("Deprecated in Java", ReplaceWith("hasAccount(playerName)"))
    override fun hasAccount(playerName: String, worldName: String): Boolean {
        return hasAccount(playerName)
    }

    override fun hasAccount(player: OfflinePlayer, worldName: String): Boolean {
        return hasAccount(player)
    }

    @Deprecated("Deprecated in Java")
    override fun getBalance(playerName: String): Double {
        val user = plugin.userManager.getIfLoaded(playerName) ?: return 0.0
        return user.coinsAsDouble
    }

    override fun getBalance(player: OfflinePlayer): Double {
        val user = plugin.userManager.getIfLoaded(player.uniqueId) ?: return 0.0
        return user.coinsAsDouble
    }

    @Deprecated("Deprecated in Java", ReplaceWith("getBalance(playerName)"))
    override fun getBalance(playerName: String, worldName: String): Double {
        return getBalance(playerName)
    }

    override fun getBalance(player: OfflinePlayer, worldName: String): Double {
        return getBalance(player)
    }

    @Deprecated("Deprecated in Java")
    override fun has(playerName: String, amount: Double): Boolean {
        return plugin.userManager.getIfLoaded(playerName)?.hasEnough(amount.toBigDecimal()) ?: false
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        return plugin.userManager.getIfLoaded(player.uniqueId)?.hasEnough(amount.toBigDecimal()) ?: false
    }

    @Deprecated("Deprecated in Java")
    override fun has(playerName: String, worldName: String, amount: Double): Boolean {
        return plugin.userManager.getIfLoaded(playerName)?.hasEnough(amount.toBigDecimal()) ?: false
    }

    override fun has(player: OfflinePlayer, worldName: String, amount: Double): Boolean {
        return plugin.userManager.getIfLoaded(player.uniqueId)?.hasEnough(amount.toBigDecimal()) ?: false
    }

    @Deprecated("Deprecated in Java")
    override fun withdrawPlayer(playerName: String?, amount: Double): EconomyResponse {
        if (playerName == null) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player name cannot be null!")
        }
        if (amount < 0) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds!")
        }
        val user = plugin.userManager.getIfLoaded(playerName)
            ?: return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "User does not exist!")
        plugin.launch { user.withdrawCoins(amount.toBigDecimal(MathContext(3))) }
        return EconomyResponse(amount, getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, null)
    }

    override fun withdrawPlayer(player: OfflinePlayer?, amount: Double): EconomyResponse {
        if (player == null) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player cannot be null!")
        }
        if (amount < 0) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds!")
        }
        val user = plugin.userManager.getIfLoaded(player.uniqueId)
            ?: return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "User does not exist!")
        plugin.launch { user.withdrawCoins(amount.toBigDecimal(MathContext(3))) }
        return EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("withdrawPlayer(playerName, amount)"))
    override fun withdrawPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        return withdrawPlayer(playerName, amount)
    }

    override fun withdrawPlayer(player: OfflinePlayer, worldName: String, amount: Double): EconomyResponse {
        return withdrawPlayer(player, amount)
    }

    @Deprecated("Deprecated in Java")
    override fun depositPlayer(playerName: String?, amount: Double): EconomyResponse {
        if (playerName == null) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player name can not be null.")
        }
        if (amount < 0) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds")
        }
        val user = plugin.userManager.getIfLoaded(playerName)
            ?: return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "User does not exist!")
        plugin.launch { user.depositCoins(amount.toBigDecimal(MathContext(3))) }
        return EconomyResponse(amount, getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, null)
    }

    override fun depositPlayer(player: OfflinePlayer?, amount: Double): EconomyResponse {
        if (player == null) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player name can not be null.")
        }
        if (amount < 0) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds")
        }
        val user = plugin.userManager.getIfLoaded(player.uniqueId)
            ?: return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "User does not exist!")
        plugin.launch { user.depositCoins(amount.toBigDecimal(MathContext(3))) }
        return EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("depositPlayer(playerName, amount)"))
    override fun depositPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        return depositPlayer(playerName, amount)
    }

    override fun depositPlayer(player: OfflinePlayer, worldName: String, amount: Double): EconomyResponse {
        return depositPlayer(player, amount)
    }

    @Deprecated("Deprecated in Java")
    override fun createBank(p0: String?, p1: String?): EconomyResponse {
        return banksNotSupportedResponse
    }

    override fun createBank(p0: String?, p1: OfflinePlayer?): EconomyResponse {
        return banksNotSupportedResponse
    }

    override fun deleteBank(p0: String?): EconomyResponse {
        return banksNotSupportedResponse
    }

    override fun bankBalance(p0: String?): EconomyResponse {
        return banksNotSupportedResponse
    }

    override fun bankHas(p0: String?, p1: Double): EconomyResponse {
        return banksNotSupportedResponse
    }

    override fun bankWithdraw(p0: String?, p1: Double): EconomyResponse {
        return banksNotSupportedResponse
    }

    override fun bankDeposit(p0: String?, p1: Double): EconomyResponse {
        return banksNotSupportedResponse
    }

    @Deprecated("Deprecated in Java")
    override fun isBankOwner(p0: String?, p1: String?): EconomyResponse {
        return banksNotSupportedResponse
    }

    override fun isBankOwner(p0: String?, p1: OfflinePlayer?): EconomyResponse {
        return banksNotSupportedResponse
    }

    @Deprecated("Deprecated in Java")
    override fun isBankMember(p0: String?, p1: String?): EconomyResponse {
        return banksNotSupportedResponse
    }

    override fun isBankMember(p0: String?, p1: OfflinePlayer?): EconomyResponse {
        return banksNotSupportedResponse
    }

    override fun getBanks(): List<String> {
        return emptyList()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("false"))
    override fun createPlayerAccount(playerName: String): Boolean {
        return false
    }

    override fun createPlayerAccount(player: OfflinePlayer): Boolean {
        return false
    }

    @Deprecated("Deprecated in Java", ReplaceWith("createPlayerAccount(playerName)"))
    override fun createPlayerAccount(playerName: String, worldName: String): Boolean {
        return createPlayerAccount(playerName)
    }

    override fun createPlayerAccount(player: OfflinePlayer, worldName: String): Boolean {
        return createPlayerAccount(player)
    }
}