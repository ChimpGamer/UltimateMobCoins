package nl.chimpgamer.ultimatemobcoins.paper.models.menu

import nl.chimpgamer.ultimatemobcoins.paper.models.menu.action.Action
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class MenuItem(
    val name: String,
    var itemStack: ItemStack? = null,
    var position: Int = -1,
    var message: String? = null,
    var permission: String? = null,
    var price: Double? = null,
    var priceVault: Double? = null,
    var stock: Int? = null,
    var purchaseLimit: Int? = null,
    val actions: MutableList<Action> = ArrayList()
) : Cloneable {
    val purchaseLimits: MutableMap<UUID, Int> = ConcurrentHashMap()

    fun getPlayerPurchaseLimit(uuid: UUID): Int {
        return purchaseLimits[uuid] ?: 0
    }

    fun increasePlayerPurchaseLimit(uuid: UUID) {
        purchaseLimits[uuid] = getPlayerPurchaseLimit(uuid) + 1
    }

    public override fun clone(): MenuItem {
        return super.clone() as MenuItem
    }

    override fun toString(): String {
        return "MenuItem{$name, $itemStack, $position, $message, $permission, $price, $priceVault, $stock, $purchaseLimit}"
    }
}