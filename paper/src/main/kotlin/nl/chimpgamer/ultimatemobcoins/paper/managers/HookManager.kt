package nl.chimpgamer.ultimatemobcoins.paper.managers

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.hooks.MythicMobsHook
import nl.chimpgamer.ultimatemobcoins.paper.hooks.PlaceholderAPIHook

class HookManager(private val plugin: UltimateMobCoinsPlugin) {
    private lateinit var placeholderAPIHook: PlaceholderAPIHook
    private val mythicMobsHook = MythicMobsHook(plugin)

    fun load() {
        checkPlaceholderAPI()
        mythicMobsHook.load()
    }

    fun unload() {
        disablePlaceholderAPI()
        mythicMobsHook.unload()
    }

    fun checkPlaceholderAPI () {
        if (plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI'")) {
            placeholderAPIHook = PlaceholderAPIHook(plugin)
            placeholderAPIHook.register()
        }
    }

    fun disablePlaceholderAPI() {
        if (this::placeholderAPIHook.isInitialized) {
            placeholderAPIHook.unregister()
        }
    }
}