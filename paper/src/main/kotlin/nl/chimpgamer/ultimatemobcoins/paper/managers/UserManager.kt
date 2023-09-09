package nl.chimpgamer.ultimatemobcoins.paper.managers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.storage.user.UserEntity
import nl.chimpgamer.ultimatemobcoins.paper.storage.user.UsersTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.math.MathContext
import java.util.UUID

class UserManager(private val plugin: UltimateMobCoinsPlugin) {

    fun onLogin(playerUUID: UUID, username: String) {
        val user = getByUUID(playerUUID)
        if (user == null) {
            transaction {
                UserEntity.new(playerUUID) {
                    this.username = username
                    this.coins = plugin.settingsConfig.mobCoinsStartingBalance.toBigDecimal(MathContext(3))
                    this.coinsCollected = BigDecimal.ZERO
                    this.coinsSpent = BigDecimal.ZERO
                }
            }
        } else {
            transaction {
                user.username = username
            }
        }
    }

    fun getByUUID(playerUUID: UUID): UserEntity? {
        return transaction { UserEntity.findById(playerUUID) }
    }

    suspend fun getTopMobCoins(): List<UserEntity> = withContext(Dispatchers.IO) {
        transaction {
            UserEntity.all().orderBy(UsersTable.coins to SortOrder.DESC).toList()
        }
    }

    suspend fun getGrindTop(): List<UserEntity> = withContext(Dispatchers.IO) {
        transaction {
            UserEntity.all().orderBy(UsersTable.coinsCollected to SortOrder.DESC).toList()
        }
    }
}