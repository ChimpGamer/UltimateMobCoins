package nl.chimpgamer.ultimatemobcoins.paper.models

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import java.math.BigDecimal
import java.util.UUID

data class User(
    private val plugin: UltimateMobCoinsPlugin,
    val uuid: UUID,
    var username: String,
    var coins: BigDecimal,
    var coinsCollected: BigDecimal,
    var coinsSpent: BigDecimal,
) {
    val coinsAsDouble get() = coins.toDouble()
    val coinsCollectedAsDouble get() = coinsCollected.toDouble()
    val coinsSpentAsDouble get() = coinsSpent.toDouble()

    suspend fun depositCoins(coins: Double) = depositCoins(coins.toBigDecimal())
    suspend fun depositCoins(coinsToDeposit: BigDecimal) {
        plugin.userManager.depositCoins(this, coinsToDeposit)
    }

    suspend fun withdrawCoins(coins: Double) = withdrawCoins(coins.toBigDecimal())
    suspend fun withdrawCoins(coinsToWithdraw: BigDecimal) {
        plugin.userManager.withdrawCoins(this, coinsToWithdraw)
    }

    suspend fun coins(newCoins: BigDecimal) {
        plugin.userManager.setCoins(this, newCoins)
    }

    suspend fun addCoinsCollected(coinsToAdd: Double) = addCoinsCollected(coinsToAdd.toBigDecimal())
    suspend fun addCoinsCollected(coinsToAdd: BigDecimal) {
        plugin.userManager.addCoinsCollected(this, coinsToAdd)
    }

    suspend fun addCoinsSpent(coinsToAdd: Double) = addCoinsSpent(coinsToAdd.toBigDecimal())
    suspend fun addCoinsSpent(coinsToAdd: BigDecimal) {
        plugin.userManager.addCoinsSpent(this, coinsToAdd)
    }
}