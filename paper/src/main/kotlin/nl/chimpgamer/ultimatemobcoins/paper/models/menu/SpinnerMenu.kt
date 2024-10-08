package nl.chimpgamer.ultimatemobcoins.paper.models.menu

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import io.github.rysefoxx.inventory.plugin.content.IntelligentItem
import io.github.rysefoxx.inventory.plugin.content.InventoryContents
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.parse
import nl.chimpgamer.ultimatemobcoins.paper.utils.FireworkUtil
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SpinnerMenu(private val plugin: UltimateMobCoinsPlugin) : InventoryProvider {

    private val inventory: RyseInventory = RyseInventory.builder()
        .size(27)
        .title(plugin.spinnerManager.menuTitle.parse())
        .provider(object : InventoryProvider {
            override fun init(player: Player, contents: InventoryContents) {
                var i = 9
                while (i in 9..17) {
                    plugin.spinnerManager.randomPrize?.itemStack?.let {
                        contents[i] = IntelligentItem.empty(it)
                    }
                    i++
                }
                plugin.launch(plugin.entityDispatcher(player), CoroutineStart.UNDISPATCHED) {
                    runInventory(player, contents)
                }
            }
        })
        .disableUpdateTask()
        .build(plugin)

    fun open(player: Player) = inventory.newInstance().open(player)

    private suspend fun runInventory(player: Player, contents: InventoryContents) {
        var time = 1
        var full = 0
        while (true) {
            delay(1.ticks)
            if (full <= 50) {
                moveItems(contents)
                plugin.spinnerManager.spinningSound?.play(player)
            }

            full++

            if (full > 51) {
                if (slowSpin().contains(time)) {
                    moveItems(contents)
                    plugin.spinnerManager.spinningSound?.play(player)
                }

                time++

                if (time == 60) {
                    plugin.spinnerManager.prizeWonSound?.play(player)
                    val prizeItem = contents[13].orElse(null)
                    val prize = plugin.spinnerManager.getPrize(prizeItem?.itemStack)
                    if (prize != null) {
                        withContext(plugin.globalRegionDispatcher) {
                            prize.givePrize(player)
                        }

                        if (plugin.spinnerManager.shootFireworks) {
                            FireworkUtil.shootRandomFirework(player.location)
                        }
                    } else {
                        plugin.logger.warning("No prize was found!")
                    }

                    delay(40.ticks)
                    if (inventory.openedPlayers.contains(player.uniqueId)) {
                        inventory.close(player)
                    }
                    break
                }
            }
        }
    }

    private fun slowSpin(): ArrayList<Int> {
        val slow = ArrayList<Int>()
        var full = 120
        var cut = 15
        var i = 120
        while (cut > 0) {
            if (full <= i - cut || full >= i - cut) {
                slow.add(i)
                i -= cut
                cut--
            }
            full--
        }
        return slow
    }

    private fun moveItems(contents: InventoryContents) {
        val items = ArrayList<ItemStack?>()
        var i = 9
        while (i in 9..16) {
            items.add(contents[i].orElse(null)?.itemStack)
            i++
        }
        plugin.spinnerManager.randomPrize?.itemStack?.let {
            contents.updateOrSet(9, it)
        }
        for (j in 0..7) {
            val item = items[j] ?: continue
            contents.updateOrSet(j + 10, item)
        }
    }
}