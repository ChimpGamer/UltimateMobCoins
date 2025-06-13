package nl.chimpgamer.ultimatemobcoins.paper.storage.dao

import nl.chimpgamer.ultimatemobcoins.paper.models.User
import java.math.BigDecimal
import java.util.UUID

interface UserDao {

    suspend fun getAll(): Set<User>

    suspend fun getUser(uuid: UUID): User?

    suspend fun createUser(uuid: UUID, username: String): User

    suspend fun setUsername(user: User, username: String)

    suspend fun setCoins(user: User, coins: BigDecimal)

    suspend fun setCoinsCollected(user: User, coinsCollected: BigDecimal)

    suspend fun setCoinsSpent(user: User, coinsSpent: BigDecimal)

    suspend fun getTopMobCoins(): List<User>

    suspend fun getGrindTop(): List<User>
}