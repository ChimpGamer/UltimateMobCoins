package nl.chimpgamer.ultimatemobcoins.paper.storage.dao.mongodb

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.managers.MongoDBManager
import nl.chimpgamer.ultimatemobcoins.paper.models.User
import nl.chimpgamer.ultimatemobcoins.paper.storage.dao.UserDao
import nl.chimpgamer.ultimatemobcoins.paper.storage.mongodb.MongoUser
import nl.chimpgamer.ultimatemobcoins.paper.storage.mongodb.toUser
import java.math.BigDecimal
import java.math.MathContext
import java.util.*

class MongoDBUserDao(private val plugin: UltimateMobCoinsPlugin) : UserDao {
    private val mongoDBManager get() = plugin.databaseManager as MongoDBManager

    override suspend fun getAll(): Set<User> {
        return mongoDBManager.usersCollection().find().map { it.toUser(plugin) }.toSet()
    }

    override suspend fun getUser(uuid: UUID): User? {
        return mongoDBManager.usersCollection().find(eq("_id", uuid)).firstOrNull()?.toUser(plugin)
    }

    override suspend fun createUser(uuid: UUID, username: String): User {
        val mongoUser = MongoUser(uuid, username, plugin.settingsConfig.mobCoinsStartingBalance.toBigDecimal(MathContext(3)))
        mongoDBManager.usersCollection().insertOne(
            mongoUser
        )
        return mongoUser.toUser(plugin)
    }

    override suspend fun setCoins(user: User, coins: BigDecimal) {
        mongoDBManager.usersCollection().updateOne(eq("_id", user.uuid), Updates.set("coins", coins))
    }

    override suspend fun setCoinsCollected(user: User, coinsCollected: BigDecimal) {
        mongoDBManager.usersCollection().updateOne(eq("_id", user.uuid), Updates.set("coinsCollected", coinsCollected))
    }

    override suspend fun setCoinsSpent(user: User, coinsSpent: BigDecimal) {
        mongoDBManager.usersCollection().updateOne(eq("_id", user.uuid), Updates.set("coinsSpent", coinsSpent))
    }

    override suspend fun getTopMobCoins(): List<User> {
        return mongoDBManager.usersCollection().find().sort(Sorts.descending("coins")).map { it.toUser(plugin) }.toList()
    }

    override suspend fun getGrindTop(): List<User> {
        return mongoDBManager.usersCollection().find().sort(Sorts.descending("coinsCollected")).map { it.toUser(plugin) }.toList()
    }
}