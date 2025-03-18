package nl.chimpgamer.ultimatemobcoins.paper.utils

import com.destroystokyo.paper.profile.ProfileProperty
import dev.dejvokep.boostedyaml.block.implementation.Section
import dev.lone.itemsadder.api.CustomStack
import io.th0rgal.oraxen.api.OraxenItems
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.*
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionType
import java.util.*

object ItemUtils {
    private val isOraxenEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("Oraxen")
    private val isItemsAdderEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")

    private val skullOwnerNamespacedKey = NamespacedKey("ultimatemobcoins", "skull_owner")

    fun itemSectionToItemStack(plugin: UltimateMobCoinsPlugin, itemSection: Section, tagResolver: TagResolver): ItemStack {
        var itemStack = ItemStack(Material.STONE)

        if (itemSection.contains("material")) {
            val material = runCatching { Material.matchMaterial(itemSection.getString("material")) }.getOrNull()
            if (material != null) {
                itemStack.type(material)
            }
        }
        if (itemSection.contains("oraxen")) {
            if (isOraxenEnabled) {
                val oraxen = itemSection.getString("oraxen")
                if (OraxenItems.exists(oraxen)) {
                    itemStack = OraxenItems.getItemById(oraxen).build()
                } else {
                    plugin.logger.info("Could not find Oraxen item $oraxen")
                }
            } else {
                plugin.logger.info("Could not use Oraxen. Oraxen is not installed or enabled!")
            }
        }
        if (itemSection.contains("itemsadder")) {
            if (isItemsAdderEnabled) {
                val itemsadder = itemSection.getString("itemsadder")
                val customStack = CustomStack.getInstance(itemsadder)
                if (customStack != null) {
                    itemStack = customStack.itemStack
                    itemStack.meta {
                        displayName
                    }
                } else {
                    plugin.logger.info("Could not find ItemsAdder item $itemsadder")
                }
            } else {
                plugin.logger.info("Could not use ItemsAdder. ItemsAdder is not installed or enabled!")
            }
        }
        if (itemSection.contains("name")) {
            val name = itemSection.getString("name")
            itemStack.name(name.parse(tagResolver))
        }
        if (itemSection.contains("lore")) {
            val lore = itemSection.getStringList("lore")
            itemStack.lore(lore.map { it.parse(tagResolver) })
        }
        if (itemSection.contains("glow")) {
            val glow = itemSection.getBoolean("glow")
            itemStack.glow(glow)
        }
        if (itemSection.contains("model_data")) {
            val modelData = itemSection.getInt("model_data", null)
            if (modelData != null) {
                itemStack.customModelData(modelData)
            }
        }
        if (itemSection.contains("skull") && itemStack.type === Material.PLAYER_HEAD) {
            val headDatabaseHook = plugin.hookManager.headDatabaseHook

            val skullData = itemSection.getString("skull")
            if (headDatabaseHook.isLoaded && skullData.startsWith("hdb:")) {
                val hdbSkull = skullData.replace("hdb:", "")
                if (hdbSkull.equals("random", ignoreCase = true)) {
                    headDatabaseHook.getRandomHead()
                } else {
                    headDatabaseHook.getHead(hdbSkull) ?: itemStack
                }
            } else {
                itemStack.customSkull(skullData)
            }
        }

        return itemStack
    }

    fun itemDataToItemStack(plugin: UltimateMobCoinsPlugin, itemData: List<String>): ItemStack {
        var itemStack = ItemStack(Material.STONE)
        itemData.forEach { data ->
            val parts = data.split(":", limit = 2)
            val name = parts[0].trim()
            val value = parts[1]

            if (name == "material") {
                val material = runCatching { Material.matchMaterial(value) }.getOrNull()
                if (material != null) {
                    itemStack = itemStack.type(material)
                }
            } else if (name == "oraxen") {
                if (isOraxenEnabled) {
                    if (OraxenItems.exists(value)) {
                        itemStack = OraxenItems.getItemById(value).build()
                    } else {
                        plugin.logger.info("Could not find Oraxen item $value")
                    }
                } else {
                    plugin.logger.info("Could not use Oraxen. Oraxen is not installed or enabled!")
                }
            } else if (name == "itemsadder") {
                if (isItemsAdderEnabled) {
                    val customStack = CustomStack.getInstance(value)
                    if (customStack != null) {
                        itemStack = customStack.itemStack
                        itemStack.meta {
                            // For some reason ItemsAdder puts legacy color coding by default on the item?
                            displayName(displayName.parseLegacy())
                        }
                    } else {
                        plugin.logger.info("Could not find ItemsAdder item $value")
                    }
                } else {
                    plugin.logger.info("Could not use ItemsAdder. ItemsAdder is not installed or enabled!")
                }
            } else if (name == "amount") {
                val amount = value.toIntOrNull()
                if (amount != null) {
                    itemStack = itemStack.amount(amount)
                }
            } else if (name == "name") {
                itemStack = itemStack.name(value)
            } else if (name == "lore") {
                itemStack = if (value.contains(newlineSplitRegex)) {
                    itemStack.lore(value.split(newlineSplitRegex))
                } else {
                    itemStack.lore(value)
                }
            } else if (name == "itemflag") {
                if (value.toBoolean() || value.equals("all", ignoreCase = true)) {
                    itemStack = itemStack.flag(*ItemFlag.entries.toTypedArray())
                } else {
                    val itemFlags = value.split("#")

                    for (itemFlagStr in itemFlags) {
                        val itemFlag = runCatching { ItemFlag.valueOf(itemFlagStr.uppercase()) }.getOrNull()
                        if (itemFlag != null) {
                            itemStack = itemStack.flag(itemFlag)
                        }
                    }
                }
            } else if (name == "glow") {
                val glow = value.toBoolean()
                itemStack.glow(glow)
            } else if (name == "enchantment") {
                val enchantmentParts = value.split("#")
                if (enchantmentParts.size != 2) {
                    return@forEach
                }
                val enchantmentName = enchantmentParts[0].trim().lowercase()
                val level = enchantmentParts[1].trim().toIntOrNull() ?: -1

                val enchantment =
                    runCatching { Enchantment.getByKey(NamespacedKey.minecraft(enchantmentName)) }.getOrNull()
                if (enchantment != null) {
                    itemStack = itemStack.enchantment(enchantment, level)
                }
            } else if (name == "potion") {
                if (itemStack.itemMeta is PotionMeta) {
                    val potionParts = value.split("#")

                    val potionTypeName = potionParts[0].trim()
                    val extended = potionParts[1].trim().toBooleanStrictOrNull() ?: false
                    val upgraded = potionParts[2].trim().toBooleanStrictOrNull() ?: false

                    val potionType =
                        PotionType.entries.firstOrNull { potionTypeName.equals(it.name, ignoreCase = true) }
                    if (potionType != null) {
                        itemStack = itemStack.potion(potionType, extended, upgraded)
                    }
                }
            } else if (name == "color") {
                val colorParts = value.split("#")
                if (colorParts.size != 3) {
                    plugin.logger.info("Invalid format for colors!")
                    return@forEach
                }

                val red = colorParts[0].trim().toIntOrNull() ?: 0
                val green = colorParts[1].trim().toIntOrNull() ?: 0
                val blue = colorParts[2].trim().toIntOrNull() ?: 0
                val color = Color.fromRGB(red, green, blue)

                itemStack = itemStack.color(color)
            } else if (name == "modeldata") {
                val modelData = value.toIntOrNull()
                if (modelData == null) {
                    plugin.logger.warning("Invalid model data!")
                    return@forEach
                }
                itemStack = itemStack.customModelData(modelData)
            } else if (name == "skull" || name == "playerhead") {
                if (itemStack.type === Material.PLAYER_HEAD) {
                    if (Utils.containsPlaceholder(value)) {
                        itemStack.editMeta { meta ->
                            val pdc = meta.persistentDataContainer
                            pdc.set(skullOwnerNamespacedKey, PersistentDataType.STRING, value)
                        }
                    } else {
                        val headDatabaseHook = plugin.hookManager.headDatabaseHook
                        val valueAsUUID = runCatching { UUID.fromString(value) }.getOrNull()
                        val offlinePlayer = if (valueAsUUID != null) {
                            Bukkit.getOfflinePlayer(valueAsUUID)
                        } else {
                            Bukkit.getOfflinePlayerIfCached(value)
                        }

                        itemStack = if (offlinePlayer != null) {
                            itemStack.skull(offlinePlayer)
                        } else if (headDatabaseHook.isLoaded && value.startsWith("hdb:")) {
                            val hdbSkull = value.replace("hdb:", "")
                            if (hdbSkull.equals("random", ignoreCase = true)) {
                                headDatabaseHook.getRandomHead()
                            } else {
                                headDatabaseHook.getHead(hdbSkull) ?: itemStack
                            }
                        } else {
                            itemStack.customSkull(value)
                        }
                    }
                }
            }
        }
        return itemStack
    }

    fun updateItem(itemStack: ItemStack, player: Player, tagResolver: TagResolver = TagResolver.empty()) {
        itemStack.editMeta { meta ->
            if (meta.hasDisplayName()) {
                val displayName = meta.displayName.parse(player, tagResolver)
                meta.displayName(displayName)
            }
            if (meta.hasLore()) {
                val lore = meta.lore?.map {
                    it.parse(player, tagResolver)
                }
                meta.lore(lore)
            }

            if (meta is SkullMeta) {
                if (!meta.hasOwner()) {
                    val pdc = meta.persistentDataContainer
                    pdc.get(skullOwnerNamespacedKey, PersistentDataType.STRING)
                        ?.let { skullOwner ->
                            val finalSkullOwner = Utils.applyPlaceholders(skullOwner, player)

                            if (finalSkullOwner.length == 180) {
                                // It is probably base64 encoded
                                val profile = Bukkit.getServer().createProfile(UUID.randomUUID())
                                profile.setProperty(ProfileProperty("textures", finalSkullOwner))
                                meta.playerProfile = profile
                                return@editMeta
                            }

                            val uuid = runCatching { UUID.fromString(finalSkullOwner) }.getOrNull()
                            val offlinePlayer = if (uuid != null) {
                                Bukkit.getOfflinePlayer(uuid)
                            } else {
                                Bukkit.getOfflinePlayerIfCached(finalSkullOwner)
                            }

                            if (offlinePlayer == null) {
                                println("Could not set player head to $finalSkullOwner because that data is not cached!")
                            }
                            if (!meta.setOwningPlayer(offlinePlayer)) {
                                println("Failed to set ${offlinePlayer?.name} as Owning Player of player head!")
                            }
                        }
                }
            }
        }
    }
}