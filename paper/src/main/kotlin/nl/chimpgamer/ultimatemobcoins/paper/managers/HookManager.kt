package nl.chimpgamer.ultimatemobcoins.paper.managers

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.hooks.*
import nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.BetonQuestHook
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent

class HookManager(private val plugin: UltimateMobCoinsPlugin) : Listener {
    private lateinit var placeholderAPIHook: PlaceholderAPIHook
    val mythicMobsHook = MythicMobsHook(plugin)
    val ecoMobsHook = EcoMobsHook(plugin)
    val vaultHook = VaultHook(plugin)
    private val betonQuestHook = BetonQuestHook(plugin)
    private var worldGuardHook: WorldGuardHook? = null
    private val miniPlaceholdersHook = MiniPlaceholdersHook(plugin)

    fun load() {
        checkPlaceholderAPI()
        mythicMobsHook.load()
        ecoMobsHook.load()
        vaultHook.initialize()
        betonQuestHook.load()
        miniPlaceholdersHook.load()
    }

    fun unload() {
        disablePlaceholderAPI()
        mythicMobsHook.unload()
        ecoMobsHook.unload()
        miniPlaceholdersHook.unload()
    }

    private fun checkPlaceholderAPI() {
        if (plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            placeholderAPIHook = PlaceholderAPIHook(plugin)
            placeholderAPIHook.register()
        }
    }

    fun loadWorldGuard() {
        if (plugin.server.pluginManager.getPlugin("WorldGuard") != null) {
            worldGuardHook = WorldGuardHook(plugin)
            worldGuardHook?.load()
        }
    }

    private fun disablePlaceholderAPI() {
        if (this::placeholderAPIHook.isInitialized) {
            placeholderAPIHook.unregister()
        }
    }

    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) {
        when (event.plugin.name) {
            "BetonQuest" -> betonQuestHook.load()
            "EcoMobs" -> ecoMobsHook.load()
        }
    }

    /*@EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {

    }*/

    fun getMobCoinMultiplier(player: Player): Double {
        return worldGuardHook?.getMobCoinDropsMultiplier(player) ?: 0.0
    }

    fun isMobCoinDropsAllowed(player: Player, location: Location): Boolean {
        return worldGuardHook?.isMobCoinDropsAllowed(player, location) != false
    }
}