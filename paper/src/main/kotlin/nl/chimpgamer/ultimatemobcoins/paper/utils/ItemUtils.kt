package nl.chimpgamer.ultimatemobcoins.paper.utils

import dev.dejvokep.boostedyaml.block.implementation.Section
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatemobcoins.paper.extensions.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object ItemUtils {

    fun itemSectionToItemStack(itemSection: Section, tagResolver: TagResolver): ItemStack {
        val itemStack = ItemStack(Material.STONE)

        if (itemSection.contains("material")) {
            val material = runCatching { Material.matchMaterial(itemSection.getString("material")) }.getOrNull()
            if (material != null) {
                itemStack.type(material)
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
        if (itemSection.contains("custom_model_data")) {
            val customModelData = itemSection.getInt("custom_model_data", null)
            if (customModelData != null) {
                itemStack.customModelData(customModelData)
            }
        }

        return itemStack
    }
}