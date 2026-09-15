package com.example.core.contracts

enum class StepStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    SKIPPED
}

data class PlanStep(
    val stepId: String,
    val toolName: String,
    val inputArguments: Map<String, Any?>,
    val description: String,
    val status: StepStatus = StepStatus.PENDING,
    val outputResult: Any? = null
)

data class ExecutionPlan(
    val planId: String,
    val userIntent: String,
    val steps: List<PlanStep>,
    val requiresUserApproval: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class WorkflowCheckpoint(
    val checkpointId: String,
    val workflowId: String,
    val stepIndex: Int,
    val completedSteps: List<PlanStep>,
    val stateSnapshot: Map<String, Any?>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Provider-neutral contract for multi-step agent workflow planning.
 */
interface WorkflowPlanner {
    suspend fun planWorkflow(intent: String, context: Map<String, Any?> = emptyMap()): Result<ExecutionPlan>
    fun createCheckpoint(planId: String, currentStepIndex: Int, steps: List<PlanStep>, stateSnapshot: Map<String, Any?>): WorkflowCheckpoint
    fun restoreCheckpoint(checkpoint: WorkflowCheckpoint): ExecutionPlan
}

class DefaultWorkflowPlanner : WorkflowPlanner {
    override suspend fun planWorkflow(intent: String, context: Map<String, Any?>): Result<ExecutionPlan> {
        return Result.success(
            ExecutionPlan(
                planId = java.util.UUID.randomUUID().toString(),
                userIntent = intent,
                steps = emptyList(),
                requiresUserApproval = false
            )
        )
    }

    override fun createCheckpoint(
        planId: String,
        currentStepIndex: Int,
        steps: List<PlanStep>,
        stateSnapshot: Map<String, Any?>
    ): WorkflowCheckpoint {
        return WorkflowCheckpoint(
            checkpointId = "chk_${java.util.UUID.randomUUID()}",
            workflowId = planId,
            stepIndex = currentStepIndex,
            completedSteps = steps.filter { it.status == StepStatus.COMPLETED },
            stateSnapshot = stateSnapshot
        )
    }

    override fun restoreCheckpoint(checkpoint: WorkflowCheckpoint): ExecutionPlan {
        return ExecutionPlan(
            planId = checkpoint.workflowId,
            userIntent = "Restored from checkpoint ${checkpoint.checkpointId}",
            steps = checkpoint.completedSteps,
            requiresUserApproval = false
        )
    }
}
