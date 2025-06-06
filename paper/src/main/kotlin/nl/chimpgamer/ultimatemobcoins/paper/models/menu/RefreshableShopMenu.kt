package nl.chimpgamer.ultimatemobcoins.paper.models.menu

import dev.dejvokep.boostedyaml.YamlDocument
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.configurations.MenuConfig
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.util.logging.Level
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists

abstract class RefreshableShopMenu(plugin: UltimateMobCoinsPlugin, config: MenuConfig) : Menu(plugin, config) {

    // Used when Shop is a rotating shop
    protected lateinit var refreshTime: Instant

    fun hasResetTimer(): Boolean = ::refreshTime.isInitialized

    fun timeToRefresh(): Boolean = hasResetTimer() && Instant.now().isAfter(refreshTime)

    fun getTimeRemaining(): Duration {
        val now = Instant.now()
        return if (now.isBefore(refreshTime)) {
            Duration.between(now, refreshTime)
        } else {
            Duration.ZERO
        }
    }

    fun resetTimeRemaining() {
        val refreshTime = config.getLong("refresh_time")
        if (refreshTime == null || refreshTime <= 0) return
        this.refreshTime = Instant.now().plusSeconds(refreshTime)
    }

    abstract fun refresh()

    abstract fun refreshShopItems()

    init {
        resetTimeRemaining()
    }
}