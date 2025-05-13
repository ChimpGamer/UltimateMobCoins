package nl.chimpgamer.ultimatemobcoins.paper.utils

import com.github.benmanes.caffeine.cache.Caffeine
import java.util.Collections
import java.util.concurrent.TimeUnit

object ExpiringSet {

    /**
     * A simple expiring set implementation using Caffeine caches
     *
     * @param <E> element type
     * @return a new expiring set
     */
    fun <E> newExpiringSet(duration: Long, unit: TimeUnit): MutableSet<E> {
        return Collections.newSetFromMap(Caffeine.newBuilder().expireAfterAccess(duration, unit).build<E, Boolean>().asMap())
    }
}