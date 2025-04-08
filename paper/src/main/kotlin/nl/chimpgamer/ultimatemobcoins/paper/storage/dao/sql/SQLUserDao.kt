package nl.chimpgamer.ultimatemobcoins.paper.storage.dao.sql

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.User
import nl.chimpgamer.ultimatemobcoins.paper.storage.dao.UserDao
import nl.chimpgamer.ultimatemobcoins.paper.storage.sql.UserEntity
import nl.chimpgamer.ultimatemobcoins.paper.storage.sql.UsersTable
import nl.chimpgamer.ultimatemobcoins.paper.storage.sql.toUser
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.math.BigDecimal
import java.math.MathContext
import java.util.*

class SQLUserDao(private val plugin: UltimateMobCoinsPlugin) : UserDao {
    override suspend fun getAll(): Set<User> {
        return newSuspendedTransaction { UserEntity.all() }.map { it.toUser(plugin) }.toSet()
    }

    override suspend fun getUser(uuid: UUID): User? {
        return newSuspendedTransaction { UserEntity.findById(uuid) }?.toUser(plugin)
    }

    override suspend fun createUser(uuid: UUID, username: String): User {
        return newSuspendedTransaction {
            UserEntity.new(uuid) {
                this.username = username
                this.coins = plugin.settingsConfig.mobCoinsStartingBalance.toBigDecimal(MathContext(3))
                this.coinsCollected = BigDecimal.ZERO
                this.coinsSpent = BigDecimal.ZERO
            }
        }.toUser(plugin)
    }

    override suspend fun setCoins(user: User, coins: BigDecimal) {
        newSuspendedTransaction(plugin.databaseManager.databaseDispatcher) {
            val userEntity = UserEntity[user.uuid]
            userEntity.coins = user.coins
        }
    }

    override suspend fun setCoinsCollected(user: User, coinsCollected: BigDecimal) {
        newSuspendedTransaction(plugin.databaseManager.databaseDispatcher) {
            val userEntity = UserEntity[user.uuid]
            userEntity.coinsCollected = coinsCollected
        }
    }

    override suspend fun setCoinsSpent(user: User, coinsSpent: BigDecimal) {
        newSuspendedTransaction(plugin.databaseManager.databaseDispatcher) {
            val userEntity = UserEntity[user.uuid]
            userEntity.coinsSpent = coinsSpent
        }
    }

    override suspend fun getTopMobCoins(): List<User> {
        return newSuspendedTransaction {
            UserEntity.all().orderBy(UsersTable.coins to SortOrder.DESC).toList()
        }.map { it.toUser(plugin) }
    }

    override suspend fun getGrindTop(): List<User> {
        return newSuspendedTransaction {
            UserEntity.all().orderBy(UsersTable.coinsCollected to SortOrder.DESC).toList()
        }.map { it.toUser(plugin) }
    }
}