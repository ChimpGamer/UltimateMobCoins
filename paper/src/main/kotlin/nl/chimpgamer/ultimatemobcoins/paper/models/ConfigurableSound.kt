package nl.chimpgamer.ultimatemobcoins.paper.models

import org.bukkit.Sound
import org.bukkit.entity.Player

class ConfigurableSound(
    private val enabled: Boolean,
    private val sound: String,
    private val volume: Float,
    private val pitch: Float
) {
    private val bukkitSound = runCatching { Sound.valueOf(sound.uppercase()) }.getOrNull()

    fun play(player: Player) {
        if (!enabled) return
        val bukkitSound = this.bukkitSound ?: return
        player.playSound(player.location, bukkitSound, volume, pitch)
    }

    companion object {
        fun deserialize(map: Map<String, Any>): ConfigurableSound {
            var enabled = false
            var name = ""
            var volume = 1.0F
            var pitch = 1.0F

            if (map.containsKey("enabled")) {
                enabled = map["enabled"].toString().toBoolean()
            }
            if (map.containsKey("name")) {
                name = map["name"].toString()
            }
            if (map.containsKey("volume")) {
                volume = map["volume"].toString().toFloat()
            }
            if (map.containsKey("pitch")) {
                pitch = map["pitch"].toString().toFloat()
            }

            return ConfigurableSound(enabled, name, volume, pitch)
        }
    }
}