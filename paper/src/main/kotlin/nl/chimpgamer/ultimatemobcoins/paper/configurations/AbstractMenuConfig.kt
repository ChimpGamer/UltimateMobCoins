package nl.chimpgamer.ultimatemobcoins.paper.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import java.io.File

abstract class AbstractMenuConfig(plugin: UltimateMobCoinsPlugin, file: File) {
    val config: YamlDocument

    fun reload() = config.reload()

    init {
        val inputStream = plugin.getResource("shops" + File.separator + file.name)
        val generalSettings = GeneralSettings.builder()
            .setDefaultString("")
            .setDefaultObject(null)
            .setKeyFormat(GeneralSettings.KeyFormat.STRING)
            .setUseDefaults(false)
            .build()
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream, generalSettings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT)
        } else {
            YamlDocument.create(file, generalSettings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT)
        }
    }
}