package nl.chimpgamer.ultimatemobcoins.paper.models

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.extensions.pdc
import nl.chimpgamer.ultimatemobcoins.paper.extensions.setString
import nl.chimpgamer.ultimatemobcoins.paper.utils.NamespacedKeys
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class SpinnerPrize(
    val name: String,
    var itemStack: ItemStack? = null,
    private val chance: Double,
    private val commands: List<String> = ArrayList(),
    private val message: String = ""
) {
    val success: Boolean get() = if (chance > 0) chance >= Random.nextInt(100) else true

    fun givePrize(player: Player) {
        commands.forEach { command ->
            Bukkit.getServer().dispatchCommand(
                Bukkit.getConsoleSender(),
                command.replace("%player%", player.name)
            )

            println("Executed Prize command `" + command.replace("%player%", player.name) + "`")
        }

        if (message.isNotEmpty()) {
            player.sendRichMessage(message)
        }
    }

    init {
        val chancePlaceholder = Placeholder.unparsed("chance", chance.toString())
        itemStack?.editMeta { meta ->
            if (meta.hasDisplayName()) {
                val displayName = meta.displayName.parse(chancePlaceholder)
                meta.displayName(displayName)
            }
            if (meta.hasLore()) {
                val lore = meta.lore?.map {
                    it.parse(chancePlaceholder)
                }
                meta.lore(lore)
            }

            meta.pdc.setString(NamespacedKeys.spinnerPrizeName, name)
        }
    }
}