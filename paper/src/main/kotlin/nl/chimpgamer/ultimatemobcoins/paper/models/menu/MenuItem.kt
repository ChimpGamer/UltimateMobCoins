package nl.chimpgamer.ultimatemobcoins.paper.models.menu

import nl.chimpgamer.ultimatemobcoins.paper.models.menu.action.Action
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.random.Random

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
    var chance: Int = 0,
    val actions: MutableList<Action> = ArrayList()
) : Cloneable {
    var purchaseLimits: MutableMap<UUID, Int> = ConcurrentHashMap()

    val success: Boolean get() = if (chance > 0) chance >= Random.nextInt(CHANCE_MAX) else true

    val isShopItem: Boolean = name != "filler" && (price != null || priceVault != null)
    val isRotatingShopItem: Boolean = isShopItem && position == -1

    fun getPlayerPurchaseLimit(uuid: UUID): Int {
        return purchaseLimits[uuid] ?: 0
    }

    fun increasePlayerPurchaseLimit(uuid: UUID) {
        purchaseLimits[uuid] = getPlayerPurchaseLimit(uuid) + 1
    }

    fun hasReachedPlayerPurchaseLimit(uuid: UUID, purchaseLimit: Int): Boolean {
        return purchaseLimits[uuid]?.let { it >= purchaseLimit } ?: false
    }

    fun hasPermission(player: Player): Boolean {
        val permissionNode = permission ?: return true
        return if (permissionNode.startsWith(NEGATION_PREFIX)) {
            !player.hasPermission(permissionNode.substring(1))
        } else {
            player.hasPermission(permissionNode)
        }
    }

    public override fun clone(): MenuItem {
        return super.clone() as MenuItem
    }

    override fun toString(): String {
        return "MenuItem{$name, $itemStack, $position, $message, $permission, $price, $priceVault, $stock, $purchaseLimit, $chance}"
    }

    private companion object {
        const val CHANCE_MAX = 100
        const val NEGATION_PREFIX = "-"
    }

}