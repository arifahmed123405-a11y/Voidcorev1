package com.example.core.model

import com.example.core.contracts.PlanStep

data class WorkflowDefinition(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val templateSteps: List<PlanStep>,
    val requiresExplicitApproval: Boolean = true,
    val isSystemPreset: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class WorkflowInstance(
    val instanceId: String = java.util.UUID.randomUUID().toString(),
    val definitionId: String,
    val title: String,
    val currentStepIndex: Int = 0,
    val steps: List<PlanStep> = emptyList(),
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
