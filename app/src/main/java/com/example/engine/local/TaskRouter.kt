package com.example.engine.local

import com.example.engine.security.StructuredIntent

sealed class TaskRouteResult {
    data class DeterministicAction(
        val intent: StructuredIntent,
        val speechAcknowledgment: String,
        val actionCategory: String
    ) : TaskRouteResult()

    data class AmbiguousActionCandidate(
        val rawPrompt: String,
        val intentCandidate: StructuredIntent? = null
    ) : TaskRouteResult()

    data class LocalConversational(
        val prompt: String
    ) : TaskRouteResult()

    data class ComplexCloudQuery(
        val prompt: String
    ) : TaskRouteResult()
}

/**
 * Intelligent 4-Tier TaskRouter for VoidCore:
 * 1. Obvious Android Command -> Deterministic Parser -> Tool (0ms, 0 tokens)
 * 2. Ambiguous Action Phrase -> Local Model -> Strict Whitelist JSON Intent -> Security Gate -> Tool
 * 3. Normal Private Conversation -> Local Model (On-Device, Offline, Streaming)
 * 4. Complex / Fresh Query -> Optional Cloud Fallback (Remote providers NEVER execute tools directly)
 */
class TaskRouter(
    private val deterministicParser: DeterministicCommandParser = DeterministicCommandParser(),
    private val localRuntime: LocalModelRuntime = LocalModelRuntime()
) {

    fun route(rawPrompt: String): TaskRouteResult {
        val input = rawPrompt.trim().lowercase()

        // TIER 1: Obvious Deterministic Android Commands
        val parsedIntent = deterministicParser.tryParse(rawPrompt)
        if (parsedIntent != null && parsedIntent !is StructuredIntent.ConversationalResponse) {
            val ack = generateAcknowledgment(parsedIntent)
            return TaskRouteResult.DeterministicAction(
                intent = parsedIntent,
                speechAcknowledgment = ack,
                actionCategory = "Deterministic Tool Execution"
            )
        }

        // TIER 2: Ambiguous Action Phrases (Heuristically recognized as device intents needing local model resolution)
        if (isAmbiguousActionPhrase(input)) {
            return TaskRouteResult.AmbiguousActionCandidate(rawPrompt)
        }

        // TIER 4: Complex / Fresh Knowledge Cloud Query Check
        if (isComplexFreshCloudQuery(input)) {
            return TaskRouteResult.ComplexCloudQuery(rawPrompt)
        }

        // TIER 3: Normal Private Local Conversation
        return TaskRouteResult.LocalConversational(rawPrompt)
    }

    private fun isAmbiguousActionPhrase(input: String): Boolean {
        return input.contains("too dark") ||
               input.contains("can't see") ||
               input.contains("too bright") ||
               input.contains("blinding") ||
               input.contains("too loud") ||
               input.contains("can't hear") ||
               input.contains("turn it down") ||
               input.contains("wake me up") ||
               input.contains("count down") ||
               input.contains("take me home") ||
               input.contains("previous screen") ||
               input.contains("dim display") ||
               input.contains("blast the sound") ||
               input.contains("silence everything")
    }

    private fun isComplexFreshCloudQuery(input: String): Boolean {
        return input.startsWith("search ") ||
               input.startsWith("who won ") ||
               input.startsWith("latest news ") ||
               input.startsWith("stock price ") ||
               input.startsWith("weather forecast for ") ||
               input.startsWith("research ") ||
               input.contains("live score") ||
               input.contains("current exchange rate")
    }

    private fun generateAcknowledgment(intent: StructuredIntent): String {
        return when (intent) {
            is StructuredIntent.Flashlight -> if (intent.enabled) "Flashlight turned on." else "Flashlight turned off."
            is StructuredIntent.SetVolume -> "Volume set to ${intent.percentage}%."
            is StructuredIntent.MuteVolume -> if (intent.muted) "Audio muted." else "Audio unmuted."
            is StructuredIntent.SetBrightness -> "Screen brightness set to ${intent.percentage}%."
            is StructuredIntent.SetTimer -> "Timer set for ${intent.seconds} seconds."
            is StructuredIntent.SetAlarm -> "Alarm set for ${String.format("%02d:%02d", intent.hour, intent.minute)}."
            is StructuredIntent.MediaControl -> "Media ${intent.command.lowercase()}."
            is StructuredIntent.LaunchApp -> "Launching ${intent.appName}."
            is StructuredIntent.NavigateHome -> "Navigating to home screen."
            is StructuredIntent.NavigateBack -> "Returning to previous screen."
            is StructuredIntent.ScrollScreen -> "Scrolling ${intent.direction.lowercase()}."
            is StructuredIntent.TapTarget -> "Tapping ${intent.label}."
            is StructuredIntent.TypeText -> "Entering text."
            is StructuredIntent.OpenSettings -> "Opening ${intent.settingType.lowercase()} settings."
            is StructuredIntent.WriteClipboard -> "Copied to clipboard."
            is StructuredIntent.ReadClipboard -> "Reading clipboard content."
            is StructuredIntent.SearchFiles -> "Searching local storage for ${intent.query}."
            else -> "Action executed."
        }
    }
}
