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
    val mobCoinsNotEnough: String get() = config.getString("mobcoins.not_enough")
    val mobCoinsPaySender: String get() = config.getString("mobcoins.pay_sender")
    val mobCoinsPayTarget: String get() = config.getString("mobcoins.pay_target")
    val mobCoinsCannotPayYourself: String get() = config.getString("mobcoins.cannot_pay_yourself")
    val mobCoinsReceivedChat: String get() = config.getString("mobcoins.received.chat")
    val mobCoinsReceivedActionBar: String get() = config.getString("mobcoins.received.actionbar")
    val mobCoinsWithdraw: String get() = config.getString("mobcoins.withdraw")
    val mobCoinsInventoryFull: String get() = config.getString("mobcoins.inventory_full")

    val menusNoPermission: String get() = config.getString("menus.no_permission")
    val menusOutOfStock: String get() = config.getString("menus.out_of_stock")
    val menusNotEnoughMobCoins: String get() = config.getString("menus.not_enough_mobcoins")
    val menusItemBought: String get() = config.getString("menus.item_bought")

    val spinnerNotEnoughMobCoins: String get() = config.getString("spinner.not_enough_mobcoins")

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