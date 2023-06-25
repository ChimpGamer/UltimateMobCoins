package nl.chimpgamer.ultimatemobcoins.paper.managers

import de.tr7zw.nbtapi.NBTItem
import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.MobCoin
import org.bukkit.entity.Entity
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
            val entityType = key.uppercase()
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

    fun getMobCoin(entityType: String) = mobCoinsList.firstOrNull { it.entityType.equals(entityType, ignoreCase = true) }

    fun getCoin(killer: Player, entity: Entity): ItemStack? {
        return getCoin(killer, entity.type.name)
    }

    fun getCoin(killer: Player, entity: String): ItemStack? {
        if (!killer.hasPermission("ultimatemobcoins.dropcoin")) return null

        val dropAmount = plugin.mobCoinsManager.getMobCoin(entity)?.getAmountToDrop(killer) ?: return null
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
        val generalSettings = GeneralSettings
            .builder()
            .setUseDefaults(false)
            .build()
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream, generalSettings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT)
        } else {
            YamlDocument.create(file, generalSettings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT)
        }
    }
}