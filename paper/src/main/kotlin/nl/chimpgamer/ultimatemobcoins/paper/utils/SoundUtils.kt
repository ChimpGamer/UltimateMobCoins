package nl.chimpgamer.ultimatemobcoins.paper.utils

import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Sound

object SoundUtils {

    fun getSound(name: String): Sound? {
        if (Sound::class.java.isEnum) {
            return runCatching { Sound.valueOf(name.uppercase()) }.getOrNull()
        }
        val fromRegistry = Registry.SOUNDS.get(NamespacedKey.minecraft(name.lowercase()))
        if (fromRegistry != null) {
            return fromRegistry
        }
        return runCatching { Sound::class.java.getDeclaredField(name.uppercase()).get(null) as Sound? }.getOrNull()
    }
}