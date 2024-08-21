package nl.chimpgamer.ultimatemobcoins.paper.hooks

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.DoubleFlag
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry
import com.sk89q.worldguard.protection.regions.RegionContainer
import com.sk89q.worldguard.session.SessionManager
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.Location
import org.bukkit.entity.Player

class WorldGuardHook(private val plugin: UltimateMobCoinsPlugin) {

    private var loaded = false

    private var allowMobCoinsDropsFlag = StateFlag("allow-mobcoin-drops", true)
    private var multiplyMobCoinDropsFlag = DoubleFlag("multiply-mobcoin-drops")
    private var multiplyMobCoinDropChanceFlag = DoubleFlag("multiply-mobcoin-drop-chance")

    private val sessionManager: SessionManager get() = WorldGuard.getInstance().platform.sessionManager
    private val regionContainer: RegionContainer get() = WorldGuard.getInstance().platform.regionContainer

    fun load() {
        val flagRegistry = WorldGuard.getInstance().flagRegistry
        registerAllowMobCoinsDropsFlag(flagRegistry)
        registerMultiplyMobCoinsDropsFlag(flagRegistry)
        registerMultiplyMobCoinsDropChanceFlag(flagRegistry)
        loaded = true
        plugin.logger.info("Successfully loaded WorldGuard hook!")
    }

    fun isMobCoinDropsAllowed(player: Player, location: Location): Boolean {
        if (!loaded) return true
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
        val regionQuery = regionContainer.createQuery()
        return sessionManager.hasBypass(localPlayer, localPlayer.world) || regionQuery.testState(BukkitAdapter.adapt(location), localPlayer, allowMobCoinsDropsFlag)
    }

    fun getMobCoinDropsMultiplier(player: Player): Double {
        if (!loaded) return 0.0
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
        val regionQuery = regionContainer.createQuery()
        return regionQuery.queryValue(BukkitAdapter.adapt(player.location), localPlayer, multiplyMobCoinDropsFlag) ?: 0.0
    }

    fun getMobCoinDropChanceMultiplier(player: Player): Double {
        if (!loaded) return 0.0
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
        val regionQuery = regionContainer.createQuery()
        return regionQuery.queryValue(BukkitAdapter.adapt(player.location), localPlayer, multiplyMobCoinDropChanceFlag) ?: 0.0
    }

    private fun registerAllowMobCoinsDropsFlag(flagRegistry: FlagRegistry) {
        try {
            flagRegistry.register(allowMobCoinsDropsFlag)
        } catch (ex: FlagConflictException) {
            val existingFlag = flagRegistry.get("allow-mobcoin-drops")
            if (existingFlag is StateFlag) {
                allowMobCoinsDropsFlag = existingFlag
            }
        }
    }

    private fun registerMultiplyMobCoinsDropsFlag(flagRegistry: FlagRegistry) {
        try {
            flagRegistry.register(multiplyMobCoinDropsFlag)
        } catch (ex: FlagConflictException) {
            val existingFlag = flagRegistry.get("multiply-mobcoin-drops")
            if (existingFlag is DoubleFlag) {
                multiplyMobCoinDropsFlag = existingFlag
            }
        }
    }

    private fun registerMultiplyMobCoinsDropChanceFlag(flagRegistry: FlagRegistry) {
        try {
            flagRegistry.register(multiplyMobCoinDropChanceFlag)
        } catch (ex: FlagConflictException) {
            val existingFlag = flagRegistry.get("multiply-mobcoin-drop-chance")
            if (existingFlag is DoubleFlag) {
                multiplyMobCoinDropsFlag = existingFlag
            }
        }
    }
}