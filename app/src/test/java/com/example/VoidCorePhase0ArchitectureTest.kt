package com.example

import com.example.core.audit.AuditRecord
import com.example.core.audit.AuditStage
import com.example.core.audit.DefaultAuditLogger
import com.example.core.contracts.DefaultAndroidToolRegistry
import com.example.core.contracts.DefaultWorkflowPlanner
import com.example.core.contracts.GenerationRequest
import com.example.core.contracts.LocalInferenceRequest
import com.example.core.contracts.Phase0LocalInferenceEngine
import com.example.core.contracts.PlanStep
import com.example.core.contracts.StepStatus
import com.example.core.contracts.StubTextGenerationProvider
import com.example.core.contracts.ToolContract
import com.example.core.contracts.ToolExecutionRequest
import com.example.core.contracts.ToolExecutionResult
import com.example.core.contracts.ToolRiskLevel
import com.example.core.executor.DefaultGatedExecutor
import com.example.core.model.VoicePresets
import com.example.core.security.Phase0SecurityGate
import com.example.core.security.SecurityDecision
import com.example.core.state.AssistantState
import com.example.core.state.DefaultAssistantStateEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoidCorePhase0ArchitectureTest {

    // Test 1: One canonical semantic state source
    @Test
    fun testOneCanonicalStateSource() = runTest {
        val stateEngine = DefaultAssistantStateEngine()
        assertEquals(AssistantState.SLEEPING, stateEngine.currentState.value)

        val states = listOf(
            AssistantState.INVOKING,
            AssistantState.LISTENING,
            AssistantState.THINKING,
            AssistantState.PLANNING,
            AssistantState.ACTING,
            AssistantState.CONFIRMATION_REQUIRED,
            AssistantState.SPEAKING,
            AssistantState.SUCCESS,
            AssistantState.ERROR_RECOVERY,
            AssistantState.DISMISSING,
            AssistantState.SLEEPING
        )

        for (state in states) {
            stateEngine.transitionTo(state)
            assertEquals(state, stateEngine.currentState.value)
        }

        assertEquals("Exactly 11 canonical states supported", 11, AssistantState.values().size)
    }

    // Test 2: AI cannot directly execute tools without GatedExecutor
    @Test
    fun testAiCannotDirectlyExecuteTools() = runTest {
        val auditLogger = DefaultAuditLogger()
        val securityGate = Phase0SecurityGate(allowList = emptySet())
        val toolRegistry = DefaultAndroidToolRegistry()
        val executor = DefaultGatedExecutor(securityGate, auditLogger, toolRegistry)

        val request = ToolExecutionRequest(
            toolName = "system_flashlight",
            arguments = mapOf("state" to "ON")
        )

        // Attempting execution via GatedExecutor with zero-registered adapters yields safe denied result
        val result = executor.executeGated(request)
        assertFalse("Tool execution must not succeed directly without authorization", result.success)
        assertNotNull(result.errorMessage)
    }

    // Test 3: Arguments are frozen before validation
    @Test
    fun testArgumentsAreFrozenBeforeValidation() {
        val mutableArgs = mutableMapOf<String, Any?>("intensity" to 0.8, "durationMs" to 500)
        val request = ToolExecutionRequest(
            toolName = "haptic_pulse",
            arguments = mutableArgs
        )

        // Mutating the original map does not change the frozenArguments map inside ToolExecutionRequest
        mutableArgs["intensity"] = 0.1
        assertEquals(0.8, request.frozenArguments["intensity"])

        // Attempting to mutate frozenArguments directly throws UnsupportedOperationException
        try {
            (request.frozenArguments as MutableMap<String, Any?>)["intensity"] = 0.5
            fail("Modifying frozenArguments directly must throw UnsupportedOperationException")
        } catch (e: UnsupportedOperationException) {
            // Success: arguments are strictly immutable/frozen
            assertTrue(true)
        }
    }

    // Test 4: Audit happens BEFORE dispatch
    @Test
    fun testAuditHappensBeforeDispatch() = runTest {
        val auditLogger = DefaultAuditLogger()
        val securityGate = Phase0SecurityGate(allowList = emptySet())
        val toolRegistry = DefaultAndroidToolRegistry()
        val executor = DefaultGatedExecutor(securityGate, auditLogger, toolRegistry)

        val request = ToolExecutionRequest(
            toolName = "camera_snapshot",
            arguments = mapOf("resolution" to "1080p")
        )

        executor.executeGated(request)

        val auditHistory = auditLogger.getRecordsForRequest(request.requestId)
        assertTrue("Audit log must have entries", auditHistory.isNotEmpty())

        // The very first recorded stage MUST be PRE_DISPATCH
        val firstAuditStage = auditHistory.first().stage
        assertEquals(AuditStage.PRE_DISPATCH, firstAuditStage)
    }

    // Test 5: Deny-by-default security
    @Test
    fun testDenyByDefaultSecurity() = runTest {
        val securityGate = Phase0SecurityGate(allowList = emptySet())

        val toolNamesToTest = listOf(
            "sms_dispatch",
            "telephony_call",
            "flashlight_toggle",
            "network_fetch",
            "arbitrary_code_exec"
        )

        for (tool in toolNamesToTest) {
            val request = ToolExecutionRequest(toolName = tool, arguments = emptyMap())
            val decision = securityGate.validate(request)
            assertEquals("Tool '$tool' must be denied by default in Phase 0", SecurityDecision.DENY, decision.decision)
            assertTrue(decision.reason.contains("Phase 0 Security Boundary"))
        }
    }

    // Test 6: Provider isolation
    @Test
    fun testProviderIsolation() = runTest {
        val stubProvider = StubTextGenerationProvider()
        val response = stubProvider.generateText(GenerationRequest(prompt = "Hello VoidCore")).getOrThrow()
        assertTrue(response.text.contains("Phase 0 Neutral Foundation"))

        val streamChunk = stubProvider.streamText(GenerationRequest(prompt = "Stream test")).first()
        assertTrue(streamChunk.text.contains("Phase 0 Neutral Foundation"))

        val localInference = Phase0LocalInferenceEngine()
        assertFalse(localInference.isModelLoaded())
        val localResponse = localInference.infer(LocalInferenceRequest(prompt = "test")).getOrThrow()
        assertTrue(localResponse.responseText.contains("ready for Phase 1"))
    }

    // Test 7: Workflow checkpoint models
    @Test
    fun testWorkflowCheckpointModels() {
        val planner = DefaultWorkflowPlanner()
        val steps = listOf(
            PlanStep(stepId = "s1", toolName = "query_battery", inputArguments = emptyMap(), description = "Step 1", status = StepStatus.COMPLETED),
            PlanStep(stepId = "s2", toolName = "query_network", inputArguments = emptyMap(), description = "Step 2", status = StepStatus.COMPLETED),
            PlanStep(stepId = "s3", toolName = "synthesize_summary", inputArguments = emptyMap(), description = "Step 3", status = StepStatus.PENDING)
        )

        val checkpoint = planner.createCheckpoint(
            planId = "workflow_001",
            currentStepIndex = 2,
            steps = steps,
            stateSnapshot = mapOf("battery" to "85%", "network" to "WIFI")
        )

        assertEquals("workflow_001", checkpoint.workflowId)
        assertEquals(2, checkpoint.stepIndex)
        assertEquals(2, checkpoint.completedSteps.size)
        assertEquals("85%", checkpoint.stateSnapshot["battery"])

        val restoredPlan = planner.restoreCheckpoint(checkpoint)
        assertEquals("workflow_001", restoredPlan.planId)
        assertEquals(2, restoredPlan.steps.size)
    }

    // Test 8: Six voice presets including synthetic non-human Omega
    @Test
    fun testVoicePresetsAndOmegaIdentity() {
        val presets = VoicePresets.ALL_PRESETS
        assertEquals(6, presets.size)

        val names = presets.map { it.name }
        assertTrue(names.contains("Neutral Core"))
        assertTrue(names.contains("Void"))
        assertTrue(names.contains("Architect"))
        assertTrue(names.contains("Spectral"))
        assertTrue(names.contains("Titan"))
        assertTrue(names.contains("Omega"))

        val omega = VoicePresets.OMEGA
        assertTrue("Omega must be synthetic non-human", omega.isSyntheticNonHuman)
        assertTrue("Omega metallic resonance must be high", omega.metallicResonance >= 0.9f)
        assertTrue("Omega harmonic dissonance must be high", omega.harmonicDissonance >= 0.8f)
    }
}
