package nl.chimpgamer.ultimatemobcoins.paper.utils

import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit

object ExpiringMap {

    /**
     * A simple expiring map implementation using Caffeine caches
     *
     * @param <K, V> element types
     * @return a new expiring map
     */
    fun <K, V> newExpiringMap(duration: Long, unit: TimeUnit): MutableMap<K, V> {
        return Caffeine.newBuilder().expireAfterAccess(duration, unit).build<K, V>().asMap()
    }
}
