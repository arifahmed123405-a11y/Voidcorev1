package com.example.agent

import android.content.Context
import com.example.core.context.ConversationContextManager
import com.example.core.database.VoidCoreRepository
import com.example.core.provider.AIProviderRegistry
import com.example.core.state.AssistantState
import com.example.core.state.AssistantStateMachine
import com.example.engine.local.LocalModelManager
import com.example.engine.local.LocalModelRuntime
import com.example.engine.local.TaskRouteResult
import com.example.engine.local.TaskRouter
import com.example.engine.security.RiskLevel
import com.example.engine.security.SecurityDecision
import com.example.engine.security.SecurityGate
import com.example.engine.security.StructuredIntent
import com.example.engine.tools.AndroidToolAdapter
import com.example.engine.tools.ToolExecutionOutcome
import com.example.engine.voice.AudioCue
import com.example.engine.voice.AudioIdentityEngine
import com.example.engine.voice.SpeechRecognitionManager
import com.example.engine.voice.VoiceEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main intelligence orchestration brain of VoidCore.
 * Seamlessly integrates:
 * 1. TaskRouter & DeterministicCommandParser (0ms, 0 tokens)
 * 2. LocalModelRuntime (Embedded on-device streaming neural model & strict whitelist JSON intent resolver)
 * 3. LocalModelManager (Profiles, download progress, checksums, space checks)
 * 4. AIProviderRegistry (Optional cloud fallback for complex queries only)
 * 5. SecurityGate (Mandatory fine-grained permission & confirmation enforcement)
 */
class AgentBrain(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val stateMachine = AssistantStateMachine.getInstance()
    val modelManager = LocalModelManager.getInstance(context)
    val localRuntime = LocalModelRuntime(modelManager.selectedProfile.value)
    private val taskRouter = TaskRouter(localRuntime = localRuntime)
    private val securityGate = SecurityGate()
    private val providerRegistry = AIProviderRegistry.getInstance(context)
    private val contextManager = ConversationContextManager(maxTurns = 12)
    private val toolAdapter = AndroidToolAdapter(context)
    private val repository = VoidCoreRepository.getInstance(context)
    private val voiceEngine = VoiceEngine.getInstance(context)
    private val speechRecognizer = SpeechRecognitionManager.getInstance(context)
    private val audioEngine = AudioIdentityEngine.getInstance(context)

    init {
        speechRecognizer.onFinalResult = { recognizedText ->
            handleUserInput(recognizedText, isSpoken = true)
        }
    }

    /**
     * Handles tapping the microphone button.
     * If speaking, immediately interrupts speech and returns to listening.
     * If listening, stops listening.
     * If idle/sleeping, starts listening.
     */
    fun handleMicTap() {
        if (voiceEngine.isSpeaking.value || stateMachine.currentState.value == AssistantState.SPEAKING) {
            voiceEngine.interruptAndListen(speechRecognizer)
        } else if (speechRecognizer.isListening.value || stateMachine.currentState.value == AssistantState.LISTENING) {
            speechRecognizer.stopListening()
            stateMachine.transitionTo(AssistantState.SLEEPING)
        } else {
            speechRecognizer.startListening()
        }
    }

    fun handleUserInput(rawInput: String, isSpoken: Boolean = false) {
        if (rawInput.isBlank()) return

        val sanitized = securityGate.sanitizePromptInput(rawInput)
        stateMachine.updateTranscript(sanitized)
        contextManager.recordTurn("user", sanitized)

        scope.launch {
            // Save user message in conversation history database
            repository.saveMessage("user", sanitized)

            // Step 1: 4-Tier Task Router Classification
            val routeResult = taskRouter.route(sanitized)

            when (routeResult) {
                // Tier 1: Obvious Android command -> deterministic parser -> tool (0ms, 0 tokens)
                is TaskRouteResult.DeterministicAction -> {
                    processStructuredIntent(routeResult.intent, sanitized, isLocalAction = true)
                }

                // Tier 2: Ambiguous action phrase -> local model -> strict whitelist JSON intent -> security gate -> tool
                is TaskRouteResult.AmbiguousActionCandidate -> {
                    stateMachine.transitionTo(AssistantState.THINKING)
                    audioEngine.playCue(AudioCue.THINKING)
                    val resolvedIntent = localRuntime.resolveAmbiguousIntent(
                        rawPrompt = sanitized,
                        contextHistory = contextManager.getFormattedHistory()
                    )
                    processStructuredIntent(resolvedIntent, sanitized, isLocalAction = true)
                }

                // Tier 3: Normal private conversation -> local model (100% on-device, offline)
                is TaskRouteResult.LocalConversational -> {
                    stateMachine.transitionTo(AssistantState.THINKING)
                    audioEngine.playCue(AudioCue.THINKING)
                    val localResponseText = localRuntime.generate(
                        rawPrompt = sanitized,
                        contextHistory = contextManager.getFormattedHistory(),
                        timeoutMs = 10000L
                    )
                    contextManager.recordTurn("assistant", localResponseText)
                    val structuredIntent = StructuredIntent.ConversationalResponse(localResponseText)
                    processStructuredIntent(structuredIntent, sanitized, isLocalAction = false)
                }

                // Tier 4: Complex/fresh query -> optional free cloud fallback when user enables it
                is TaskRouteResult.ComplexCloudQuery -> {
                    stateMachine.transitionTo(AssistantState.THINKING)
                    audioEngine.playCue(AudioCue.THINKING)
                    val response = providerRegistry.generateWithFailover(
                        prompt = sanitized,
                        contextHistory = contextManager.getFormattedHistory()
                    )
                    // CRITICAL: Remote providers NEVER generate executable tool requests directly.
                    // They only supply conversational response text.
                    contextManager.recordTurn("assistant", response.text)
                    val structuredIntent = StructuredIntent.ConversationalResponse(response.text)
                    processStructuredIntent(structuredIntent, sanitized, isLocalAction = false)
                }
            }
        }
    }

    private suspend fun processStructuredIntent(
        intent: StructuredIntent,
        originalPrompt: String,
        isLocalAction: Boolean = false
    ) {
        // Step 2: Security Gate Validation
        stateMachine.transitionTo(AssistantState.PLANNING)
        val validation = securityGate.evaluate(intent)

        when (validation.decision) {
            SecurityDecision.APPROVED -> {
                executeValidatedIntent(intent, validation.riskLevel, "APPROVED", originalPrompt)
            }

            SecurityDecision.CONFIRMATION_REQUIRED -> {
                audioEngine.playCue(AudioCue.CONFIRMATION)
                stateMachine.requestConfirmation(
                    title = validation.confirmationTitle ?: "Confirmation Required",
                    description = validation.confirmationPrompt ?: "VoidCore requires confirmation before executing.",
                    riskBadge = if (validation.riskLevel == RiskLevel.HIGH) "HIGH RISK" else "MEDIUM RISK",
                    onConfirm = {
                        scope.launch {
                            executeValidatedIntent(intent, validation.riskLevel, "USER_CONFIRMED", originalPrompt)
                        }
                    },
                    onDeny = {
                        scope.launch {
                            repository.logAudit(
                                actionName = intent.javaClass.simpleName,
                                target = originalPrompt,
                                riskLevel = validation.riskLevel.name,
                                securityDecision = "USER_DENIED",
                                outcomeStatus = "CANCELLED",
                                details = "Execution denied by user during confirmation prompt."
                            )
                            stateMachine.triggerError("Operation cancelled by user.")
                        }
                    }
                )
            }

            SecurityDecision.BLOCKED -> {
                audioEngine.playCue(AudioCue.WARNING)
                repository.logAudit(
                    actionName = intent.javaClass.simpleName,
                    target = originalPrompt,
                    riskLevel = validation.riskLevel.name,
                    securityDecision = "BLOCKED",
                    outcomeStatus = "BLOCKED",
                    details = "Security gate blocked execution: ${validation.reason}"
                )
                stateMachine.triggerError("Action blocked by security policy: ${validation.reason}")
            }
        }
    }

    private suspend fun executeValidatedIntent(
        intent: StructuredIntent,
        riskLevel: RiskLevel,
        securityDecision: String,
        originalPrompt: String
    ) {
        stateMachine.transitionTo(AssistantState.ACTING)
        audioEngine.playCue(AudioCue.ACTING)
        stateMachine.setActionStatus("Executing: ${intent.javaClass.simpleName}")

        delay(120)
        val outcome: ToolExecutionOutcome = if (intent is StructuredIntent.ConversationalResponse) {
            ToolExecutionOutcome(true, "Assistant Response", intent.text)
        } else {
            toolAdapter.execute(intent)
        }

        // Outcome Verification & Immutable Audit Logging
        val outcomeStatus = if (outcome.isSuccess) "SUCCESS" else "FAILED"
        repository.logAudit(
            actionName = outcome.actionLabel,
            target = originalPrompt,
            riskLevel = riskLevel.name,
            securityDecision = securityDecision,
            outcomeStatus = outcomeStatus,
            details = outcome.details + (outcome.remediation?.let { " [Remediation: $it]" } ?: "")
        )

        // Save assistant message to conversation history
        repository.saveMessage("assistant", outcome.details)

        // State Machine Feedback & Speech Output
        if (outcome.isSuccess) {
            audioEngine.playCue(AudioCue.SUCCESS)
            stateMachine.setActionStatus(outcome.details)
            stateMachine.transitionTo(AssistantState.SUCCESS)
            delay(400)
            voiceEngine.speak(outcome.details)
        } else {
            audioEngine.playCue(AudioCue.ERROR)
            val errorMsg = "${outcome.details} ${outcome.remediation ?: ""}"
            stateMachine.triggerError(errorMsg)
            voiceEngine.speak(errorMsg)
        }
    }

    fun interruptAssistant() {
        voiceEngine.cancelSpeech()
        stateMachine.transitionTo(AssistantState.LISTENING)
    }

    companion object {
        @Volatile
        private var INSTANCE: AgentBrain? = null

        fun getInstance(context: Context): AgentBrain {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AgentBrain(context).also { INSTANCE = it }
            }
        }
    }
}

