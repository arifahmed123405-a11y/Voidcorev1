package com.example.core.model

enum class ActivityStatus {
    PROPOSED,
    AWAITING_APPROVAL,
    APPROVED,
    EXECUTING,
    SUCCESS,
    FAILED,
    BLOCKED_BY_POLICY,
    DENIED_BY_USER
}

data class ActivityLogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String,
    val target: String,
    val parameters: Map<String, Any?> = emptyMap(),
    val status: ActivityStatus,
    val securityLevel: String = "TIER_0",
    val latencyMs: Long = 0L,
    val auditHash: String = ""
)
