package nl.chimpgamer.ultimatemobcoins.paper.managers

import kotlinx.coroutines.*
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.storage.user.UserEntity
import nl.chimpgamer.ultimatemobcoins.paper.storage.user.UsersTable
import nl.chimpgamer.ultimatemobcoins.paper.tasks.UserHouseKeeperTask
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.math.MathContext
import java.util.UUID

class UserManager(private val plugin: UltimateMobCoinsPlugin) {
    val cache = HashMap<UUID, Deferred<UserEntity?>>()
    val houseKeeper = UserHouseKeeperTask(plugin)

    fun initialize() {
        plugin.server.scheduler.runTaskTimer(plugin, houseKeeper, 1L, 20L * 10L)
    }

    suspend fun onLogin(playerUUID: UUID, username: String) {
        val user = getByUUID(playerUUID)
        if (user == null) {
            coroutineScope {
                cache[playerUUID] = async(Dispatchers.IO) {
                    transaction {
                        UserEntity.new(playerUUID) {
                            this.username = username
                            this.coins = plugin.settingsConfig.mobCoinsStartingBalance.toBigDecimal(MathContext(3))
                            this.coinsCollected = BigDecimal.ZERO
                            this.coinsSpent = BigDecimal.ZERO
                        }
                    }
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                transaction {
                    user.username = username
                }
            }
        }
    }

    fun getIfLoaded(player: Player) = getIfLoaded(player.uniqueId)
    fun getIfLoaded(playerUUID: UUID) = runCatching { cache[playerUUID]?.getCompleted() }.getOrNull()

    suspend fun getByUUID(player: Player): UserEntity? = getByUUID(player.uniqueId)

    suspend fun getByUUID(playerUUID: UUID): UserEntity? {
        houseKeeper.registerUsage(playerUUID)
        return coroutineScope {
            if (!cache.containsKey(playerUUID)) {
                cache[playerUUID] = async(Dispatchers.IO) {
                    transaction { UserEntity.findById(playerUUID) }
                }
            }

            cache[playerUUID]!!.await()
        }
    }

    fun unload(playerUUID: UUID) = cache.remove(playerUUID)

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