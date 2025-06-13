package nl.chimpgamer.ultimatemobcoins.paper.utils.updatechecker

import com.google.gson.Gson
import io.github.g00fy2.versioncompare.Version
import io.leangen.geantyref.TypeToken
import kotlinx.coroutines.future.await
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import org.bukkit.entity.Player
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ModrinthUpdateChecker(private val plugin: UltimateMobCoinsPlugin) {
    private val updates = ArrayList<ProjectVersion>()
    private val gson = Gson()

    companion object {
        private const val API_URL = "https://api.modrinth.com/v2/project/ultimatemobcoins/version"
        private const val UPDATE_MESSAGE = "There is a newer version of UltimateMobCoins available: %s, you're on: %s"
        private const val PLAYER_UPDATE_MESSAGE = "<gold>There is a newer version of UltimateMobCoins available: <yellow>%s<gold>, you're on: <yellow>%s"
        private const val RELEASE_VERSION_TYPE = "release"
    }

    suspend fun checkUpdate() {
        // Would've liked to use ktor but didn't work with paper dependency loading.
        val httpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder(URI.create(API_URL))
            .GET()
            .build()
        val response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
        val jsonAsString = response.body()
        val updates = gson.fromJson<ArrayList<ProjectVersion>>(
            jsonAsString,
            object : TypeToken<ArrayList<ProjectVersion>>() {}.type
        )
        this.updates.clear()
        this.updates.addAll(updates)
    }

    fun notifyAboutUpdate() {
        val latestRelease = updates.first { it.versionType == RELEASE_VERSION_TYPE }
        val latestVersion = Version(latestRelease.versionNumber)
        val pluginVersion = Version(plugin.version)
        if (pluginVersion >= latestVersion) return
        plugin.logger.info(UPDATE_MESSAGE.format(latestRelease.versionNumber, plugin.version))
    }

    fun notifyPlayerAboutUpdate(player: Player) {
        if (!plugin.settingsConfig.updateNotifyOnJoin) return
        if (!player.hasPermission("ultimatemobcoins.update-check")) return
        val latestRelease = updates.first { it.versionType == RELEASE_VERSION_TYPE }
        val latestVersion = Version(latestRelease.versionNumber)
        val pluginVersion = Version(plugin.version)
        if (pluginVersion >= latestVersion) return
        player.sendRichMessage(PLAYER_UPDATE_MESSAGE.format(latestRelease.versionNumber, plugin.version))
    }
}