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
    companion object {
        private const val INVENTORY_SIZE = 27
        private const val FIRST_SLOT = 9
        private const val LAST_SLOT = 17
        private const val MIDDLE_SLOT = 13
        private const val FULL_SPIN_THRESHOLD = 50
        private const val FINAL_SPIN_TIME = 60
        private const val PRIZE_DELAY_TICKS = 40
        private const val INITIAL_SLOW_SPIN_VALUE = 120
        private const val INITIAL_CUT_VALUE = 15
    }

    private val inventory: RyseInventory = RyseInventory.builder()
        .size(INVENTORY_SIZE)
        .title(plugin.spinnerManager.menuTitle.parse())
        .provider(createInventoryProvider())
        .disableUpdateTask()
        .build(plugin)

    fun open(player: Player) = inventory.newInstance().open(player)

    private fun createInventoryProvider() = object : InventoryProvider {
        override fun init(player: Player, contents: InventoryContents) {
            initializeInventorySlots(contents)
            launchSpinnerProcess(player, contents)
        }
    }

    private fun initializeInventorySlots(contents: InventoryContents) {
        (FIRST_SLOT..LAST_SLOT).forEach { slot ->
            plugin.spinnerManager.randomPrize?.itemStack?.let {
                contents[slot] = IntelligentItem.empty(it)
            }
        }
    }

    private fun launchSpinnerProcess(player: Player, contents: InventoryContents) {
        plugin.launch(plugin.entityDispatcher(player), CoroutineStart.UNDISPATCHED) {
            runInventory(player, contents)
        }
    }

    private suspend fun runInventory(player: Player, contents: InventoryContents) {
        var spinTime = 1
        var spinCount = 0

        while (true) {
            delay(1.ticks)
            
            when {
                isInitialSpinning(spinCount) -> performInitialSpin(player, contents)
                isSlowSpinning(spinCount, spinTime) -> performSlowSpin(player, contents)
                isPrizeTime(spinTime) -> {
                    handlePrizeGiving(player, contents)
                    break
                }
            }
            
            spinCount++
            if (spinCount > FULL_SPIN_THRESHOLD) spinTime++
        }
    }

    private fun isInitialSpinning(spinCount: Int) = spinCount <= FULL_SPIN_THRESHOLD

    private fun isSlowSpinning(spinCount: Int, spinTime: Int) = spinCount > FULL_SPIN_THRESHOLD && slowSpin().contains(spinTime)

    private fun isPrizeTime(spinTime: Int) = spinTime == FINAL_SPIN_TIME

    private suspend fun handlePrizeGiving(player: Player, contents: InventoryContents) {
        plugin.spinnerManager.prizeWonSound?.play(player)
        val prizeItem = contents[MIDDLE_SLOT].orElse(null)
        
        givePrizeToPlayer(player, prizeItem?.itemStack)
        delay(PRIZE_DELAY_TICKS.ticks)
        closeInventoryIfOpen(player)
    }

    private suspend fun givePrizeToPlayer(player: Player, itemStack: ItemStack?) {
        val prize = plugin.spinnerManager.getPrize(itemStack)
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
    }

    private fun closeInventoryIfOpen(player: Player) {
        if (inventory.openedPlayers.contains(player.uniqueId)) {
            inventory.close(player)
        }
    }

    private fun performInitialSpin(player: Player, contents: InventoryContents) {
        moveItems(contents)
        plugin.spinnerManager.spinningSound?.play(player)
    }

    private fun performSlowSpin(player: Player, contents: InventoryContents) {
        moveItems(contents)
        plugin.spinnerManager.spinningSound?.play(player)
    }

    private fun slowSpin() = sequence {
        var value = INITIAL_SLOW_SPIN_VALUE
        var cut = INITIAL_CUT_VALUE
        while (cut > 0) {
            yield(value)
            value -= cut
            cut--
        }
    }.toList()

    private fun moveItems(contents: InventoryContents) {
        val items = (FIRST_SLOT..LAST_SLOT - 1).map { 
            contents[it].orElse(null)?.itemStack 
        }
        
        plugin.spinnerManager.randomPrize?.itemStack?.let {
            contents.updateOrSet(FIRST_SLOT, it)
        }
        
        items.forEachIndexed { index, item ->
            item?.let { contents.updateOrSet(index + FIRST_SLOT + 1, it) }
        }
    }
}