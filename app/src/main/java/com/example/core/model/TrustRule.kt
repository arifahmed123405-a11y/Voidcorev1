package com.example.core.model

enum class PermissionTier {
    TIER_0_READ_ONLY,
    TIER_1_DEVICE_QUERY,
    TIER_2_SETTINGS_MODIFICATION,
    TIER_3_COMMUNICATION_OUTBOUND,
    TIER_4_FINANCIAL_CRITICAL
}

enum class TrustPolicyLevel {
    STRICT_CONFIRM_ALL,
    BALANCED_HIGH_RISK_ONLY,
    DEVELOPER_ALLOW_REGISTERED
}

data class TrustRule(
    val id: String = java.util.UUID.randomUUID().toString(),
    val targetToolPattern: String,
    val requiredTier: PermissionTier,
    val policyLevel: TrustPolicyLevel = TrustPolicyLevel.STRICT_CONFIRM_ALL,
    val isEnabled: Boolean = true,
    val description: String = ""
)

data class CommandAlias(
    val id: String = java.util.UUID.randomUUID().toString(),
    val triggerPhrase: String,
    val mappedIntent: String,
    val isSystemPreset: Boolean = false
)
