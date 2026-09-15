package com.example.core.contracts

enum class ToolRiskLevel {
    READ_ONLY,
    LOW_RISK,
    MEDIUM_RISK,
    HIGH_RISK,
    CRITICAL
}

data class ToolParameter(
    val name: String,
    val type: String,
    val description: String,
    val isRequired: Boolean = true,
    val defaultValue: Any? = null
)

/**
 * Immutable request to execute a tool.
 * Arguments are frozen into an unmodifiable map at construction time.
 */
data class ToolExecutionRequest(
    val toolName: String,
    val arguments: Map<String, Any?>,
    val requestId: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val initiatedBy: String = "AI_AGENT"
) {
    // Ensure arguments map is defensively copied and unmodifiable
    val frozenArguments: Map<String, Any?> = java.util.Collections.unmodifiableMap(HashMap(arguments))
}

data class ToolExecutionResult(
    val requestId: String,
    val toolName: String,
    val success: Boolean,
    val outputData: Map<String, Any?> = emptyMap(),
    val errorMessage: String? = null,
    val executionTimeMs: Long = 0L
)

/**
 * Provider-neutral contract for Android tools.
 */
interface ToolContract {
    val name: String
    val description: String
    val riskLevel: ToolRiskLevel
    val requiredPermissions: List<String>
    val parameters: List<ToolParameter>

    suspend fun execute(request: ToolExecutionRequest): ToolExecutionResult
}

/**
 * Tool registry holding available system tools.
 */
interface AndroidToolRegistry {
    fun registerTool(tool: ToolContract)
    fun unregisterTool(toolName: String)
    fun getTool(toolName: String): ToolContract?
    fun getAllTools(): List<ToolContract>
    fun hasTool(toolName: String): Boolean
}

class DefaultAndroidToolRegistry : AndroidToolRegistry {
    private val tools = java.util.concurrent.ConcurrentHashMap<String, ToolContract>()

    override fun registerTool(tool: ToolContract) {
        tools[tool.name] = tool
    }

    override fun unregisterTool(toolName: String) {
        tools.remove(toolName)
    }

    override fun getTool(toolName: String): ToolContract? = tools[toolName]

    override fun getAllTools(): List<ToolContract> = tools.values.toList()

    override fun hasTool(toolName: String): Boolean = tools.containsKey(toolName)
}
