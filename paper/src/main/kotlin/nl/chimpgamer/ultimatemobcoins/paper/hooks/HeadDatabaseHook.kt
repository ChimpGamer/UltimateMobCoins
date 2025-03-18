package nl.chimpgamer.ultimatemobcoins.paper.hooks

import me.arcaniax.hdb.api.HeadDatabaseAPI
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.inventory.ItemStack

class HeadDatabaseHook(ultimateWarpsPlugin: UltimateMobCoinsPlugin) : PluginHook(ultimateWarpsPlugin, "HeadDatabase") {
    private lateinit var api: HeadDatabaseAPI

    fun getHead(id: String): ItemStack? = api.getItemHead(id)

    fun getRandomHead(): ItemStack = api.randomHead

    override fun load() {
        if (isLoaded) return
        if (shouldHook()) {
            api = HeadDatabaseAPI()
            isLoaded = true
            plugin.logger.info("Successfully loaded $pluginName hook!")
        }
    }

    override fun unload() {
        isLoaded = false
    }
}