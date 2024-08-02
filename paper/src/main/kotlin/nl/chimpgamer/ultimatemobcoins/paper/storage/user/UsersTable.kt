package nl.chimpgamer.ultimatemobcoins.paper.storage.user

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import java.math.BigDecimal

object UsersTable : UUIDTable("users", "uuid") {
    val username: Column<String> = varchar("username", 16)
    val coins: Column<BigDecimal> = decimal("coins", 12, 3)
    val coinsCollected: Column<BigDecimal> = decimal("coins_collected", 12, 3)
    val coinsSpent: Column<BigDecimal> = decimal("coins_spent", 12, 3)
}