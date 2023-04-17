package nl.chimpgamer.ultimatemobcoins.paper.extensions

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import org.bukkit.material.Colorable
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType
import kotlin.collections.ArrayList

fun ItemStack.amount(amount: Int): ItemStack {
    setAmount(amount)
    return this
}

fun ItemStack.name(name: String): ItemStack {
    meta {
        setDisplayName(name)
    }
    return this
}

fun ItemStack.name(name: Component): ItemStack {
    meta {
        displayName(name)
    }
    return this
}

fun ItemStack.lore(text: Component): ItemStack {
    meta {
        val lore = lore() ?: ArrayList()
        lore.add(text)
        lore(lore)
    }
    return this
}

fun ItemStack.lore(text: String): ItemStack {
    val meta = itemMeta
    val lore = meta?.lore ?: ArrayList()
    lore.add(text)
    meta.lore = lore
    itemMeta = meta
    return this
}

fun ItemStack.lore(vararg text: String): ItemStack {
    lore(text.toList())
    return this
}

fun ItemStack.lore(text: List<String>): ItemStack {
    meta {
        val lore = this.lore ?: ArrayList()
        text.forEach {
            lore.add(it)
        }
        this.lore = lore
    }
    return this
}

fun ItemStack.enchantment(enchantment: Enchantment, level: Int): ItemStack {
    if (type === Material.ENCHANTED_BOOK) {
        if (itemMeta == null) return this
        val enchantmentStorageMeta = itemMeta as EnchantmentStorageMeta
        enchantmentStorageMeta.addStoredEnchant(enchantment, level, true)
        itemMeta = enchantmentStorageMeta
        return this
    }
    addUnsafeEnchantment(enchantment, level)
    return this
}

fun ItemStack.enchantment(enchantment: Enchantment): ItemStack {
    enchantment(enchantment, 1)
    return this
}

fun ItemStack.type(material: Material): ItemStack {
    type = material
    return this
}

fun ItemStack.clearLore(): ItemStack {
    meta {
        lore(ArrayList())
    }
    return this
}

fun ItemStack.clearEnchantments(): ItemStack {
    enchantments.keys.forEach { removeEnchantment(it) }
    return this
}

@Throws(IllegalArgumentException::class)
fun ItemStack.color(color: Color): ItemStack {
    if (itemMeta is Colorable) {
        val colorable = itemMeta as Colorable
        val dyeColor = DyeColor.getByColor(color)
        colorable.color = dyeColor
        itemMeta = colorable as ItemMeta
    }

    if (itemMeta is LeatherArmorMeta) {
        val leatherArmorMeta = itemMeta as LeatherArmorMeta
        leatherArmorMeta.setColor(color)
        itemMeta = leatherArmorMeta
    }

    if (itemMeta is PotionMeta) {
        val potionMeta = itemMeta as PotionMeta
        potionMeta.color = color
        itemMeta = potionMeta
    }

    throw IllegalArgumentException("Colors are not applicable for this type of material!")
}

fun ItemStack.glow(glow: Boolean = true): ItemStack {
    if (glow) {
        enchantment(Enchantment.LURE)
        addItemFlags(ItemFlag.HIDE_ENCHANTS)
    } else {
        removeEnchantment(Enchantment.LURE)
        removeItemFlags(ItemFlag.HIDE_ENCHANTS)
    }
    return this
}

fun ItemStack.flag(vararg flag: ItemFlag): ItemStack {
    addItemFlags(*flag)
    return this
}

fun ItemStack.skull(offlinePlayer: OfflinePlayer): ItemStack {
    val skullMeta = itemMeta as SkullMeta
    skullMeta.owningPlayer = offlinePlayer
    itemMeta = skullMeta
    return this
}

fun ItemStack.potion(potionType: PotionType, extended: Boolean = false, upgraded: Boolean = false): ItemStack {
    meta {
        if (this is PotionMeta) {
            this.basePotionData = PotionData(potionType, extended, upgraded)
        }
    }
    return this
}

fun ItemStack.customModelData(customModelData: Int) : ItemStack {
    editMeta {
        it.setCustomModelData(customModelData)
    }
    return this
}

inline fun ItemStack.meta(block: ItemMeta.() -> Unit) {
    itemMeta = itemMeta.apply {
        block(this)
    }
}
