package nl.chimpgamer.ultimatemobcoins.paper.hooks

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import dev.rosewood.rosestacker.api.RoseStackerAPI
import dev.rosewood.rosestacker.config.SettingKey
import dev.rosewood.rosestacker.event.EntityStackMultipleDeathEvent
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.registerSuspendingEvents
import nl.chimpgamer.ultimatemobcoins.paper.listeners.RoseStackerListener
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDeathEvent

class RoseStackerHook(plugin: UltimateMobCoinsPlugin) : PluginHook(plugin, "RoseStacker") {
    private lateinit var roseStackerListener: RoseStackerListener

    override fun load() {
        if (!isLoaded && canHook()) {
            roseStackerListener = RoseStackerListener(plugin)
            plugin.registerSuspendingEvents(roseStackerListener, eventDispatcher = mapOf(
                Pair(EntityStackMultipleDeathEvent::class.java) {
                    require(it is EntityStackMultipleDeathEvent)
                    plugin.entityDispatcher(it.stack.entity)
                },
            ))

            isLoaded = true
            plugin.logger.info("Successfully loaded $pluginName hook!")
        }
    }

    override fun unload() {
        if (!isLoaded) return

        HandlerList.unregisterAll(roseStackerListener)
    }

    fun isEntityStacked(livingEntity: LivingEntity): Boolean {
        if (!isLoaded) return true
        return RoseStackerAPI.getInstance().isEntityStacked(livingEntity)
    }

    fun areMultipleEntitiesDying(event: EntityDeathEvent): Boolean {
        if (!isLoaded) return false
        val stackedEntity = RoseStackerAPI.getInstance().getStackedEntity(event.entity) ?: return false
        return stackedEntity.areMultipleEntitiesDying(event)
    }

    fun shouldIgnoreNormalDeathEvent(entity: LivingEntity): Boolean {
        if (!isLoaded) return false
        val api = RoseStackerAPI.getInstance()
        val stackedEntity = api.getStackedEntity(entity)

        if (!api.isEntityStackMultipleDeathEventCalled || stackedEntity == null || stackedEntity.stackSize == 1) return false

        if (stackedEntity.isEntireStackKilledOnDeath) return true

        val killer = entity.killer
        if (!SettingKey.ENTITY_MULTIKILL_ENABLED.get()) return false

        if (!SettingKey.ENTITY_MULTIKILL_PLAYER_ONLY.get() && killer == null) return false

        if (!SettingKey.ENTITY_MULTIKILL_ENCHANTMENT_ENABLED.get()) return true

        if (killer == null) return false

        val enchantment = Enchantment.getByKey(NamespacedKey.fromString(SettingKey.ENTITY_MULTIKILL_ENCHANTMENT_TYPE.get())) ?: return false

        return killer.inventory.itemInMainHand.getEnchantmentLevel(enchantment) > 0
    }
}