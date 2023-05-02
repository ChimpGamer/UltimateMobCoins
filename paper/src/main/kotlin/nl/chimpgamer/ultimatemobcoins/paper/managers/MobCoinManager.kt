package nl.chimpgamer.ultimatemobcoins.paper.managers

import de.tr7zw.nbtapi.NBTItem
import dev.dejvokep.boostedyaml.YamlDocument
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.MobCoin
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal

class MobCoinManager(private val plugin: UltimateMobCoinsPlugin) {
    val config: YamlDocument
    val mobCoinsList = ArrayList<MobCoin>()

    fun loadMobCoins() {
        mobCoinsList.clear()
        val mobCoinDrops = config.getSection("mobCoinDrops") ?: return
        mobCoinDrops.keys.mapNotNull { it.toString() }.forEach { key ->
            val entityType = runCatching { EntityType.valueOf(key.uppercase()) }.getOrNull() ?: return@forEach
            val chance = mobCoinDrops.getDouble("$key.chance")
            val amount = DoubleArray(2)
            val amountStr = mobCoinDrops.getString("$key.amount")

            if (amountStr.contains("-")) {
                for ((i, dat) in amountStr.split("-").withIndex()) {
                    amount[i] = dat.toDouble()
                }
            } else {
                amount[0] = amountStr.toDouble()
                amount[1] = 0.0
            }

            mobCoinsList.add(MobCoin(entityType, chance, amount))
        }
        plugin.logger.info("Loaded ${mobCoinsList.size} mobcoin drops")
    }

    fun getMobCoin(entityType: EntityType) = mobCoinsList.firstOrNull { it.entityType === entityType }

    fun getCoin(killer: Player, entity: Entity): ItemStack? {
        if (!killer.hasPermission("ultimatemobcoins.dropcoin")) return null

        val dropAmount = plugin.mobCoinsManager.getMobCoin(entity.type)?.getAmountToDrop(killer) ?: return null
        if (dropAmount == BigDecimal.ZERO) return null
        val amount = plugin.applyMultiplier(killer, dropAmount)

        val mobCoinItem = plugin.settingsConfig.getMobCoinsItem(Placeholder.unparsed("amount", dropAmount.toString())) // EpicHoppers ignores the item if the name starts with *** (https://github.com/songoda/EpicHoppers/blob/master/src/main/java/com/songoda/epichoppers/hopper/levels/modules/ModuleSuction.java#L91)

        val nbtMobCoin = NBTItem(mobCoinItem)
        nbtMobCoin.setBoolean("isMobCoin", true)
        nbtMobCoin.setDouble("amount", amount.toDouble())

        return nbtMobCoin.item
    }

    fun reload() {
        config.reload()
        loadMobCoins()
    }

    init {
        val file = plugin.dataFolder.resolve("mobcoins.yml")
        val inputStream = plugin.getResource("mobcoins.yml")
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream)
        } else {
            YamlDocument.create(file)
        }
    }
}