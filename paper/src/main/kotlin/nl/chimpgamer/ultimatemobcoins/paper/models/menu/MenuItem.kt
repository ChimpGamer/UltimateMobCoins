package nl.chimpgamer.ultimatemobcoins.paper.models.menu

import nl.chimpgamer.ultimatemobcoins.paper.models.menu.action.Action
import org.bukkit.inventory.ItemStack

class MenuItem(
    val name: String,
    var itemStack: ItemStack? = null,
    var position: Int = -1,
    var message: String? = null,
    var permission: String? = null,
    var price: Double? = null,
    var stock: Int? = null,
    val actions: MutableList<Action> = ArrayList()
) : Cloneable {

    public override fun clone(): MenuItem {
        return super.clone() as MenuItem
    }
}