package nl.chimpgamer.ultimatemobcoins.paper.hooks

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.jetbrains.annotations.Contract
import java.math.BigDecimal

class VaultHook {
    private lateinit var economy: Economy

    fun initialize() {
        val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java) ?: return
        economy = rsp.provider
    }

    fun example() {
        val player = Bukkit.getPlayer("playername") ?: return
        take(player, BigDecimal(100)).handle({ player.sendRichMessage("<green>100$ has been taken from your account!") }) { reason -> player.sendRichMessage(reason)}
    }

    fun get(offlinePlayer: OfflinePlayer): BigDecimal? {
        if (!isInitialized()) return null
        return BigDecimal(economy.getBalance(offlinePlayer))
    }

    fun give(offlinePlayer: OfflinePlayer, amount: BigDecimal): Response {
        if (!isInitialized()) return Response.failing("Vault has not been initialized!")
        val response = economy.depositPlayer(offlinePlayer, amount.toDouble())
        return if (response.transactionSuccess()) Response.passing() else Response.failing(response.errorMessage)
    }

    fun take(offlinePlayer: OfflinePlayer, amount: BigDecimal): Response {
        if (!isInitialized()) return Response.failing("Vault has not been initialized!")
        val response = economy.withdrawPlayer(offlinePlayer, amount.toDouble())
        return if (response.transactionSuccess()) Response.passing() else Response.failing(response.errorMessage)
    }

    fun isInitialized() = this::economy.isInitialized
}

/**
 * Represents a transaction response from Vault
 *
 * @see Response.result
 * @see Response.reason
 */
class Response private constructor(
    /**
     * @return Whether the transaction passed or failed
     */
    val result: Result,
    /**
     * @return The reason the transaction failed, if it failed...
     */
    val reason: String
) {
    /**
     * Represents whether the result of the transaction was passing or failing
     */
    enum class Result {
        PASS,
        FAIL
    }

    /**
     * @return true if this transaction was successful, false otherwise
     */

    val isPassing: Boolean
        get() = result == Result.PASS

    /**
     * @return true if this transaction failed, false otherwise
     */

    val isFailing: Boolean
        get() = result == Result.FAIL

    /**
     * Convenience function for handling the state of this response
     *
     * @param passing What to do if this transaction was successful
     * @param failing What to do if this transaction failed
     */
    fun handle(passing: Runnable, failing: (String) -> Unit) {
        when (result) {
            Result.PASS -> passing.run() // run the passing runnable when this is a passing response
            Result.FAIL -> failing(reason) // provide the reason to the failing consumer when this is a failing response
        }
    }

    companion object {
        /**
         * Creates a new response denoting a successful transaction
         *
         * @return The new response holding [Result.PASS]
         */
        @Contract(value = " -> new", pure = true)
        fun passing(): Response = Response(Result.PASS, "")

        /**
         * Creates a new response denoting a failing transaction
         *
         * @param reason The reason the transaction failed
         * @return The new response holding [Result.FAIL] and a Reason
         */
        @Contract(value = "_ -> new", pure = true)
        fun failing(reason: String): Response = Response(Result.FAIL, reason)
    }
}