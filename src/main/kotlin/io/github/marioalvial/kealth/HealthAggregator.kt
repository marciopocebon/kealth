package io.github.marioalvial.kealth

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * An aggregator of health components.
 *
 * This class holds a list of health components.
 *
 * @property components List<HealthComponent> - list of health components.
 * @constructor Creates a health aggregator for the component list passed as constructor parameter.
 */
class HealthAggregator(
    private val components: List<HealthComponent>
) {

    /**
     * Execute the health method of each health component and builds a map with component name and health status.
     * @return Map<String, HealthStatus>
     */
    suspend fun aggregate(): Map<String, HealthStatus> = coroutineScope {
        components
            .associate { it.name to async { it.health() } }
            .mapValues { it.value.await() }
    }
}
