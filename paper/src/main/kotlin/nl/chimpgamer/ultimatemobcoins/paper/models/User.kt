package nl.chimpgamer.ultimatemobcoins.paper.models

import nl.chimpgamer.ultimatemobcoins.paper.tables.UsersTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.util.UUID

class User(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<User>(UsersTable)

    var username by UsersTable.username
    var coins by UsersTable.coins
    var coinsCollected by UsersTable.coinsCollected
    var coinsSpent by UsersTable.coinsSpent

    val coinsAsDouble get() = coins.toDouble()
    val coinsCollectedAsDouble get() = coinsCollected.toDouble()
    val coinsSpentAsDouble get() = coinsSpent.toDouble()

    fun depositCoins(coins: Double) = depositCoins(coins.toBigDecimal())
    fun depositCoins(coinsToDeposit: BigDecimal) {
        transaction {
            coins = coins.add(coinsToDeposit)
        }
    }

    fun withdrawCoins(coins: Double) = withdrawCoins(coins.toBigDecimal())
    fun withdrawCoins(coinsToWithdraw: BigDecimal) {
        transaction {
            coins = coins.subtract(coinsToWithdraw)
        }
    }

    fun coins(newCoins: BigDecimal) {
        transaction {
            coins = newCoins
        }
    }

    fun addCoinsCollected(coinsToAdd: BigDecimal) {
        transaction {
            coinsCollected = coinsCollected.add(coinsToAdd)
        }
    }
}