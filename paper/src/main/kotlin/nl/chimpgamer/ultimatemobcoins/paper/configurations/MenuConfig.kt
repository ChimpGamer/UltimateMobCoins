package nl.chimpgamer.ultimatemobcoins.paper.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.block.implementation.Section
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.menu.MenuType
import java.io.File
import kotlin.reflect.KClass

class MenuConfig(plugin: UltimateMobCoinsPlugin, val file: File) {
    val config: YamlDocument

    val menuType: MenuType get() = config.getEnum("type", MenuType::class.java, MenuType.NORMAL)

    fun hasResetTimer(): Boolean {
        val refreshTime = config.getLong("refresh_time")
        return !(refreshTime == null || refreshTime <= 0)
    }

    fun reload() = config.reload()

    fun getSection(section: String): Section? = config.getSection(section)

    fun getString(route: String): String? = config.getString(route)

    fun getString(route: String, def: String?): String? = config.getString(route, def)

    fun getBoolean(route: String): Boolean = config.getBoolean(route)

    fun getInt(route: String, def: Int): Int = config.getInt(route, def)

    fun getLong(route: String): Long? = config.getLong(route)

    fun getIntList(route: String): List<Int> = config.getIntList(route)

    fun <T : Enum<T>> getEnum(route: String, clazz: KClass<T>, def: T): T = config.getEnum(route, clazz.java, def)

    init {
        val inputStream = plugin.getResource("shops/${file.name}")
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