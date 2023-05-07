package nl.chimpgamer.ultimatemobcoins.paper.listeners

import nl.chimpgamer.ultimatemobcoins.paper.extensions.getBoolean
import nl.chimpgamer.ultimatemobcoins.paper.extensions.pdc
import nl.chimpgamer.ultimatemobcoins.paper.utils.FireworkUtil
import org.bukkit.entity.Firework
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class FireworkListener : Listener {

    @EventHandler
    fun EntityDamageByEntityEvent.onEntityDamageByEntity() {
        if (damager is Firework && damager.pdc.getBoolean(FireworkUtil.noDamageKey)) {
            isCancelled = true
        }
    }
}