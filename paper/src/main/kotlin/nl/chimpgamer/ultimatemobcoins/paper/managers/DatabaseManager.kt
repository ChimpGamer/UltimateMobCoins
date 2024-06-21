package nl.chimpgamer.ultimatemobcoins.paper.managers

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.storage.user.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseManager(private val plugin: UltimateMobCoinsPlugin) {
    private lateinit var database: Database

    val isDatabaseInitialized: Boolean get() = this::database.isInitialized

    val databaseDispatcher get() = plugin.asyncDispatcher

    private fun connect() {
        val databaseFile = plugin.dataFolder.resolve("data.db")
        val settings = plugin.settingsConfig
        val storageType = settings.storageType.lowercase()

        if (storageType == "sqlite") {
            val hikariConfig = HikariConfig().apply {
                poolName = "UltimateMobCoins-pool"
                jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"
                driverClassName = "org.sqlite.JDBC"
                maximumPoolSize = 1
                transactionIsolation = "TRANSACTION_SERIALIZABLE"
            }
            database = Database.connect(HikariDataSource(hikariConfig), databaseConfig = DatabaseConfig {
                defaultMinRetryDelay = 100L
                defaultMinRepetitionDelay = 100L
            })
        } else if (storageType == "mysql" || storageType == "mariadb") {
            val host = settings.storageHost
            val port = settings.storagePort
            val databaseName = settings.storageDatabase
            val username = settings.storageUsername
            val password = settings.storagePassword
            val properties = settings.storageProperties.toMutableMap()
            if (storageType == "mysql") {
                properties.apply {
                    putIfAbsent("cachePrepStmts", "true")
                    putIfAbsent("prepStmtCacheSize", "250")
                    putIfAbsent("prepStmtCacheSqlLimit", "2048")
                    putIfAbsent("useServerPrepStmts", "true")
                    putIfAbsent("useLocalSessionState", "true")
                    putIfAbsent("rewriteBatchedStatements", "true")
                    putIfAbsent("cacheResultSetMetadata", "true")
                    putIfAbsent("cacheServerConfiguration", "true")
                    putIfAbsent("elideSetAutoCommits", "true")
                    putIfAbsent("maintainTimeStats", "true")
                    putIfAbsent("alwaysSendSetIsolation", "false")
                    putIfAbsent("cacheCallableStmts", "true")
                }
            }

            var url = "jdbc:$storageType://$host:$port/$databaseName"
            if (properties.isNotEmpty()) {
                url += "?" + properties.map { "${it.key}=${it.value}" }.joinToString("&")
            }

            val hikariConfig = HikariConfig().apply {
                poolName = "UltimateMobCoins-pool"
                jdbcUrl = url
                driverClassName = if (storageType == "mysql") {
                    "com.mysql.cj.jdbc.Driver"
                } else {
                    "org.mariadb.jdbc.Driver"
                }
                this.username = username
                this.password = password
                this.maximumPoolSize = settings.storagePoolSettingsMaximumPoolSize
                this.minimumIdle = settings.storagePoolSettingsMinimumIdle
                this.maxLifetime = settings.storagePoolSettingsMaximumLifetime
                this.connectionTimeout = settings.storagePoolSettingsConnectionTimeout
                this.initializationFailTimeout = -1
            }

            database = Database.connect(HikariDataSource(hikariConfig))
        }
    }

    fun initialize() {
        connect()
        if (isDatabaseInitialized) {
            transaction {
                SchemaUtils.create(UsersTable)
            }
        }
    }

    fun close() = TransactionManager.closeAndUnregister(database)
}