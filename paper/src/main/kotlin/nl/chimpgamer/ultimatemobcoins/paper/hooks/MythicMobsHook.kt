package nl.chimpgamer.ultimatemobcoins.paper.hooks

import io.lumine.mythic.api.adapters.AbstractItemStack
import io.lumine.mythic.api.drops.DropMetadata
import io.lumine.mythic.api.drops.IItemDrop
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.adapters.BukkitItemStack
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import io.lumine.mythic.core.mobs.ActiveMob
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.registerEvents
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class MythicMobsHook(private val plugin: UltimateMobCoinsPlugin) : Listener {

    private val isPluginEnabled get() = plugin.server.pluginManager.isPluginEnabled("MythicMobs")

    fun load() {
        if (isPluginEnabled) {
            plugin.registerEvents(this)
            plugin.logger.info("Successfully loaded MythicMobs hook!")
        }
    }

    fun unload() {
        HandlerList.unregisterAll(this)
    }

    fun isMythicMob(entity: Entity): Boolean {
        return if (isPluginEnabled) {
            MythicBukkit.inst().mobManager.isActiveMob(entity.uniqueId)
        } else {
            false
        }
    }

    @EventHandler
    fun MythicDropLoadEvent.onMythicDropLoad() {
        if (dropName.equals("mobcoin", ignoreCase = true)) {
            register(MobCoinDrop(plugin))
            //plugin.logger.info("Loaded `mobcoin` drop for MythicMobs!")
        }
    }

    class MobCoinDrop(private val plugin: UltimateMobCoinsPlugin) : IItemDrop {
        override fun getDrop(data: DropMetadata?, amount: Double): AbstractItemStack {
            val dropper = data?.dropper?.orElse(null)
            if (dropper is ActiveMob) {
                val trigger = data.trigger
                if (trigger?.bukkitEntity is Player) {
                    val mobCoinItem = plugin.mobCoinsManager.getCoin(trigger.bukkitEntity as Player, dropper.type.internalName)
                    if (mobCoinItem != null) {
                        return BukkitItemStack(mobCoinItem)
                    }
                }
            }
            return BukkitItemStack(Material.AIR)
        }
    }
}