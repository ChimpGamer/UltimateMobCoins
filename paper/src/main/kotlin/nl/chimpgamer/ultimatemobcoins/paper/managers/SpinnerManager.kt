package nl.chimpgamer.ultimatemobcoins.paper.managers

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.getString
import nl.chimpgamer.ultimatemobcoins.paper.extensions.pdc
import nl.chimpgamer.ultimatemobcoins.paper.models.SpinnerPrize
import nl.chimpgamer.ultimatemobcoins.paper.models.ConfigurableSound
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.SpinnerMenu
import nl.chimpgamer.ultimatemobcoins.paper.utils.ItemUtils
import nl.chimpgamer.ultimatemobcoins.paper.utils.NamespacedKeys
import org.bukkit.inventory.ItemStack

class SpinnerManager(private val plugin: UltimateMobCoinsPlugin) {
    private companion object {
        const val MAX_RANDOM_PRIZE_ATTEMPTS = 2000
    }

    var spinningSound: ConfigurableSound? = null
    var prizeWonSound: ConfigurableSound? = null

    val prizes = HashSet<SpinnerPrize>()

    val spinnerMenu by lazy { SpinnerMenu(plugin) }

    val randomPrize: SpinnerPrize?
        get() = buildSet {
            var attempts = 0
            while (isEmpty() && attempts <= MAX_RANDOM_PRIZE_ATTEMPTS) {
                attempts++
                addAll(prizes.filter(SpinnerPrize::success))
            }
        }.randomOrNull()

    private fun loadSounds() {
        plugin.spinnerConfig.getSoundsSection()?.let { soundsSection ->
            spinningSound = soundsSection.getSection("spinning")
                ?.getStringRouteMappedValues(false)
                ?.let(ConfigurableSound::deserialize)

            prizeWonSound = soundsSection.getSection("prize_won")
                ?.getStringRouteMappedValues(false)
                ?.let(ConfigurableSound::deserialize)
        }
    }

    private fun loadPrizes() {
        prizes.clear()
        val prizesSection = plugin.spinnerConfig.getPrizesSection() ?: return

        prizesSection.keys.forEach { key ->
            val prize = SpinnerPrize(
                name = key.toString(),
                itemStack = ItemUtils.itemDataToItemStack(plugin, prizesSection.getStringList("$key.item")),
                chance = prizesSection.getDouble("$key.chance"),
                commands = prizesSection.getStringList("$key.commands"),
                message = prizesSection.getString("$key.message", "")
            )
            prizes.add(prize)
        }
        plugin.logger.info("Loaded ${prizes.size} prizes for the mobcoin spinner!")
    }

    fun getPrize(itemStack: ItemStack?): SpinnerPrize? {
        if (itemStack == null || !itemStack.hasItemMeta()) return null
        itemStack.itemMeta.pdc {
            if (has(NamespacedKeys.spinnerPrizeName)) {
                val name = getString(NamespacedKeys.spinnerPrizeName)
                return prizes.find { it.name == name }
            }
        }
        return null
    }

    fun reload() {
        plugin.spinnerConfig.reload()
        loadSounds()
        loadPrizes()
    }

    init {
        loadSounds()
        loadPrizes()
    }
}