package nl.chimpgamer.ultimatemobcoins.paper.configurations

import com.googlecode.aviator.AviatorEvaluator
import com.googlecode.aviator.Expression
import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.ConfigurableAnimation
import nl.chimpgamer.ultimatemobcoins.paper.models.ConfigurableSound
import nl.chimpgamer.ultimatemobcoins.paper.utils.ItemUtils
import nl.chimpgamer.ultimatemobcoins.paper.utils.NumberFormatter

class SettingsConfig(private val plugin: UltimateMobCoinsPlugin) {
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

    val storageMongoDBCollectionPrefix: String get() = config.getString("storage.mongodb-collection-prefix")
    val storageMongoDBConnectionUri: String get() = config.getString("storage.mongodb-connection-uri")

    val mobCoinsDisabledWorlds: List<String> get() = config.getStringList("mobcoins.disabled_worlds")
    val mobCoinsStartingBalance: Double get() = config.getDouble("mobcoins.starting_balance")
    val mobCoinsDropsAutoRedeemOnPickup: Boolean get() = config.getBoolean("mobcoins.drops.auto-redeem-on-pickup", true)
    val mobCoinsDropsAutoPickup: Boolean get() = config.getBoolean("mobcoins.drops.auto-pickup", false)
    val mobCoinsDropsAllowHopperPickup: Boolean get() = config.getBoolean("mobcoins.drops.allow-hopper-pickup", false)
    val mobCoinsFormat: String get() = config.getString("mobcoins.format")
    val mobCoinsFormatLocale: String get() = config.getString("mobcoins.format-locale")
    fun getMobCoinsItem(tagResolver: TagResolver) = ItemUtils.itemSectionToItemStack(plugin, config.getSection("mobcoins.item"), tagResolver)
    val mobCoinsItemSelfRedeemable: Boolean get() = config.getBoolean("mobcoins.item.self-redeemable", true)
    val mobCoinsSoundsDrop: ConfigurableSound get() = ConfigurableSound.deserialize(config.getSection("mobcoins.sounds.drop").getStringRouteMappedValues(false))
    val mobCoinsSoundsPickup: ConfigurableSound get() = ConfigurableSound.deserialize(config.getSection("mobcoins.sounds.pickup").getStringRouteMappedValues(false))
    val mobCoinsLootingEnchantMultiplierEnabled: Boolean get() = config.getBoolean("mobcoins.looting-enchant-multiplier.enabled", true)
    val mobCoinsLootingEnchantMultiplierFormula: String get() = config.getString("mobcoins.looting-enchant-multiplier.formula", "(mobcoins * (looting-level * 10) / 100)")
    val mobCoinsLossOnDeathType: String get() = config.getString("mobcoins.loss-on-death.type")
    val mobCoinsLossOnDeathValue: Double get() = config.getDouble("mobcoins.loss-on-death.value")
    val mobCoinsLeaderboardEnabled: Boolean get() = config.getBoolean("mobcoins.leaderboard.enabled", false)
    val mobCoinsLeaderboardShowZero: Boolean get() = config.getBoolean("mobcoins.leaderboard.show-zero", false)
    val mobCoinsAnimationsDrop: ConfigurableAnimation get() = ConfigurableAnimation.deserialize(config.getSection("mobcoins.animations.drop").getStringRouteMappedValues(false))
    val mobCoinsEarningsPenaltyEnabled: Boolean get() = config.getBoolean("mobcoins.earnings-penalty.enabled", false)
    val mobCoinsEarningsPenaltyThreshold: Double get() = config.getDouble("mobcoins.earnings-penalty.threshold", 0.0)
    val mobCoinsEarningsPenaltyCooldownValue: Long get() = config.getLong("mobcoins.earnings-penalty.cooldown.value", 1)
    val mobCoinsEarningsPenaltyCooldownUnit: String get() = config.getString("mobcoins.earnings-penalty.cooldown.unit", "minutes")
    val mobCoinsEarningsPenaltyType: String get() = config.getString("mobcoins.earnings-penalty.type", "percentage")
    val mobCoinsEarningsPenaltyValue: Double get() = config.getDouble("mobcoins.earnings-penalty.value", 0.0)

    val logPay: Boolean get() = config.getBoolean("log.pay")
    val logWithdraw: Boolean get() = config.getBoolean("log.withdraw")
    val logSpinner: Boolean get() = config.getBoolean("log.spinner")

    val commandName: String get() = config.getString("command.name")
    val commandAliases: List<String> get() = config.getStringList("command.aliases")
    val commandDefaultShop: String get() = config.getString("command.default_shop")

    val commandWithdrawAmountDoubleType: Boolean get() = config.getBoolean("command.withdraw.amount.double-type", true)
    val commandWithdrawAmountMax: Double get() = config.getDouble("command.withdraw.amount.max", Double.MAX_VALUE)

    val updateNotifyOnJoin: Boolean get() = config.getBoolean("update.notify-on-join", true)

    val debug: Boolean get() = config.getBoolean("debug", false)

    var mobCoinsLootingEnchantMultiplierFormulaCompiled: Expression? = null

    fun reload() {
        config.reload()
        NumberFormatter.setPrettyFormat(mobCoinsFormat, mobCoinsFormatLocale)
        mobCoinsLootingEnchantMultiplierFormulaCompiled = AviatorEvaluator.compile(mobCoinsLootingEnchantMultiplierFormula)
    }

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

        NumberFormatter.setPrettyFormat(mobCoinsFormat, mobCoinsFormatLocale)
        mobCoinsLootingEnchantMultiplierFormulaCompiled = AviatorEvaluator.compile(mobCoinsLootingEnchantMultiplierFormula)

        if (config.contains("mobcoins.auto-pickup")) {
            val mobCoinsAutoPickup = config.getBoolean("mobcoins.auto-pickup")
            config.set("mobcoins.drops.auto-pickup", mobCoinsAutoPickup)
            config.set("mobcoins.auto-pickup", null)
        }
        if (config.contains("mobcoins.allow-hopper-pickup")) {
            val mobCoinsAllowHopperPickup = config.getBoolean("mobcoins.allow-hopper-pickup")
            config.set("mobcoins.drops.allow-hopper-pickup", mobCoinsAllowHopperPickup)
            config.set("mobcoins.allow-hopper-pickup", null)
        }
    }
}