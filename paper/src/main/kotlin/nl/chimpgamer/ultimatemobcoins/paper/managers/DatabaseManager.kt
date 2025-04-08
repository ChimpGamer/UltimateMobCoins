package nl.chimpgamer.ultimatemobcoins.paper.managers

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.storage.dao.UserDao

abstract class DatabaseManager(private val plugin: UltimateMobCoinsPlugin) {
    abstract val userDao: UserDao

    val databaseDispatcher get() = plugin.asyncDispatcher

    abstract fun initialize()

    abstract fun close()

    abstract suspend fun databaseNameAndVersion(): String
}