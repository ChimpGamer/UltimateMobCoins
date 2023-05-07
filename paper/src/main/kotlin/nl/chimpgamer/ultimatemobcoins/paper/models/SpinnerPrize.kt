package nl.chimpgamer.ultimatemobcoins.paper.models

import de.tr7zw.nbtapi.NBTItem
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
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

            println("Executed Prize command " + command.replace("%player%", player.name))
        }

        if (message.isNotEmpty()) {
            player.sendRichMessage(message)
        }
    }

    init {
        val chancePlaceholder = Placeholder.unparsed("chance", chance.toString())
        itemStack?.editMeta { meta ->
            val displayName = meta.displayName.parse(chancePlaceholder)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            val lore = meta.lore?.map {
                it.parse(chancePlaceholder)
                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            }
            meta.displayName(displayName)
            meta.lore(lore)
        }

        val nbtItem = NBTItem(itemStack)
        nbtItem.setString("ultimatemobcoins.spinner.prize.name", name)

        itemStack = nbtItem.item
    }
}