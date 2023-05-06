package nl.chimpgamer.ultimatemobcoins.paper.models.menu

import org.bukkit.Sound
import org.bukkit.entity.Player

class MenuSound(
    val enabled: Boolean,
    val name: String,
    val volume: Float,
    val pitch: Float
) {
    val bukkitSound = runCatching { Sound.valueOf(name.uppercase()) }.getOrNull()

    fun play(player: Player) {
        if (!enabled) return
        val bukkitSound = this.bukkitSound ?: return
        player.playSound(player.location, bukkitSound, volume, pitch)
    }

    companion object {
        fun deserialize(map: Map<String, Any>): MenuSound {
            var enabled = false
            var name = ""
            var volume = 1.0F
            var pitch = 1.0F

            if (map.containsKey("Enabled")) {
                enabled = map["Enabled"].toString().toBoolean()
            }
            if (map.containsKey("Name")) {
                name = map["Name"].toString()
            }
            if (map.containsKey("Volume")) {
                volume = map["Volume"].toString().toFloat()
            }
            if (map.containsKey("Pitch")) {
                pitch = map["Pitch"].toString().toFloat()
            }

            return MenuSound(enabled, name, volume, pitch)
        }
    }
}