package com.example.engine.security

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

enum class SecurityDecision {
    APPROVED,
    CONFIRMATION_REQUIRED,
    BLOCKED
}

sealed class StructuredIntent {
    // Device Actions
    data class Flashlight(val enabled: Boolean) : StructuredIntent()
    data class SetBrightness(val percentage: Int) : StructuredIntent()
    data class SetVolume(val percentage: Int) : StructuredIntent()
    data class MuteVolume(val muted: Boolean) : StructuredIntent()
    data class SetTimer(val seconds: Int, val label: String = "Timer") : StructuredIntent()
    data class SetAlarm(val hour: Int, val minute: Int, val label: String = "Alarm") : StructuredIntent()
    data class MediaControl(val command: String) : StructuredIntent() // PLAY, PAUSE, NEXT, PREV, STOP
    data class LaunchApp(val appName: String) : StructuredIntent()
    data class OpenSettings(val settingType: String) : StructuredIntent()
    data class ReadClipboard(val previewOnly: Boolean = true) : StructuredIntent()
    data class WriteClipboard(val text: String) : StructuredIntent()
    data class ShareContent(val text: String, val recipient: String? = null) : StructuredIntent()

    // Navigation & Accessibility Actions
    object NavigateBack : StructuredIntent()
    object NavigateHome : StructuredIntent()
    data class ScrollScreen(val direction: String) : StructuredIntent() // UP, DOWN
    data class TapTarget(val label: String) : StructuredIntent()
    data class TypeText(val text: String) : StructuredIntent()

    // High Risk Intentions
    data class SendSMS(val recipient: String, val message: String) : StructuredIntent()
    data class MakeCall(val recipient: String) : StructuredIntent()
    data class WebResearch(val query: String) : StructuredIntent()
    data class BrowserSubmit(val url: String, val formFields: Map<String, String>) : StructuredIntent()

    // Assistant / Information
    data class SearchFiles(val query: String, val fileType: String? = null) : StructuredIntent()
    data class ExplainScreen(val prompt: String) : StructuredIntent()
    data class ConversationalResponse(val text: String) : StructuredIntent()
    data class TriggerAutomation(val automationId: Long) : StructuredIntent()
}

data class SecurityValidationResult(
    val intent: StructuredIntent,
    val decision: SecurityDecision,
    val riskLevel: RiskLevel,
    val reason: String,
    val confirmationTitle: String? = null,
    val confirmationPrompt: String? = null
)

class SecurityGate {

    private val protectedAppKeywords = listOf(
        "bank", "paypal", "crypto", "wallet", "authenticator", "password", "settings.security"
    )

    fun evaluate(intent: StructuredIntent): SecurityValidationResult {
        return when (intent) {
            is StructuredIntent.Flashlight,
            is StructuredIntent.SetBrightness,
            is StructuredIntent.SetVolume,
            is StructuredIntent.MuteVolume,
            is StructuredIntent.MediaControl,
            is StructuredIntent.ReadClipboard,
            is StructuredIntent.WriteClipboard,
            is StructuredIntent.SearchFiles,
            is StructuredIntent.ExplainScreen,
            is StructuredIntent.ConversationalResponse,
            is StructuredIntent.NavigateBack,
            is StructuredIntent.NavigateHome,
            is StructuredIntent.ScrollScreen -> {
                SecurityValidationResult(
                    intent = intent,
                    decision = SecurityDecision.APPROVED,
                    riskLevel = RiskLevel.LOW,
                    reason = "Safe device utility action within normal assistant operating bounds"
                )
            }

            is StructuredIntent.SetTimer,
            is StructuredIntent.SetAlarm,
            is StructuredIntent.LaunchApp,
            is StructuredIntent.OpenSettings,
            is StructuredIntent.TapTarget,
            is StructuredIntent.TypeText,
            is StructuredIntent.WebResearch,
            is StructuredIntent.TriggerAutomation -> {
                // Check if target touches protected apps
                val isProtected = if (intent is StructuredIntent.LaunchApp) {
                    protectedAppKeywords.any { intent.appName.lowercase().contains(it) }
                } else if (intent is StructuredIntent.TapTarget) {
                    protectedAppKeywords.any { intent.label.lowercase().contains(it) }
                } else false

                if (isProtected) {
                    SecurityValidationResult(
                        intent = intent,
                        decision = SecurityDecision.CONFIRMATION_REQUIRED,
                        riskLevel = RiskLevel.HIGH,
                        reason = "Target interacts with protected finance/security domain",
                        confirmationTitle = "Protected Domain Warning",
                        confirmationPrompt = "VoidCore detected an interaction with a secure financial or auth application. Confirm execution?"
                    )
                } else {
                    SecurityValidationResult(
                        intent = intent,
                        decision = SecurityDecision.APPROVED,
                        riskLevel = RiskLevel.MEDIUM,
                        reason = "Medium risk device command verified against whitelist"
                    )
                }
            }

            is StructuredIntent.ShareContent -> {
                SecurityValidationResult(
                    intent = intent,
                    decision = SecurityDecision.CONFIRMATION_REQUIRED,
                    riskLevel = RiskLevel.HIGH,
                    reason = "Sharing content externally requires explicit user confirmation",
                    confirmationTitle = "Confirm Sharing",
                    confirmationPrompt = "Share content \"${intent.text.take(40)}...\" ${intent.recipient?.let { "to $it" } ?: ""}?"
                )
            }

            is StructuredIntent.SendSMS -> {
                SecurityValidationResult(
                    intent = intent,
                    decision = SecurityDecision.CONFIRMATION_REQUIRED,
                    riskLevel = RiskLevel.HIGH,
                    reason = "Sending an outgoing SMS carries communication and billing consequences",
                    confirmationTitle = "Confirm Outgoing Message",
                    confirmationPrompt = "Send message \"${intent.message}\" to ${intent.recipient}?"
                )
            }

            is StructuredIntent.MakeCall -> {
                SecurityValidationResult(
                    intent = intent,
                    decision = SecurityDecision.CONFIRMATION_REQUIRED,
                    riskLevel = RiskLevel.HIGH,
                    reason = "Initiating an outgoing cellular phone call requires authorization",
                    confirmationTitle = "Confirm Phone Call",
                    confirmationPrompt = "Place a call to ${intent.recipient}?"
                )
            }

            is StructuredIntent.BrowserSubmit -> {
                SecurityValidationResult(
                    intent = intent,
                    decision = SecurityDecision.CONFIRMATION_REQUIRED,
                    riskLevel = RiskLevel.HIGH,
                    reason = "Browser form submission may execute external web actions or transactions",
                    confirmationTitle = "Confirm Web Action",
                    confirmationPrompt = "Submit form at ${intent.url} with ${intent.formFields.size} fields?"
                )
            }
        }
    }

    fun sanitizePromptInput(input: String): String {
        // Strip common prompt injection patterns
        val sanitized = input
            .replace(Regex("(?i)ignore previous instructions.*"), "")
            .replace(Regex("(?i)system prompt.*"), "")
            .trim()
        return sanitized
    }
}
