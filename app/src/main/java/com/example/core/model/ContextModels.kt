package com.example.core.model

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL_OUTPUT
}

data class ContextEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)

data class ConversationContext(
    val sessionId: String = java.util.UUID.randomUUID().toString(),
    val entries: List<ContextEntry> = emptyList(),
    val activeGoal: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
