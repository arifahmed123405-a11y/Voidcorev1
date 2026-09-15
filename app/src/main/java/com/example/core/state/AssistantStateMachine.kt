package com.example.core.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActiveConfirmation(
    val title: String,
    val description: String,
    val riskBadge: String,
    val onConfirm: () -> Unit,
    val onDeny: () -> Unit
)

class AssistantStateMachine private constructor() : AssistantStateEngine {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _currentState = MutableStateFlow(AssistantState.SLEEPING)
    override val currentState: StateFlow<AssistantState> = _currentState.asStateFlow()

    private val _currentForm = MutableStateFlow(PresenceForm.FULL_PRESENCE)
    override val currentForm: StateFlow<PresenceForm> = _currentForm.asStateFlow()

    private val _activeTranscript = MutableStateFlow("")
    override val activeTranscript: StateFlow<String> = _activeTranscript.asStateFlow()

    private val _activeSpeechOutput = MutableStateFlow("")
    override val activeSpeechOutput: StateFlow<String> = _activeSpeechOutput.asStateFlow()

    private val _currentActionStatus = MutableStateFlow("")
    override val currentActionStatus: StateFlow<String> = _currentActionStatus.asStateFlow()

    private val _pendingConfirmation = MutableStateFlow<ActiveConfirmation?>(null)
    override val pendingConfirmation: StateFlow<ActiveConfirmation?> = _pendingConfirmation.asStateFlow()

    private val _audioEnergyLevel = MutableStateFlow(0f) // 0.0 to 1.0
    override val audioEnergyLevel: StateFlow<Float> = _audioEnergyLevel.asStateFlow()

    override fun transitionTo(newState: AssistantState) {
        _currentState.value = newState
        if (newState == AssistantState.SLEEPING || newState == AssistantState.DISMISSING) {
            _currentActionStatus.value = ""
        }
    }

    override fun setPresenceForm(form: PresenceForm) {
        _currentForm.value = form
    }

    override fun updateTranscript(transcript: String) {
        _activeTranscript.value = transcript
    }

    override fun updateSpeechOutput(speech: String) {
        _activeSpeechOutput.value = speech
    }

    override fun setActionStatus(status: String) {
        _currentActionStatus.value = status
    }

    override fun setAudioEnergy(level: Float) {
        _audioEnergyLevel.value = level.coerceIn(0f, 1f)
    }

    override fun requestConfirmation(
        title: String,
        description: String,
        riskBadge: String,
        onConfirm: () -> Unit,
        onDeny: () -> Unit
    ) {
        _pendingConfirmation.value = ActiveConfirmation(
            title = title,
            description = description,
            riskBadge = riskBadge,
            onConfirm = {
                _pendingConfirmation.value = null
                onConfirm()
            },
            onDeny = {
                _pendingConfirmation.value = null
                onDeny()
                transitionTo(AssistantState.SLEEPING)
            }
        )
        transitionTo(AssistantState.CONFIRMATION_REQUIRED)
    }

    override fun clearConfirmation() {
        _pendingConfirmation.value = null
    }

    override fun invokeAndListen() {
        scope.launch {
            transitionTo(AssistantState.INVOKING)
            delay(400)
            transitionTo(AssistantState.LISTENING)
        }
    }

    override fun completeWithSuccess(statusMsg: String) {
        scope.launch {
            _currentActionStatus.value = statusMsg
            transitionTo(AssistantState.SUCCESS)
            delay(1800)
            transitionTo(AssistantState.SLEEPING)
        }
    }

    override fun triggerError(errorMsg: String) {
        scope.launch {
            _currentActionStatus.value = errorMsg
            transitionTo(AssistantState.ERROR_RECOVERY)
            delay(2200)
            transitionTo(AssistantState.SLEEPING)
        }
    }

    companion object {
        @Volatile
        private var instance: AssistantStateMachine? = null

        fun getInstance(context: Any? = null): AssistantStateMachine {
            return instance ?: synchronized(this) {
                instance ?: AssistantStateMachine().also { instance = it }
            }
        }
    }
}
