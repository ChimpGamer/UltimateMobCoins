package nl.chimpgamer.ultimatemobcoins.paper.managers

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.storage.dao.mongodb.MongoDBUserDao
import nl.chimpgamer.ultimatemobcoins.paper.storage.mongodb.MongoUser
import org.bson.Document
import org.bson.UuidRepresentation

class MongoDBManager(private val plugin: UltimateMobCoinsPlugin) : DatabaseManager(plugin) {
    private lateinit var mongoClient: MongoClient
    private lateinit var database: MongoDatabase

    override val userDao = MongoDBUserDao(plugin)

    private fun connect() {
        val settings = plugin.settingsConfig
        val host = settings.storageHost
        val port = settings.storagePort
        val databaseName = settings.storageDatabase
        val username = settings.storageUsername
        val password = settings.storagePassword
        val uri = settings.storageMongoDBConnectionUri
            .takeUnless { it.isEmpty() }
            ?: "mongodb://$username:$password@$host:$port"

        val clientSettings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(uri))
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .build()
        mongoClient = MongoClient.create(clientSettings)
        database = mongoClient.getDatabase(databaseName)
    }

    override fun initialize() {
        connect()
    }

    fun usersCollection() = database.getCollection<MongoUser>(plugin.settingsConfig.storageMongoDBCollectionPrefix + "users")

    override fun close() {
        if (this::mongoClient.isInitialized) {
            mongoClient.close()
        }
    }

    override suspend fun databaseNameAndVersion() = "MongoDB " + database.runCommand(Document("buildinfo", 1)).getString("version")
}