package nl.chimpgamer.ultimatemobcoins.paper.storage.sql

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.User
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class UserEntity(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<UserEntity>(UsersTable)

    var username by UsersTable.username
    var coins by UsersTable.coins
    var coinsCollected by UsersTable.coinsCollected
    var coinsSpent by UsersTable.coinsSpent
}

fun UserEntity.toUser(plugin: UltimateMobCoinsPlugin) = User(plugin, id.value, username, coins, coinsCollected, coinsSpent)