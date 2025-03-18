package nl.chimpgamer.ultimatemobcoins.paper.hooks

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin

abstract class PluginHook(protected val plugin: UltimateMobCoinsPlugin, val pluginName: String) {
    open var isLoaded: Boolean = false
    abstract fun load()
    open fun unload() {}

    fun shouldHook(): Boolean {
        return plugin.hooksConfig.isHookEnabled(pluginName)
    }

    fun canHook(): Boolean {
        return shouldHook() && plugin.server.pluginManager.isPluginEnabled(pluginName)
    }

    fun isPluginLoaded(): Boolean {
        return plugin.server.pluginManager.getPlugin(pluginName) != null
    }
}