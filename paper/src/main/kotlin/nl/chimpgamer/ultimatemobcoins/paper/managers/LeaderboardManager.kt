package nl.chimpgamer.ultimatemobcoins.paper.managers

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.models.User
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes

interface Leaderboard {
    suspend fun refresh()
    fun getPosition(position: Int): User?
}

class LeaderboardManager(private val plugin: UltimateMobCoinsPlugin) {
    companion object {
        private const val REFRESH_INTERVAL = 15L
        private const val MIN_POSITION = 1
    }

    private val mobCoinsLeaderboard = MobCoinsLeaderboard()
    private val mobCoinsGrindLeaderboard = MobCoinsGrindLeaderboard()
    private lateinit var updateJob: Job

    fun start() {
        stop()
        updateJob = plugin.launch(plugin.asyncDispatcher, CoroutineStart.UNDISPATCHED) {
            while (true) {
                delay(REFRESH_INTERVAL.minutes)
                mobCoinsLeaderboard.refresh()
                mobCoinsGrindLeaderboard.refresh()
            }
        }
    }

    fun stop() {
        if (::updateJob.isInitialized && updateJob.isActive) {
            updateJob.cancel()
        }
    }

    fun getTopMobCoinsPosition(position: Int): User? = 
        mobCoinsLeaderboard.getPosition(position)

    fun getTopMobCoinsGrindPosition(position: Int): User? =
        mobCoinsGrindLeaderboard.getPosition(position)

    private inner class MobCoinsLeaderboard : Leaderboard {
        private val cache = ConcurrentHashMap<Int, User>()

        override suspend fun refresh() {
            val size = cache.keys.maxOrNull() ?: return
            plugin.userManager.getTopMobCoins()
                .take(size)
                .withIndex()
                .forEach { (index, user) -> 
                    cache[index + 1] = user 
                }
        }

        override fun getPosition(position: Int): User? {
            if (position < MIN_POSITION) return null
            return cache[position] ?: requestPositionAsync(position)
        }

        private fun requestPositionAsync(position: Int): User? {
            plugin.launch(plugin.asyncDispatcher, CoroutineStart.UNDISPATCHED) {
                val topUsers = plugin.userManager.getTopMobCoins()
                if (topUsers.size >= position) {
                    cache[position] = topUsers[position - 1]
                }
            }
            return null
        }
    }

    private inner class MobCoinsGrindLeaderboard : Leaderboard {
        private val cache = ConcurrentHashMap<Int, User>()

        override suspend fun refresh() {
            val size = cache.keys.maxOrNull() ?: return
            plugin.userManager.getGrindTop()
                .take(size)
                .withIndex()
                .forEach { (index, user) -> 
                    cache[index + 1] = user 
                }
        }

        override fun getPosition(position: Int): User? {
            if (position < MIN_POSITION) return null
            return cache[position] ?: requestPositionAsync(position)
        }

        private fun requestPositionAsync(position: Int): User? {
            plugin.launch(plugin.asyncDispatcher, CoroutineStart.UNDISPATCHED) {
                val topUsers = plugin.userManager.getGrindTop()
                if (topUsers.size >= position) {
                    cache[position] = topUsers[position - 1]
                }
            }
            return null
        }
    }
}