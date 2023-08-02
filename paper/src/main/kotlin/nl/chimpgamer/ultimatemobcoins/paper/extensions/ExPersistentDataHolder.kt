package nl.chimpgamer.ultimatemobcoins.paper.extensions

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType

inline fun PersistentDataHolder.pdc(
    block: PersistentDataContainer.() -> Unit
) = persistentDataContainer.also(block)

val PersistentDataHolder.pdc get() = this.persistentDataContainer

fun PersistentDataContainer.setBoolean(key: NamespacedKey, value: Boolean) = set(key, PersistentDataType.BYTE, if (value) 1.toByte() else 0.toByte())

fun PersistentDataContainer.getBoolean(key: NamespacedKey) =
    get(key, PersistentDataType.BYTE) == 1.toByte()

fun PersistentDataContainer.setString(key: NamespacedKey, value: String) = set(key, PersistentDataType.STRING, value)

fun PersistentDataContainer.getString(key: NamespacedKey) = get(key, PersistentDataType.STRING)

fun PersistentDataContainer.setDouble(key: NamespacedKey, value: Double) = set(key, PersistentDataType.DOUBLE, value)

fun PersistentDataContainer.getDouble(key: NamespacedKey) = get(key, PersistentDataType.DOUBLE)