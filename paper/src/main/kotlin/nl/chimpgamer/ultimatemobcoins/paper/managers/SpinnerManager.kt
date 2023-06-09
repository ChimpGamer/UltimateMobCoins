package nl.chimpgamer.ultimatemobcoins.paper.managers

import de.tr7zw.nbtapi.NBTItem
import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
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
            val randomPrizes: MutableSet<SpinnerPrize> = HashSet()
            var stop = 0
            while (randomPrizes.size == 0 && stop <= 2000) {
                stop++
                randomPrizes.addAll(prizes.filter(SpinnerPrize::success))
            }

            return randomPrizes.randomOrNull()
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
        if (nbtItem.hasNBTData() && nbtItem.hasTag("ultimatemobcoins.spinner.prize.name")) {
            val name = nbtItem.getString("ultimatemobcoins.spinner.prize.name")
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
        val generalSettings = GeneralSettings
            .builder()
            .setUseDefaults(false)
            .build()
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream, generalSettings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT)
        } else {
            YamlDocument.create(file, generalSettings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT)
        }

        loadSounds()
        loadPrizes()
    }
}