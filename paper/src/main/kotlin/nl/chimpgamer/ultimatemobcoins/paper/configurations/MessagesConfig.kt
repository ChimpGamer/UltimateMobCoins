package nl.chimpgamer.ultimatemobcoins.paper.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin

class MessagesConfig(plugin: UltimateMobCoinsPlugin) {
    val config: YamlDocument

    val mobCoinsBalance: String get() = config.getString("mobcoins.balance")
    val mobCoinsBalanceOthers: String get() = config.getString("mobcoins.balance_others")
    val mobCoinsSetSender: String get() = config.getString("mobcoins.set_sender")
    val mobCoinsSetTarget: String get() = config.getString("mobcoins.set_target")
    val mobCoinsGiveSender: String get() = config.getString("mobcoins.give_sender")
    val mobCoinsGiveTarget: String get() = config.getString("mobcoins.give_target")
    val mobCoinsTakeSender: String get() = config.getString("mobcoins.take_sender")
    val mobCoinsTakeTarget: String get() = config.getString("mobcoins.take_target")

    val noPermission: String get() = config.getString("noPermission")

    init {
        val file = plugin.dataFolder.resolve("messages.yml")
        val inputStream = plugin.getResource("messages.yml")
        val loaderSettings = LoaderSettings.builder().setAutoUpdate(true).build()
        val updaterSettings = UpdaterSettings.builder().setVersioning(BasicVersioning("config-version")).build()
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        } else {
            YamlDocument.create(file, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        }
    }
}