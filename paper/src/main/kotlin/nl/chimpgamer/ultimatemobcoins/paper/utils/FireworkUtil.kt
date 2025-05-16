package nl.chimpgamer.ultimatemobcoins.paper.utils

import nl.chimpgamer.ultimatemobcoins.paper.extensions.pdc
import nl.chimpgamer.ultimatemobcoins.paper.extensions.setBoolean
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Firework
import kotlin.random.Random

object FireworkUtil {
    val noDamageKey = NamespacedKey("ultimatemobcoins", "no_firework_damage")
    private val colors = setOf(
        Color.AQUA,
        Color.BLUE,
        Color.BLACK,
        Color.GRAY,
        Color.FUCHSIA,
        Color.GREEN,
        Color.LIME,
        Color.MAROON,
        Color.NAVY,
        Color.OLIVE,
        Color.ORANGE,
        Color.PURPLE,
        Color.RED,
        Color.SILVER,
        Color.TEAL,
        Color.WHITE,
        Color.YELLOW
    )

    private val randomEffect: FireworkEffect get() = getRandomEffect(FireworkEffect.Type.entries[Random.nextInt(
        FireworkEffect.Type.entries.size)])

    private fun getRandomEffect(type: FireworkEffect.Type): FireworkEffect {
        return FireworkEffect.builder()
            .flicker(Random.nextBoolean())
            .withColor(colors.random())
            .withFade(colors.random())
            .with(type)
            .trail(Random.nextBoolean())
            .build()
    }

    fun shootRandomFirework(location: Location) {
        val firework = location.world.spawn(location, Firework::class.java)
        val fireworkMeta = firework.fireworkMeta.apply {
            pdc.setBoolean(noDamageKey, true)
            addEffect(randomEffect)
        }
        firework.fireworkMeta = fireworkMeta
    }
}