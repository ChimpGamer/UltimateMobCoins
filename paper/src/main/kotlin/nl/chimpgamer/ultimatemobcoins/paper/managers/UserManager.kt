package nl.chimpgamer.ultimatemobcoins.paper.managers

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.User
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.util.UUID

class UserManager(private val plugin: UltimateMobCoinsPlugin) {

    fun onLogin(playerUUID: UUID, username: String) {
        val user = getByUUID(playerUUID)
        if (user == null) {
            transaction {
                User.new(playerUUID) {
                    this.username = username
                    this.coins = BigDecimal.ZERO
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

    fun getByUUID(playerUUID: UUID): User? {
        return transaction { User.findById(playerUUID) }
    }
}