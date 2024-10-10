package nl.chimpgamer.ultimatemobcoins.paper.managers

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.hooks.*
import nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.BetonQuestHook
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
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
    val roseStackerHook = RoseStackerHook(plugin)

    fun load() {
        checkPlaceholderAPI()
        mythicMobsHook.load()
        ecoMobsHook.load()
        vaultHook.initialize()
        betonQuestHook.load()
        miniPlaceholdersHook.load()
        roseStackerHook.load()
    }

    fun unload() {
        disablePlaceholderAPI()
        mythicMobsHook.unload()
        ecoMobsHook.unload()
        miniPlaceholdersHook.unload()
        roseStackerHook.unload()
    }

    private fun checkPlaceholderAPI() {
        if (plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            placeholderAPIHook = PlaceholderAPIHook(plugin)
            placeholderAPIHook.register()
        }
    }

    fun loadWorldGuard() {
        if (plugin.server.pluginManager.getPlugin("WorldGuard") != null && plugin.hooksConfig.isHookEnabled("WorldGuard")) {
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

    fun getMobCoinDropsMultiplier(player: Player): Double {
        return worldGuardHook?.getMobCoinDropsMultiplier(player) ?: 0.0
    }

    fun getMobCoinDropChanceMultiplier(player: Player): Double {
        return worldGuardHook?.getMobCoinDropChanceMultiplier(player) ?: 0.0
    }

    fun isMobCoinDropsAllowed(player: Player, location: Location): Boolean {
        return worldGuardHook?.isMobCoinDropsAllowed(player, location) != false
    }

    fun getEntityName(entity: LivingEntity): String {
        var entityTypeName = entity.type.name.lowercase()
        // If entity is a mythic mob don't drop mob coins through this event.
        if (mythicMobsHook.isMythicMob(entity)) {
            val mythicMobId = mythicMobsHook.getMythicMobId(entity)
            if (mythicMobId != null) {
                entityTypeName = mythicMobId
            }
        }

        // If entity is a EcoMobs mob then we have to alter the entityTypeName.
        if (ecoMobsHook.isEcoMob(entity)) {
            val ecoMobId = ecoMobsHook.getEcoMobId(entity)
            if (ecoMobId != null) {
                entityTypeName = ecoMobId
            }
        }

        return entityTypeName
    }
}