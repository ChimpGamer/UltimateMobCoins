package nl.chimpgamer.ultimatemobcoins.paper.models

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.math.MathContext
import kotlin.random.Random

class MobCoin(
    private val plugin: UltimateMobCoinsPlugin,
    val entityType: String,
    var chance: Double,
    var amount: DoubleArray
) {
    fun applyDropChanceMultiplier(player: Player) {
        this.chance = plugin.applyDropChanceMultiplier(player, chance)
    }

    private fun willDropCoins(): Boolean {
        if (chance >= 100.0) return true
        return Random.nextDouble(101.0) < chance
    }

    fun getAmountToDrop(player: Player): BigDecimal {
        if (amount.isEmpty()) return BigDecimal.ZERO
        if (!willDropCoins()) return BigDecimal.ZERO
        val lootingEnchantment = plugin.lootingEnchantment

        val hand = player.inventory.itemInMainHand
        if (amount[1] == 0.0) {
            var amountOfCoins = amount[0]
            if (hand.containsEnchantment(lootingEnchantment)) {
                val level = hand.getEnchantmentLevel(lootingEnchantment)
                val finalLevel = level * 10
                amountOfCoins += (amountOfCoins * finalLevel) / 100
            }

            return amountOfCoins.toBigDecimal()
        }

        var minimumCoins = amount[0]
        var maximumCoins = amount[1]

        if (hand.containsEnchantment(lootingEnchantment)) {
            val level = hand.getEnchantmentLevel(lootingEnchantment)
            val finalLevel = level * 10
            minimumCoins += (minimumCoins * finalLevel) / 100
            maximumCoins += (maximumCoins * finalLevel) / 100
        }

        return BigDecimal(Random.nextDouble(maximumCoins - minimumCoins) + minimumCoins, MathContext(3))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MobCoin) return false
        return entityType === other.entityType && chance == other.chance && amount.contentEquals(other.amount)
    }

    override fun hashCode(): Int {
        var result = entityType.hashCode()
        result = 31 * result + chance.hashCode()
        result = 31 * result + amount.contentHashCode()
        return result
    }
}