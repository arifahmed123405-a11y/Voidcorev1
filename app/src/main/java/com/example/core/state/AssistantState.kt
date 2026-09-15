package com.example.core.state

enum class AssistantState(val displayName: String, val description: String) {
    SLEEPING("Sleeping", "Calm, dark, low internal activity"),
    INVOKING("Invoking", "Presence assembles from edge filaments into core"),
    LISTENING("Listening", "Attentive, brighter cool particles, responsive"),
    THINKING("Thinking", "Deep internal circulation, multi-speed orbital activity"),
    PLANNING("Planning", "Richer branching flow implied by particle paths"),
    ACTING("Acting", "Directional energy bias and target projection"),
    CONFIRMATION_REQUIRED("Confirmation Required", "Poised, suspended, attentive for user confirmation"),
    SPEAKING("Speaking", "Rhythmic light & particle activity, voice output"),
    SUCCESS("Success", "Luminous convergence and restrained release"),
    ERROR_RECOVERY("Error Recovery", "Instability, then elegant stabilization"),
    DISMISSING("Dismissing", "Light collapses, particles retract to edge form");

    companion object {
        val WAITING_FOR_USER: AssistantState get() = CONFIRMATION_REQUIRED
    }
}

enum class PresenceForm(val displayName: String) {
    FULL_PRESENCE("Full Presence"),
    COMPACT("Compact"),
    CAPSULE("Capsule"),
    EDGE_AGENT("Edge Agent")
}

sealed class AssistantEvent {
    object Wake : AssistantEvent()
    object StartListening : AssistantEvent()
    data class SpeechInput(val text: String, val isPartial: Boolean = false) : AssistantEvent()
    data class PlanAction(val actionDescription: String) : AssistantEvent()
    data class ExecuteAction(val toolName: String) : AssistantEvent()
    data class RequireConfirmation(val prompt: String, val onConfirm: () -> Unit) : AssistantEvent()
    data class Speak(val text: String) : AssistantEvent()
    data class Completed(val message: String) : AssistantEvent()
    data class Error(val reason: String) : AssistantEvent()
    object Dismiss : AssistantEvent()
    object Sleep : AssistantEvent()
}
