package nl.chimpgamer.ultimatemobcoins.paper.models

import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.configurations.AbstractMenuConfig
import java.io.File

open class ShopMenuBase(plugin: UltimateMobCoinsPlugin, private val file: File) : AbstractMenuConfig(plugin, file) {

    var title: String? = null
        get() = if (field == null) file.nameWithoutExtension else field
        set(value) {
            field = value ?: file.nameWithoutExtension
        }

    var closeOnClick: Boolean = false
    protected var updateInterval: Int
    protected var inventorySize: Int

    lateinit var inventory: RyseInventory

    init {
        title = config.getString("Title", "MobCoin Shop")
        closeOnClick = config.getBoolean("CloseOnClick")

        updateInterval = config.getInt("UpdateInterval", 20)
        if (updateInterval > 0) updateInterval * 50

        inventorySize = config.getInt("MenuSize", 54)
        if (inventorySize < 9) {
            inventorySize = 54
        }
    }
}