package com.example.feature.call

import android.content.Context
import com.example.core.database.AutomationEntity
import com.example.core.database.VoidCoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActiveIncomingCall(
    val callerName: String,
    val callerNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isScreeningActive: Boolean = false,
    val screenedTranscript: List<String> = emptyList()
)

class CallIntelligenceManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val repo = VoidCoreRepository.getInstance(context)

    private val _simulatedCall = MutableStateFlow<ActiveIncomingCall?>(null)
    val simulatedCall: StateFlow<ActiveIncomingCall?> = _simulatedCall.asStateFlow()

    fun triggerSimulatedCall(callerName: String = "Ahmed", callerNumber: String = "+1 (555) 234-5678") {
        _simulatedCall.value = ActiveIncomingCall(
            callerName = callerName,
            callerNumber = callerNumber
        )
    }

    fun dismissCall() {
        _simulatedCall.value = null
    }

    fun startScreening(customMessage: String = "I am in a meeting, I will call you back in 30 minutes.") {
        val current = _simulatedCall.value ?: return
        _simulatedCall.value = current.copy(
            isScreeningActive = true,
            screenedTranscript = listOf(
                "VoidCore: Hello, I am VoidCore, an AI assistant for the user. They are currently unavailable and requested me to relay: \"$customMessage\"",
                "${current.callerName}: Understood! Please ask them to check the PDF when they are free. Thanks!"
            )
        )

        // Automatically create 30-minute callback reminder
        scope.launch {
            repo.saveAutomation(
                AutomationEntity(
                    title = "Return Call to ${current.callerName}",
                    triggerType = "CALL_FOLLOWUP",
                    triggerConfig = "In 30 minutes",
                    actionSummary = "Priority reminder to return call to ${current.callerName} (${current.callerNumber})",
                    isEnabled = true
                )
            )
            repo.logAudit(
                actionName = "Call Screening",
                target = current.callerName,
                riskLevel = "MEDIUM",
                securityDecision = "USER_CONFIRMED",
                outcomeStatus = "SUCCESS",
                details = "Screened incoming call with AI assistant disclosure and created 30m callback reminder."
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: CallIntelligenceManager? = null

        fun getInstance(context: Context): CallIntelligenceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CallIntelligenceManager(context).also { INSTANCE = it }
            }
        }
    }
}
