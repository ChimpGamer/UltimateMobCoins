package nl.chimpgamer.ultimatemobcoins.paper.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin

class SpinnerConfig(plugin: UltimateMobCoinsPlugin) {
    private val config: YamlDocument

    val shootFireworks: Boolean get() = config.getBoolean("shoot_fireworks")
    val usageCosts: Double get() = config.getDouble("usage_costs")
    val menuTitle: String get() = config.getString("menu_title")

    init {
        val fileName = "spinner.yml"
        val file = plugin.dataFolder.resolve(fileName)
        val inputStream = plugin.getResource(fileName)
        val generalSettings = GeneralSettings.builder()
            .setUseDefaults(false)
            .build()

        config = if (inputStream != null) {
            YamlDocument.create(
                file,
                inputStream,
                generalSettings,
                LoaderSettings.DEFAULT,
                DumperSettings.DEFAULT,
                UpdaterSettings.DEFAULT
            )
        } else {
            YamlDocument.create(
                file,
                generalSettings,
                LoaderSettings.DEFAULT,
                DumperSettings.DEFAULT,
                UpdaterSettings.DEFAULT
            )
        }
    }

    fun getSoundsSection() = config.getSection("sounds")
    fun getPrizesSection() = config.getSection("prizes")
    fun reload() = config.reload()
}
