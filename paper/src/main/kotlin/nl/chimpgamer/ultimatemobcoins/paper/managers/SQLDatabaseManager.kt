package nl.chimpgamer.ultimatemobcoins.paper.managers

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.storage.dao.UserDao
import nl.chimpgamer.ultimatemobcoins.paper.storage.dao.sql.SQLUserDao
import nl.chimpgamer.ultimatemobcoins.paper.storage.sql.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

class SQLDatabaseManager(private val plugin: UltimateMobCoinsPlugin) : DatabaseManager(plugin) {
    private lateinit var database: Database

    override val userDao = SQLUserDao(plugin)

    val isDatabaseInitialized: Boolean get() = this::database.isInitialized

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

    override fun initialize() {
        connect()
        if (isDatabaseInitialized) {
            transaction {
                SchemaUtils.create(UsersTable)

                if (plugin.settingsConfig.storageType.lowercase() != "sqlite") {
                    // Workaround for Exposed bug https://youtrack.jetbrains.com/issue/EXPOSED-467/Decimal-type-precision-and-scale-not-checked-by-SchemaUtils
                    // Not supported on SQLite
                    exec(UsersTable.coins.modifyStatement().single())
                    exec(UsersTable.coinsCollected.modifyStatement().single())
                    exec(UsersTable.coinsSpent.modifyStatement().single())
                }
            }
        }
    }

    override fun close() = TransactionManager.closeAndUnregister(database)

    override suspend fun databaseNameAndVersion() = database.name + " " + database.version
}