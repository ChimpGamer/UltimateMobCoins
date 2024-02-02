package nl.chimpgamer.ultimatemobcoins.paper.managers

import kotlinx.coroutines.*
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.User
import nl.chimpgamer.ultimatemobcoins.paper.storage.user.UserEntity
import nl.chimpgamer.ultimatemobcoins.paper.storage.user.UsersTable
import nl.chimpgamer.ultimatemobcoins.paper.storage.user.toUser
import nl.chimpgamer.ultimatemobcoins.paper.tasks.UserHouseKeeperTask
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.math.MathContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class UserManager(private val plugin: UltimateMobCoinsPlugin) {
    val users: MutableMap<UUID, User> = ConcurrentHashMap()
    val houseKeeper = UserHouseKeeperTask(plugin)

    fun initialize() {
        plugin.server.scheduler.runTaskTimer(plugin, houseKeeper, 1L, 20L * 10L)
    }

    fun loadUser(playerUUID: UUID, username: String) {
        var entity = transaction { UserEntity.findById(playerUUID) }
        if (entity == null) {
            entity = transaction {
                UserEntity.new(playerUUID) {
                    this.username = username
                    this.coins = plugin.settingsConfig.mobCoinsStartingBalance.toBigDecimal(MathContext(3))
                    this.coinsCollected = BigDecimal.ZERO
                    this.coinsSpent = BigDecimal.ZERO
                }
            }
        }
        houseKeeper.registerUsage(playerUUID)
        users[playerUUID] = entity.toUser(plugin)
    }

    fun getIfLoaded(player: Player) = getIfLoaded(player.uniqueId)
    fun getIfLoaded(playerUUID: UUID) = users[playerUUID]

    suspend fun getUser(playerUUID: UUID): User? {
        houseKeeper.registerUsage(playerUUID)
        return if (!users.containsKey(playerUUID)) {
            val entity = suspendedTransactionAsync(Dispatchers.IO) {
                UserEntity.findById(playerUUID)
            }.await()
            entity!!.toUser(plugin).also { users[playerUUID] = it }
        } else {
            users[playerUUID]
        }
    }

    fun unload(playerUUID: UUID) = users.remove(playerUUID)

    suspend fun depositCoins(user: User, coinsToDeposit: BigDecimal) {
        user.apply {
            coins = coins.add(coinsToDeposit)
        }
        newSuspendedTransaction(Dispatchers.IO) {
            val userEntity = UserEntity[user.uuid]
            userEntity.coins = user.coins
        }
    }

    suspend fun depositCoins(user: User, coinsToDeposit: Double) = depositCoins(user, coinsToDeposit.toBigDecimal())

    suspend fun withdrawCoins(user: User, coinsToWithdraw: BigDecimal) {
        user.apply {
            coins = coins.subtract(coinsToWithdraw)
        }
        newSuspendedTransaction(Dispatchers.IO) {
            val userEntity = UserEntity[user.uuid]
            userEntity.coins = user.coins
        }
    }

    suspend fun withdrawCoins(user: User, coinsToWithdraw: Double) = withdrawCoins(user, coinsToWithdraw.toBigDecimal())

    suspend fun setCoins(user: User, newCoins: BigDecimal) {
        user.apply {
            coins = newCoins
        }
        newSuspendedTransaction(Dispatchers.IO) {
            val userEntity = UserEntity[user.uuid]
            userEntity.coins = user.coins
        }
    }

    suspend fun setCoins(user: User, newCoins: Double) = setCoins(user, newCoins.toBigDecimal())

    suspend fun addCoinsCollected(user: User, coinsToAdd: BigDecimal) {
        user.apply {
            coinsCollected = coinsCollected.add(coinsToAdd)
        }
        newSuspendedTransaction(Dispatchers.IO) {
            val userEntity = UserEntity[user.uuid]
            userEntity.coinsCollected = user.coinsCollected
        }
    }

    suspend fun addCoinsCollected(user: User, coinsToAdd: Double) = addCoinsCollected(user, coinsToAdd.toBigDecimal())

    suspend fun addCoinsSpent(user: User, coinsToAdd: BigDecimal) {
        user.apply {
            coinsSpent = coinsSpent.add(coinsToAdd)
        }
        newSuspendedTransaction(Dispatchers.IO) {
            val userEntity = UserEntity[user.uuid]
            userEntity.coinsSpent = user.coinsSpent
        }
    }

    suspend fun addCoinsSpent(user: User, coinsToAdd: Double) = addCoinsSpent(user, coinsToAdd.toBigDecimal())

    suspend fun getTopMobCoins(): List<UserEntity> = newSuspendedTransaction {
        UserEntity.all().orderBy(UsersTable.coins to SortOrder.DESC).toList()
    }

    suspend fun getGrindTop(): List<UserEntity> = newSuspendedTransaction {
        UserEntity.all().orderBy(UsersTable.coinsCollected to SortOrder.DESC).toList()
    }
}