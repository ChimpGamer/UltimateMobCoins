package nl.chimpgamer.ultimatemobcoins.paper.hooks

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit

class VaultHook {
    lateinit var economy: Economy

    fun initialize() {
        val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java) ?: return
        economy = rsp.provider
    }

    fun isInitialized() = this::economy.isInitialized
}