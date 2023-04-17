package nl.chimpgamer.ultimatemobcoins.paper.managers

import dev.dejvokep.boostedyaml.YamlDocument
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.MobCoin
import org.bukkit.entity.EntityType

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