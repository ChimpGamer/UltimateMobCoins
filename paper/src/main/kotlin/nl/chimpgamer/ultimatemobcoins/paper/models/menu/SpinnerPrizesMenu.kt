package nl.chimpgamer.ultimatemobcoins.paper.models.menu

import io.github.rysefoxx.inventory.plugin.content.InventoryContents
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import org.bukkit.entity.Player

class SpinnerPrizesMenu(private val plugin: UltimateMobCoinsPlugin) : InventoryProvider {

    val inventory = RyseInventory.builder()
        .size(54)
        .title("<gold>MobCoin Spinner Prizes".parse())
        .provider(this)
        .disableUpdateTask()
        .build(plugin)

    override fun init(player: Player, contents: InventoryContents) {
        plugin.spinnerManager.prizes.forEach { prize -> contents.add(prize.itemStack) }
    }
}