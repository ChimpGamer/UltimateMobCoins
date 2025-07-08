package nl.chimpgamer.ultimatemobcoins.paper.managers

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.User
import nl.chimpgamer.ultimatemobcoins.paper.tasks.UserHouseKeeperTask
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

class UserManager(private val plugin: UltimateMobCoinsPlugin) {
    val users: MutableMap<UUID, User> = ConcurrentHashMap()
    val houseKeeper = UserHouseKeeperTask(plugin)

    fun initialize() {
        plugin.launch(plugin.globalRegionDispatcher, CoroutineStart.UNDISPATCHED) {
            delay(1.ticks)
            while (true) {
                delay(10.seconds)
                houseKeeper.run()
            }
        }
    }

    suspend fun loadUser(playerUUID: UUID, username: String) {
        var user = plugin.databaseManager.userDao.getUser(playerUUID)
        if (user == null) {
            user = plugin.databaseManager.userDao.createUser(playerUUID, username)
        } else {
            if (user.username != username) {
                user.username = username
                plugin.databaseManager.userDao.setUsername(user, username)
            }
        }
        houseKeeper.registerUsage(playerUUID)
        users[playerUUID] = user
    }

    fun getIfLoaded(player: Player) = getIfLoaded(player.uniqueId)
    fun getIfLoaded(playerUUID: UUID) = users[playerUUID]
    fun getIfLoaded(playerName: String) = users.values.find { it.username == playerName }

    suspend fun getUser(playerUUID: UUID): User? {
        houseKeeper.registerUsage(playerUUID)
        return if (!users.containsKey(playerUUID)) {
            val user = plugin.databaseManager.userDao.getUser(playerUUID) ?: return null
            user.also { users[playerUUID] = user }
        } else {
            users[playerUUID]
        }
    }

    fun unload(playerUUID: UUID) = users.remove(playerUUID)

    private suspend fun updateUserCoins(user: User, newCoins: BigDecimal) {
        user.coins = newCoins
        plugin.databaseManager.userDao.setCoins(user, user.coins)
    }

    private suspend fun updateUserCoinsCollected(user: User, coinsCollected: BigDecimal) {
        user.coinsCollected = coinsCollected
        plugin.databaseManager.userDao.setCoinsCollected(user, user.coinsCollected)
    }

    private suspend fun updateUserCoinsSpent(user: User, coinsSpent: BigDecimal) {
        user.coinsSpent = coinsSpent
        plugin.databaseManager.userDao.setCoinsSpent(user, user.coinsSpent)
    }

    suspend fun depositCoins(user: User, coinsToDeposit: BigDecimal) = updateUserCoins(user, user.coins.add(coinsToDeposit))
    suspend fun depositCoins(user: User, coinsToDeposit: Double) = depositCoins(user, coinsToDeposit.toBigDecimal())
    fun depositCoinsAsync(user: User, coinsToDeposit: BigDecimal) = plugin.executeAsyncOperation { depositCoins(user, coinsToDeposit) }
    fun depositCoinsAsync(userUUID: UUID, coinsToDeposit: BigDecimal) = executeAsyncOperationWithUser(userUUID) { user ->
        depositCoins(user, coinsToDeposit)
    }

    suspend fun withdrawCoins(user: User, coinsToWithdraw: BigDecimal) = updateUserCoins(user, user.coins.subtract(coinsToWithdraw))
    suspend fun withdrawCoins(user: User, coinsToWithdraw: Double) = withdrawCoins(user, coinsToWithdraw.toBigDecimal())
    fun withdrawCoinsAsync(user: User, coinsToDeposit: BigDecimal) = plugin.executeAsyncOperation { withdrawCoins(user, coinsToDeposit) }
    fun withdrawCoinsAsync(userUUID: UUID, coinsToDeposit: BigDecimal) = executeAsyncOperationWithUser(userUUID) { user ->
        withdrawCoins(user, coinsToDeposit)
    }

    suspend fun setCoins(user: User, newCoins: BigDecimal) = updateUserCoins(user, newCoins)
    suspend fun setCoins(user: User, newCoins: Double) = setCoins(user, newCoins.toBigDecimal())
    fun setCoinsAsync(user: User, newCoins: BigDecimal) = plugin.executeAsyncOperation { setCoins(user, newCoins) }
    fun setCoinsAsync(userUUID: UUID, newCoins: BigDecimal) = executeAsyncOperationWithUser(userUUID) { user -> setCoins(user, newCoins) }

    suspend fun addCoinsCollected(user: User, coinsToAdd: BigDecimal) = updateUserCoinsCollected(user, user.coinsCollected.add(coinsToAdd))
    suspend fun addCoinsCollected(user: User, coinsToAdd: Double) = addCoinsCollected(user, coinsToAdd.toBigDecimal())
    fun addCoinsCollectedAsync(user: User, coinsToAdd: BigDecimal) = plugin.executeAsyncOperation { addCoinsCollected(user, coinsToAdd) }
    fun addCoinsCollectedAsync(userUUID: UUID, coinsToAdd: BigDecimal) = executeAsyncOperationWithUser(userUUID) { user ->
        addCoinsCollected(user, coinsToAdd)
    }

    suspend fun addCoinsSpent(user: User, coinsToAdd: BigDecimal) = updateUserCoinsSpent(user, user.coinsSpent.add(coinsToAdd))
    suspend fun addCoinsSpent(user: User, coinsToAdd: Double) = addCoinsSpent(user, coinsToAdd.toBigDecimal())
    fun addCoinsSpentAsync(user: User, coinsToAdd: BigDecimal) = plugin.executeAsyncOperation { addCoinsSpent(user, coinsToAdd) }
    fun addCoinsSpentAsync(userUUID: UUID, coinsToAdd: BigDecimal) = executeAsyncOperationWithUser(userUUID) { user ->
        addCoinsSpent(user, coinsToAdd)
    }

    suspend fun getTopMobCoins() = plugin.databaseManager.userDao.getTopMobCoins()
    suspend fun getGrindTop() = plugin.databaseManager.userDao.getGrindTop()

    private fun executeAsyncOperationWithUser(userUUID: UUID, operation: suspend (User) -> Unit) {
        plugin.executeAsyncOperation {val user = getUser(userUUID) ?: return@executeAsyncOperation
            operation(user)
        }
    }
}