package com.example.core.model

enum class TriggerType {
    TIME_SCHEDULE,
    LOCATION_GEOFENCE,
    BATTERY_THRESHOLD,
    HEADPHONES_CONNECTED,
    VOICE_PHRASE
}

data class TriggerCondition(
    val type: TriggerType,
    val parameters: Map<String, String>
)

data class ActionSpec(
    val toolName: String,
    val arguments: Map<String, Any?>,
    val description: String
)

data class AutomationRule(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val trigger: TriggerCondition,
    val actions: List<ActionSpec>,
    val isEnabled: Boolean = true,
    val runCount: Int = 0,
    val lastTriggeredTime: Long? = null
)
