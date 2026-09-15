package com.example.core.audit

enum class AuditStage {
    PROPOSAL_RECEIVED,
    SECURITY_EVALUATION,
    PRE_DISPATCH,
    EXECUTION_ATTEMPT,
    EXECUTION_SUCCESS,
    EXECUTION_FAILED,
    SECURITY_DENIED,
    USER_CANCELLED
}

data class AuditRecord(
    val recordId: String = java.util.UUID.randomUUID().toString(),
    val requestId: String,
    val toolName: String,
    val frozenArguments: Map<String, Any?>,
    val stage: AuditStage,
    val detailMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Immutable audit contract.
 * Guarantees that audit recording happens before dispatch.
 */
interface AuditLogger {
    fun record(record: AuditRecord)
    fun getHistory(): List<AuditRecord>
    fun getRecordsForRequest(requestId: String): List<AuditRecord>
    fun clearHistory()
}

class DefaultAuditLogger : AuditLogger {
    private val records = java.util.concurrent.CopyOnWriteArrayList<AuditRecord>()

    override fun record(record: AuditRecord) {
        records.add(record)
    }

    override fun getHistory(): List<AuditRecord> = records.toList()

    override fun getRecordsForRequest(requestId: String): List<AuditRecord> =
        records.filter { it.requestId == requestId }

    override fun clearHistory() {
        records.clear()
    }
}
