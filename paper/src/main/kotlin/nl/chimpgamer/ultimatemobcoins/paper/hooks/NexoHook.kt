package nl.chimpgamer.ultimatemobcoins.paper.hooks

import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import com.nexomc.nexo.api.NexoItems
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

class NexoHook(plugin: UltimateMobCoinsPlugin) : PluginHook(plugin, "Nexo") {
    @Volatile var isReady: Boolean = false
        private set

    private val cache = ConcurrentHashMap<String, ItemStack>()

    override fun load() {
        if (!isLoaded && canHook()) {
            // Warm/verify the registry once
            isReady = ensureNexoRegistryLoaded()
            isLoaded = true
            plugin.logger.info("Successfully loaded $pluginName hook! (ready=$isReady)")
        }
    }

    override fun unload() {
        cache.clear()
        isReady = false
        isLoaded = false
    }

    /** Build a Nexo item by id, with normalization + one-time recovery steps. */
    fun build(raw: String): ItemStack? {
        val id = normalize(raw)
        // Fast cache
        cache[id]?.let { return it.clone() }

        // Ensure registry ready
        if (!isReady) isReady = ensureNexoRegistryLoaded()

        // Try resolve
        var builder = runCatching { NexoItems.optionalItemFromId(id).orElse(null) }.getOrNull()
        if (builder == null) {
            // Try reloading just this item, then re-query
            runCatching { NexoItems.reloadItem(id) }
            builder = runCatching { NexoItems.optionalItemFromId(id).orElse(null) }.getOrNull()
        }

        val built = runCatching { builder?.build() }.getOrNull()
        if (built != null) cache[id] = built.clone()
        else plugin.logger.warning("$pluginName hook → could not resolve item '$id' (raw='$raw')")

        return built
    }

    fun exists(raw: String): Boolean =
        runCatching { NexoItems.exists(normalize(raw)) }.getOrDefault(false)

    fun idFrom(item: ItemStack?): String? =
        runCatching { NexoItems.idFromItem(item) }.getOrNull()

    /** Load/verify registry once. */
    // Should be Fixed for the rules now....
    private fun ensureNexoRegistryLoaded(): Boolean {
        val names = runCatching { NexoItems.itemNames() }.getOrElse { emptySet() }
        if (names.isEmpty()) {
            runCatching { NexoItems.loadItems() }.onFailure {
                plugin.logger.warning("$pluginName hook → loadItems() failed: ${it.javaClass.simpleName}: ${it.message}")
            }
        }
        return runCatching { NexoItems.itemNames().isNotEmpty() }.getOrDefault(false)
    }

    private fun normalize(raw: String): String =
        raw.trim().trim('"','\'').removePrefix("nexo:").removePrefix("NEXO:").trim()
}
