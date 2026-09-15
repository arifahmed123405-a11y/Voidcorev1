package com.example.core.contracts

enum class HealthStatus {
    HEALTHY,
    DEGRADED,
    UNAVAILABLE,
    MAINTENANCE
}

data class ProviderStatus(
    val providerId: String,
    val isAvailable: Boolean,
    val status: HealthStatus,
    val latencyMs: Long = 0L,
    val errorRate: Float = 0.0f,
    val lastCheckedTimestamp: Long = System.currentTimeMillis()
)

enum class FailoverStrategy {
    LOCAL_FIRST,
    CLOUD_FIRST,
    BALANCED_ROUND_ROBIN,
    MANUAL_ONLY
}

data class FailoverPolicy(
    val primaryProviderId: String,
    val fallbackProviderIds: List<String>,
    val strategy: FailoverStrategy = FailoverStrategy.LOCAL_FIRST,
    val maxRetriesBeforeFailover: Int = 2
)

/**
 * Contract for monitoring provider health and managing seamless failover.
 */
interface ProviderHealthMonitor {
    suspend fun checkHealth(providerId: String): ProviderStatus
    fun selectActiveProvider(available: List<String>, policy: FailoverPolicy): String?
    fun reportError(providerId: String, error: Throwable)
    fun reportSuccess(providerId: String, latencyMs: Long)
}

class DefaultProviderHealthMonitor : ProviderHealthMonitor {
    private val healthMap = java.util.concurrent.ConcurrentHashMap<String, ProviderStatus>()

    override suspend fun checkHealth(providerId: String): ProviderStatus {
        return healthMap.getOrPut(providerId) {
            ProviderStatus(
                providerId = providerId,
                isAvailable = true,
                status = HealthStatus.HEALTHY,
                latencyMs = 12L
            )
        }
    }

    override fun selectActiveProvider(available: List<String>, policy: FailoverPolicy): String? {
        if (available.contains(policy.primaryProviderId)) {
            val primaryStatus = healthMap[policy.primaryProviderId]
            if (primaryStatus == null || primaryStatus.status != HealthStatus.UNAVAILABLE) {
                return policy.primaryProviderId
            }
        }
        return policy.fallbackProviderIds.firstOrNull { available.contains(it) }
    }

    override fun reportError(providerId: String, error: Throwable) {
        val current = healthMap[providerId] ?: ProviderStatus(providerId, true, HealthStatus.HEALTHY)
        healthMap[providerId] = current.copy(
            isAvailable = false,
            status = HealthStatus.DEGRADED,
            errorRate = (current.errorRate + 0.2f).coerceAtMost(1.0f)
        )
    }

    override fun reportSuccess(providerId: String, latencyMs: Long) {
        val current = healthMap[providerId] ?: ProviderStatus(providerId, true, HealthStatus.HEALTHY)
        healthMap[providerId] = current.copy(
            isAvailable = true,
            status = HealthStatus.HEALTHY,
            latencyMs = latencyMs,
            errorRate = (current.errorRate * 0.8f)
        )
    }
}
