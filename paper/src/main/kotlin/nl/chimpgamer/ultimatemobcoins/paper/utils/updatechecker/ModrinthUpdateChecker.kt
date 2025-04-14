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

    suspend fun checkUpdate() {
        // Would've liked to use ktor but didn't work with paper dependency loading.
        val httpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder(URI.create("https://api.modrinth.com/v2/project/ultimatemobcoins/version"))
            .GET()
            .build()
        val response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
        val jsonAsString = response.body()
        val updates = gson.fromJson<ArrayList<ProjectVersion>>(jsonAsString, object : TypeToken<ArrayList<ProjectVersion>>() {}.type)
        this.updates.clear()
        this.updates.addAll(updates)
    }

    fun notifyAboutUpdate() {
        val latestRelease = updates.first { it.versionType == "release" }
        val latestVersion = Version(latestRelease.versionNumber)
        val pluginVersion = Version(plugin.version)
        if (pluginVersion >= latestVersion) return
        plugin.logger.info("There is a newer version of UltimateMobCoins available: ${latestRelease.versionNumber}, you're on: ${plugin.version}")
    }

    fun notifyPlayerAboutUpdate(player: Player) {
        if (!plugin.settingsConfig.updateNotifyOnJoin) return
        val latestRelease = updates.first { it.versionType == "release" }
        val latestVersion = Version(latestRelease.versionNumber)
        val pluginVersion = Version(plugin.version)
        if (pluginVersion >= latestVersion) return
        player.sendRichMessage("<gold>There is a newer version of UltimateMobCoins available: <yellow>${latestRelease.versionNumber}<gold, you're on: <yellow>${plugin.version}")
    }
}