package com.example.core.model

data class VoiceProfile(
    val id: String,
    val name: String,
    val description: String,
    val pitch: Float,
    val speechRate: Float,
    val cadence: Float,
    val metallicResonance: Float,
    val bassBoost: Float,
    val harmonicDissonance: Float = 0.0f,
    val isSyntheticNonHuman: Boolean = false
)

data class AudioProfile(
    val soundEffectsEnabled: Boolean = true,
    val transitionCuesEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val masterVolume: Float = 1.0f,
    val activeVoiceProfileId: String = "preset_neutral_core"
)

object VoicePresets {
    val NEUTRAL_CORE = VoiceProfile(
        id = "preset_neutral_core",
        name = "Neutral Core",
        description = "Balanced, crisp, modern synthetic assistant voice with minimal coloration.",
        pitch = 1.0f,
        speechRate = 1.05f,
        cadence = 1.0f,
        metallicResonance = 0.0f,
        bassBoost = 0.1f,
        harmonicDissonance = 0.0f,
        isSyntheticNonHuman = false
    )

    val VOID = VoiceProfile(
        id = "preset_void",
        name = "Void",
        description = "Deep, reverberant, cosmic resonance with subtle dark-matter acoustic undertones.",
        pitch = 0.82f,
        speechRate = 0.95f,
        cadence = 0.9f,
        metallicResonance = 0.35f,
        bassBoost = 0.65f,
        harmonicDissonance = 0.15f,
        isSyntheticNonHuman = false
    )

    val ARCHITECT = VoiceProfile(
        id = "preset_architect",
        name = "Architect",
        description = "Precise, mathematical, measured cadence with crystal-clear analytical articulation.",
        pitch = 1.08f,
        speechRate = 1.15f,
        cadence = 1.1f,
        metallicResonance = 0.15f,
        bassBoost = 0.05f,
        harmonicDissonance = 0.0f,
        isSyntheticNonHuman = false
    )

    val SPECTRAL = VoiceProfile(
        id = "preset_spectral",
        name = "Spectral",
        description = "Ethereal, airy harmonic whisper with subtle spatial shimmer and light diffusion.",
        pitch = 1.25f,
        speechRate = 0.92f,
        cadence = 0.85f,
        metallicResonance = 0.40f,
        bassBoost = 0.0f,
        harmonicDissonance = 0.20f,
        isSyntheticNonHuman = false
    )

    val TITAN = VoiceProfile(
        id = "preset_titan",
        name = "Titan",
        description = "Heavy, resonant baritone with authoritative industrial weight and deep chest resonance.",
        pitch = 0.70f,
        speechRate = 0.90f,
        cadence = 0.95f,
        metallicResonance = 0.20f,
        bassBoost = 0.85f,
        harmonicDissonance = 0.05f,
        isSyntheticNonHuman = false
    )

    val OMEGA = VoiceProfile(
        id = "preset_omega",
        name = "Omega",
        description = "Original, synthetic and non-human with heavy metallic resonance, harmonic dissonance, and alien frequency vocoding.",
        pitch = 0.60f,
        speechRate = 1.20f,
        cadence = 1.30f,
        metallicResonance = 0.95f,
        bassBoost = 0.50f,
        harmonicDissonance = 0.85f,
        isSyntheticNonHuman = true
    )

    val ALL_PRESETS = listOf(
        NEUTRAL_CORE,
        VOID,
        ARCHITECT,
        SPECTRAL,
        TITAN,
        OMEGA
    )

    fun getById(id: String): VoiceProfile {
        return ALL_PRESETS.find { it.id == id } ?: NEUTRAL_CORE
    }
}
