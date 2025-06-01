package nl.chimpgamer.ultimatemobcoins.paper.models

import com.github.shynixn.mccoroutine.folia.ticks
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Entity
import kotlin.math.cos
import kotlin.math.sin

enum class AnimationType {
    NONE,
    CIRCLE
}

class ConfigurableAnimation(
    private val enabled: Boolean,
    private val type: AnimationType,
    private val particle: String,
    private val duration: Int
) {
    private val bukkitParticle = runCatching { Particle.valueOf(particle.uppercase()) }.getOrNull()

    suspend fun play(entity: Entity, duration: Int = this.duration) {
        if (!enabled) return
        val particle = bukkitParticle
        if (particle == null) {
            println("Tried to play `${this.particle}` but that particle does not exist!")
            return
        }
        if (duration <= 0) return

        when (type) {
            AnimationType.CIRCLE -> {
                val location = entity.location.clone()
                location.add(0.0, 0.4, 0.0)
                val blocks = getCircle(location, 0.3, 5)

                var loop = 0
                for (i in 0 until 20 * duration) {
                    if (loop >= blocks.size) loop = 0
                    if (entity.isDead) break

                    location.world?.spawnParticle(particle, blocks[loop++], 1, 0.0, 0.0, 0.0, 0.0)
                    delay(1.ticks)
                }
            }

            else -> {}
        }
    }

    private fun getCircle(
        center: Location,
        radius: Double,
        amount: Int
    ): List<Location> {
        val world = center.world
        val increment = 2 * Math.PI / amount
        val locations = ArrayList<Location>()
        for (i in 0 until amount) {
            val angle = i * increment
            val x = center.x + radius * cos(angle)
            val z = center.z + radius * sin(angle)
            locations.add(Location(world, x, center.y, z))
        }
        return locations
    }

    companion object {
        fun deserialize(map: Map<String, Any>): ConfigurableAnimation {
            var enabled = false
            var type = AnimationType.NONE
            var particle = ""
            var duration = 1

            if (map.containsKey("enabled")) {
                enabled = map["enabled"].toString().toBoolean()
            }
            if (map.containsKey("type")) {
                type = AnimationType.valueOf(map["type"].toString())
            }
            if (map.containsKey("particle")) {
                particle = map["particle"].toString()
            }
            if (map.containsKey("duration")) {
                duration = map["duration"].toString().toIntOrNull() ?: 1
            }

            return ConfigurableAnimation(enabled, type, particle, duration)
        }
    }
}