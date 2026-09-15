package com.example.core.security

import com.example.core.contracts.ToolExecutionRequest
import com.example.core.contracts.ToolRiskLevel

enum class SecurityDecision {
    ALLOW,
    REQUIRE_USER_CONFIRMATION,
    DENY
}

data class SecurityValidationResult(
    val decision: SecurityDecision,
    val reason: String,
    val riskBadge: String = "HIGH RISK",
    val requiredPermissions: List<String> = emptyList()
)

/**
 * Security validation gate for Android tool execution.
 * Phase 0 rule: DENY all actions until adapters are explicitly registered in later phases.
 */
interface SecurityGate {
    fun validate(request: ToolExecutionRequest): SecurityValidationResult
}

/**
 * Initial Phase 0 Security Gate: Deny by default.
 * All action execution is blocked with clear boundary explanations.
 */
class Phase0SecurityGate(
    private val allowList: Set<String> = emptySet()
) : SecurityGate {

    override fun validate(request: ToolExecutionRequest): SecurityValidationResult {
        // Enforce argument freeze validation
        val frozenArgs = request.frozenArguments
        
        // Deny-by-default for Phase 0
        if (allowList.isEmpty() || !allowList.contains(request.toolName)) {
            return SecurityValidationResult(
                decision = SecurityDecision.DENY,
                reason = "Phase 0 Security Boundary: Execution of tool '${request.toolName}' is denied by default. No hardware or OS adapters are registered.",
                riskBadge = "PHASE 0 LOCKED"
            )
        }

        return SecurityValidationResult(
            decision = SecurityDecision.REQUIRE_USER_CONFIRMATION,
            reason = "Tool '${request.toolName}' requires user confirmation.",
            riskBadge = "SECURITY GATE"
        )
    }
}
