package com.example.core.model

enum class MemoryType {
    USER_PREFERENCE,
    FACTUAL_KNOWLEDGE,
    ROUTINE_HABIT,
    CONTACT_RELATION,
    SYSTEM_NOTE
}

data class MemoryProvenance(
    val sourceSessionId: String,
    val sourceUtterance: String,
    val confidenceScore: Float = 1.0f,
    val verifiedByUser: Boolean = false,
    val extractionTimestamp: Long = System.currentTimeMillis()
)

data class MemoryEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val key: String,
    val value: String,
    val type: MemoryType = MemoryType.USER_PREFERENCE,
    val provenance: MemoryProvenance,
    val lastAccessedTimestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
