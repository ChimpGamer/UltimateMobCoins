package nl.chimpgamer.ultimatemobcoins.paper.managers

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.User
import nl.chimpgamer.ultimatemobcoins.paper.tasks.UserHouseKeeperTask
import nl.chimpgamer.ultimatemobcoins.paper.utils.ExpiringMap
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
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

    suspend fun depositCoins(user: User, coinsToDeposit: BigDecimal) {
        user.apply {
            coins = coins.add(coinsToDeposit)
        }
        plugin.databaseManager.userDao.setCoins(user, user.coins)
    }

    suspend fun depositCoins(user: User, coinsToDeposit: Double) = depositCoins(user, coinsToDeposit.toBigDecimal())

    suspend fun withdrawCoins(user: User, coinsToWithdraw: BigDecimal) {
        user.apply {
            coins = coins.subtract(coinsToWithdraw)
        }
        plugin.databaseManager.userDao.setCoins(user, user.coins)
    }

    suspend fun withdrawCoins(user: User, coinsToWithdraw: Double) = withdrawCoins(user, coinsToWithdraw.toBigDecimal())

    suspend fun setCoins(user: User, newCoins: BigDecimal) {
        user.apply {
            coins = newCoins
        }
        plugin.databaseManager.userDao.setCoins(user, user.coins)
    }

    suspend fun setCoins(user: User, newCoins: Double) = setCoins(user, newCoins.toBigDecimal())

    suspend fun addCoinsCollected(user: User, coinsToAdd: BigDecimal) {
        user.apply {
            coinsCollected = coinsCollected.add(coinsToAdd)
        }
        plugin.databaseManager.userDao.setCoinsCollected(user, user.coinsCollected)
    }

    suspend fun addCoinsCollected(user: User, coinsToAdd: Double) = addCoinsCollected(user, coinsToAdd.toBigDecimal())

    suspend fun addCoinsSpent(user: User, coinsToAdd: BigDecimal) {
        user.apply {
            coinsSpent = coinsSpent.add(coinsToAdd)
        }
        plugin.databaseManager.userDao.setCoinsSpent(user, user.coinsSpent)
    }

    suspend fun addCoinsSpent(user: User, coinsToAdd: Double) = addCoinsSpent(user, coinsToAdd.toBigDecimal())

    suspend fun getTopMobCoins() = plugin.databaseManager.userDao.getTopMobCoins()

    suspend fun getGrindTop() = plugin.databaseManager.userDao.getGrindTop()
}