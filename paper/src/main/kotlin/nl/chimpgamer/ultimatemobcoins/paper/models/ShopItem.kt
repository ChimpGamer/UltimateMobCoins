package nl.chimpgamer.ultimatemobcoins.paper.models

import nl.chimpgamer.ultimatemobcoins.paper.models.action.Action
import org.bukkit.inventory.ItemStack

class ShopItem(
    val name: String,
    var itemStack: ItemStack? = null,
    var position: Int = -1,
    var message: String? = null,

    var price: Double? = null,
    var stock: Int? = null,
    val actions: MutableList<Action> = ArrayList()
) : Cloneable {

    public override fun clone(): ShopItem {
        return super.clone() as ShopItem
    }
}