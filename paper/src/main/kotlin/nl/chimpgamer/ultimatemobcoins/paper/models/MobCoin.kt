package nl.chimpgamer.ultimatemobcoins.paper.models

import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.math.MathContext
import kotlin.random.Random

class MobCoin(
    val entityType: String,
    val chance: Double,
    var amount: DoubleArray
) {
    private val willDropCoins get() = if (chance == 100.0) true else Random.nextInt(101) < chance

    fun getAmountToDrop(player: Player): BigDecimal {
        val willDropCoins = this.willDropCoins
        println("willDropCoins=$willDropCoins")
        if (!willDropCoins) return BigDecimal.ZERO
        if (amount.isEmpty()) return BigDecimal.ZERO

        val hand = player.inventory.itemInMainHand
        if (amount[1] == 0.0) {
            var amountOfCoins = amount[0]
            if (hand.containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) {
                val level = hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)
                val finalLevel = level * 10
                amountOfCoins += (amountOfCoins * finalLevel) / 100
            }

            return amountOfCoins.toBigDecimal()
        }

        var minimumCoins = amount[0]
        var maximumCoins = amount[1]

        if (hand.containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) {
            val level = hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)
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