package nl.chimpgamer.ultimatemobcoins.paper.managers

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.hooks.EcoBossesHook
import nl.chimpgamer.ultimatemobcoins.paper.hooks.MythicMobsHook
import nl.chimpgamer.ultimatemobcoins.paper.hooks.PlaceholderAPIHook

class HookManager(private val plugin: UltimateMobCoinsPlugin) {
    private lateinit var placeholderAPIHook: PlaceholderAPIHook
    private val mythicMobsHook = MythicMobsHook(plugin)
    private val ecoBossesHook = EcoBossesHook(plugin)

    fun load() {
        checkPlaceholderAPI()
        mythicMobsHook.load()
        ecoBossesHook.load()
    }

    fun unload() {
        disablePlaceholderAPI()
        mythicMobsHook.unload()
        ecoBossesHook.unload()
    }

    private fun checkPlaceholderAPI () {
        if (plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI'")) {
            placeholderAPIHook = PlaceholderAPIHook(plugin)
            placeholderAPIHook.register()
        }
    }

    private fun disablePlaceholderAPI() {
        if (this::placeholderAPIHook.isInitialized) {
            placeholderAPIHook.unregister()
        }
    }
}