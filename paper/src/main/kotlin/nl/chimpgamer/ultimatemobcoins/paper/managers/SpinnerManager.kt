package nl.chimpgamer.ultimatemobcoins.paper.managers

import de.tr7zw.nbtapi.NBTItem
import dev.dejvokep.boostedyaml.YamlDocument
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.SpinnerPrize
import nl.chimpgamer.ultimatemobcoins.paper.models.ConfigurableSound
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.SpinnerMenu
import nl.chimpgamer.ultimatemobcoins.paper.utils.ItemUtils
import org.bukkit.inventory.ItemStack

class SpinnerManager(private val plugin: UltimateMobCoinsPlugin) {
    private val config: YamlDocument

    val shootFireworks: Boolean get() = config.getBoolean("shoot_fireworks")
    val usageCosts: Double get() = config.getDouble("usage_costs")
    val menuTitle: String get() = config.getString("menu_title")

    var spinningSound: ConfigurableSound? = null
    var prizeWonSound: ConfigurableSound? = null

    val prizes = HashSet<SpinnerPrize>()

    val spinnerMenu by lazy { SpinnerMenu(plugin) }

    val randomPrize: SpinnerPrize?
        get() {
            return buildSet {
                var stop = 0
                while (size == 0 && stop <= 2000) {
                    stop++
                    addAll(prizes.filter(SpinnerPrize::success))
                }
            }.randomOrNull()
        }

    private fun loadSounds() {
        val soundsSection = config.getSection("sounds")
        if (soundsSection != null) {
            if (soundsSection.contains("spinning")) {
                spinningSound = ConfigurableSound.deserialize(soundsSection.getSection("spinning").getStringRouteMappedValues(false))
            }
            if (soundsSection.contains("prize_won")) {
                prizeWonSound = ConfigurableSound.deserialize(soundsSection.getSection("prize_won").getStringRouteMappedValues(false))
            }
        }
    }

    private fun loadPrizes() {
        prizes.clear()
        val prizesSection = config.getSection("prizes") ?: return

        for (key in prizesSection.keys) {
            val itemData = prizesSection.getStringList("$key.item")
            val chance = prizesSection.getDouble("$key.chance")
            val commands = prizesSection.getStringList("$key.commands")
            val message = prizesSection.getString("$key.message", "")

            val itemStack = ItemUtils.itemDataToItemStack(plugin, itemData)
            val spinnerPrize = SpinnerPrize(key.toString(), itemStack, chance, commands, message)
            prizes.add(spinnerPrize)
        }

        plugin.logger.info("Loaded ${prizes.size} prizes for the mobcoin spinner!")
    }

    fun getPrize(itemStack: ItemStack?): SpinnerPrize? {
        if (itemStack == null) return null
        val nbtItem = NBTItem(itemStack)
        val tagKey = "ultimatemobcoins.spinner.prize.name"
        if (nbtItem.hasNBTData() && nbtItem.hasTag(tagKey)) {
            val name = nbtItem.getString(tagKey)
            return prizes.firstOrNull { it.name == name }
        }
        return null
    }

    fun reload() {
        config.reload()
        loadSounds()
        loadPrizes()
    }

    init {
        val file = plugin.dataFolder.resolve("spinner.yml")
        val inputStream = plugin.getResource("spinner.yml")
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream)
        } else {
            YamlDocument.create(file)
        }

        loadSounds()
        loadPrizes()
    }
}