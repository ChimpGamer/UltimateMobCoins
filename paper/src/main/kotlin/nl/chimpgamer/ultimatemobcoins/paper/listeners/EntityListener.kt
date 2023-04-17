package nl.chimpgamer.ultimatemobcoins.paper.listeners

import de.tr7zw.nbtapi.NBTItem
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.name
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.math.BigDecimal

class EntityListener(private val plugin: UltimateMobCoinsPlugin) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityDeathEvent.onEntityDeath() {
        val killer = entity.killer ?: return

        val mobCoinItem = getCoin(killer, entity) ?: return
        if (drops.any { it.type === mobCoinItem.type }) return

        drops.add(mobCoinItem)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun ItemSpawnEvent.onItemSpawn() {
        val itemStack = entity.itemStack
        val nbtItem = NBTItem(itemStack)
        if (!nbtItem.hasNBTData()
            && !nbtItem.hasTag("isMobCoin") ||
            !nbtItem.getBoolean("isMobCoin")) return
        entity.setMetadata("NO_PICKUP", FixedMetadataValue(plugin, true)) // UpgradableHoppers support
    }

    private fun getCoin(killer: Player, entity: Entity): ItemStack? {
        if (!killer.hasPermission("ultimatemobcoins.dropcoin")) return null

        val mobCoinItem = ItemStack(Material.SUNFLOWER)
            .name("***MobCoin") // EpicHoppers ignores the item if the name starts with *** (https://github.com/songoda/EpicHoppers/blob/master/src/main/java/com/songoda/epichoppers/hopper/levels/modules/ModuleSuction.java#L91)

        val dropAmount = plugin.mobCoinsManager.getMobCoin(entity.type)?.getAmountToDrop(killer) ?: return null
        if (dropAmount == BigDecimal.ZERO) return null

        val nbtMobCoin = NBTItem(mobCoinItem)
        nbtMobCoin.setBoolean("isMobCoin", true)
        nbtMobCoin.setDouble("amount", dropAmount.toDouble())

        return nbtMobCoin.item
    }
}