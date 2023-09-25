package nl.chimpgamer.ultimatemobcoins.paper.utils

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit

/**
 * A simple expiring set implementation using Caffeine caches
 *
 * @param <E> element type
 */
class ExpiringSet<E : Any>(duration: Long, unit: TimeUnit) {
    private val cache: Cache<E, Long> = Caffeine.newBuilder().expireAfterWrite(duration, unit).build()
    private val lifetime: Long = unit.toMillis(duration)

    fun add(element: E): Boolean {
        val present: Boolean = contains(element)
        cache.put(element, System.currentTimeMillis() + lifetime)
        return !present
    }

    fun contains(element: E): Boolean {
        val timeout = cache.getIfPresent(element)
        return timeout != null && timeout > System.currentTimeMillis()
    }

    fun remove(element: E) {
        cache.invalidate(element)
    }

    fun removeAll(elements: Collection<E>) {
        cache.invalidateAll(elements)
    }
}
