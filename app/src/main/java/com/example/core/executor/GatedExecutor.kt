package com.example.core.executor

import com.example.core.audit.AuditLogger
import com.example.core.audit.AuditRecord
import com.example.core.audit.AuditStage
import com.example.core.contracts.AndroidToolRegistry
import com.example.core.contracts.ToolExecutionRequest
import com.example.core.contracts.ToolExecutionResult
import com.example.core.security.SecurityDecision
import com.example.core.security.SecurityGate

/**
 * Gated Executor responsible for mediating all tool requests.
 * Direct tool execution is prohibited; all requests must pass through
 * argument freezing, pre-dispatch auditing, and the SecurityGate.
 */
interface GatedExecutor {
    suspend fun executeGated(request: ToolExecutionRequest): ToolExecutionResult
}

class DefaultGatedExecutor(
    private val securityGate: SecurityGate,
    private val auditLogger: AuditLogger,
    private val toolRegistry: AndroidToolRegistry
) : GatedExecutor {

    override suspend fun executeGated(request: ToolExecutionRequest): ToolExecutionResult {
        val startTime = System.currentTimeMillis()

        // 1. Ensure arguments are immutable/frozen
        val frozenArgs = request.frozenArguments

        // 2. Mandatory Pre-Dispatch Audit Record (Auditing happens BEFORE dispatch)
        auditLogger.record(
            AuditRecord(
                requestId = request.requestId,
                toolName = request.toolName,
                frozenArguments = frozenArgs,
                stage = AuditStage.PRE_DISPATCH,
                detailMessage = "Evaluating security policy for tool execution request"
            )
        )

        // 3. Security Gate Validation
        val validation = securityGate.validate(request)

        when (validation.decision) {
            SecurityDecision.DENY -> {
                auditLogger.record(
                    AuditRecord(
                        requestId = request.requestId,
                        toolName = request.toolName,
                        frozenArguments = frozenArgs,
                        stage = AuditStage.SECURITY_DENIED,
                        detailMessage = "Denied by security gate: ${validation.reason}"
                    )
                )
                return ToolExecutionResult(
                    requestId = request.requestId,
                    toolName = request.toolName,
                    success = false,
                    errorMessage = validation.reason,
                    executionTimeMs = System.currentTimeMillis() - startTime
                )
            }

            SecurityDecision.REQUIRE_USER_CONFIRMATION -> {
                auditLogger.record(
                    AuditRecord(
                        requestId = request.requestId,
                        toolName = request.toolName,
                        frozenArguments = frozenArgs,
                        stage = AuditStage.SECURITY_EVALUATION,
                        detailMessage = "Blocked awaiting user confirmation: ${validation.reason}"
                    )
                )
                return ToolExecutionResult(
                    requestId = request.requestId,
                    toolName = request.toolName,
                    success = false,
                    errorMessage = "Action requires user confirmation in Phase 0. Automated execution halted.",
                    executionTimeMs = System.currentTimeMillis() - startTime
                )
            }

            SecurityDecision.ALLOW -> {
                val tool = toolRegistry.getTool(request.toolName)
                if (tool == null) {
                    auditLogger.record(
                        AuditRecord(
                            requestId = request.requestId,
                            toolName = request.toolName,
                            frozenArguments = frozenArgs,
                            stage = AuditStage.EXECUTION_FAILED,
                            detailMessage = "No tool adapter registered for '${request.toolName}' in Phase 0 foundation."
                        )
                    )
                    return ToolExecutionResult(
                        requestId = request.requestId,
                        toolName = request.toolName,
                        success = false,
                        errorMessage = "Tool adapter '${request.toolName}' is not registered in Phase 0.",
                        executionTimeMs = System.currentTimeMillis() - startTime
                    )
                }

                auditLogger.record(
                    AuditRecord(
                        requestId = request.requestId,
                        toolName = request.toolName,
                        frozenArguments = frozenArgs,
                        stage = AuditStage.EXECUTION_ATTEMPT,
                        detailMessage = "Dispatching execution to registered tool adapter"
                    )
                )

                return try {
                    val result = tool.execute(request)
                    auditLogger.record(
                        AuditRecord(
                            requestId = request.requestId,
                            toolName = request.toolName,
                            frozenArguments = frozenArgs,
                            stage = if (result.success) AuditStage.EXECUTION_SUCCESS else AuditStage.EXECUTION_FAILED,
                            detailMessage = if (result.success) "Execution completed" else "Execution failed: ${result.errorMessage}"
                        )
                    )
                    result
                } catch (e: Exception) {
                    auditLogger.record(
                        AuditRecord(
                            requestId = request.requestId,
                            toolName = request.toolName,
                            frozenArguments = frozenArgs,
                            stage = AuditStage.EXECUTION_FAILED,
                            detailMessage = "Unhandled exception during tool execution: ${e.message}"
                        )
                    )
                    ToolExecutionResult(
                        requestId = request.requestId,
                        toolName = request.toolName,
                        success = false,
                        errorMessage = e.message ?: "Unknown execution error",
                        executionTimeMs = System.currentTimeMillis() - startTime
                    )
                }
            }
        }
    }
}
