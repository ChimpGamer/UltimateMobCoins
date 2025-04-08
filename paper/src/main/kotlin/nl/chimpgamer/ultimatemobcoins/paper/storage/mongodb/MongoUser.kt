package nl.chimpgamer.ultimatemobcoins.paper.storage.mongodb

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.User
import org.bson.codecs.pojo.annotations.BsonId
import java.math.BigDecimal
import java.util.*

data class MongoUser(
    @BsonId val uuid: UUID,
    var username: String,
    var coins: BigDecimal,
    var coinsCollected: BigDecimal = BigDecimal.ZERO,
    var coinsSpent: BigDecimal = BigDecimal.ZERO
)

fun MongoUser.toUser(plugin: UltimateMobCoinsPlugin) = User(plugin, uuid, username, coins, coinsCollected, coinsSpent)
