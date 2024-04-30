package nl.chimpgamer.ultimatemobcoins.paper.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.ConfigurableSound
import nl.chimpgamer.ultimatemobcoins.paper.utils.ItemUtils

class SettingsConfig(plugin: UltimateMobCoinsPlugin) {
    val config: YamlDocument

    val storageType: String get() = config.getString("storage.type", "sqlite")
    val storageHost: String get() = config.getString("storage.host", "localhost")
    val storagePort: Int get() = config.getInt("storage.port", 3306)
    val storageDatabase: String get() = config.getString("storage.database", "ultimatemobcoins")
    val storageUsername: String get() = config.getString("storage.username", "ultimatemobcoins")
    val storagePassword: String get() = config.getString("storage.password", "ultimatemobcoins")
    val storagePoolSettingsMaximumPoolSize: Int get() = config.getInt("storage.pool-settings.maximum-pool-size", 5)
    val storagePoolSettingsMinimumIdle: Int get() = config.getInt("storage.pool-settings.minimum-idle", 2)
    val storagePoolSettingsMaximumLifetime: Long get() = config.getLong("storage.pool-settings.maximum-lifetime", 1800000L)
    val storagePoolSettingsConnectionTimeout: Long get() = config.getLong("storage.pool-settings.connection-timeout", 5000L)
    val storageProperties: Map<String, String> get() = config.getSection("storage.properties").getStringRouteMappedValues(false).mapValues { it.value.toString() }

    val mobCoinsDisabledWorlds: List<String> get() = config.getStringList("mobcoins.disabled_worlds")
    val mobCoinsStartingBalance: Double get() = config.getDouble("mobcoins.starting_balance")
    val mobCoinsAutoPickup: Boolean get() = config.getBoolean("mobcoins.auto_pickup", false)
    fun getMobCoinsItem(tagResolver: TagResolver) = ItemUtils.itemSectionToItemStack(config.getSection("mobcoins.item"), tagResolver)
    val mobCoinsSoundsPickup: ConfigurableSound get() = ConfigurableSound.deserialize(config.getSection("mobcoins.sounds.pickup").getStringRouteMappedValues(false))

    val logPay: Boolean get() = config.getBoolean("log.pay")
    val logWithdraw: Boolean get() = config.getBoolean("log.withdraw")
    val logSpinner: Boolean get() = config.getBoolean("log.spinner")

    val commandName: String get() = config.getString("command.name")
    val commandAliases: List<String> get() = config.getStringList("command.aliases")
    val commandDefaultShop: String get() = config.getString("command.default_shop")

    init {
        val file = plugin.dataFolder.resolve("settings.yml")
        val inputStream = plugin.getResource("settings.yml")
        val loaderSettings = LoaderSettings.builder().setAutoUpdate(true).build()
        val updaterSettings = UpdaterSettings.builder().setVersioning(BasicVersioning("config-version")).build()
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        } else {
            YamlDocument.create(file, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        }
    }
}