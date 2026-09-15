package com.example.engine.voice

enum class VoicePreset(
    val id: String,
    val displayName: String,
    val description: String,
    val defaultPitch: Float,
    val defaultSpeed: Float,
    val warmth: Float,
    val metallicResonance: Float,
    val syntheticDepth: Float,
    val bass: Float,
    val glitchAmount: Float
) {
    NEUTRAL_CORE(
        id = "neutral_core",
        displayName = "Neutral Core",
        description = "Balanced, crisp, modern synthetic clarity",
        defaultPitch = 1.0f,
        defaultSpeed = 1.0f,
        warmth = 0.5f,
        metallicResonance = 0.2f,
        syntheticDepth = 0.3f,
        bass = 0.4f,
        glitchAmount = 0.0f
    ),
    VOID(
        id = "void",
        displayName = "Void",
        description = "Deep, resonant plasma timbre with sub-harmonic undertone",
        defaultPitch = 0.85f,
        defaultSpeed = 0.95f,
        warmth = 0.3f,
        metallicResonance = 0.6f,
        syntheticDepth = 0.8f,
        bass = 0.85f,
        glitchAmount = 0.05f
    ),
    ARCHITECT(
        id = "architect",
        displayName = "Architect",
        description = "Analytical, precise cadence with heightened clarity",
        defaultPitch = 1.05f,
        defaultSpeed = 1.1f,
        warmth = 0.4f,
        metallicResonance = 0.3f,
        syntheticDepth = 0.4f,
        bass = 0.3f,
        glitchAmount = 0.0f
    ),
    SPECTRAL(
        id = "spectral",
        displayName = "Spectral",
        description = "Ethereal, luminous presence with harmonic air",
        defaultPitch = 1.2f,
        defaultSpeed = 0.95f,
        warmth = 0.7f,
        metallicResonance = 0.1f,
        syntheticDepth = 0.5f,
        bass = 0.2f,
        glitchAmount = 0.0f
    ),
    TITAN(
        id = "titan",
        displayName = "Titan",
        description = "Heavy, authoritative low-frequency presence",
        defaultPitch = 0.75f,
        defaultSpeed = 0.9f,
        warmth = 0.4f,
        metallicResonance = 0.4f,
        syntheticDepth = 0.7f,
        bass = 0.95f,
        glitchAmount = 0.0f
    ),
    OMEGA(
        id = "omega",
        displayName = "Omega",
        description = "Original deep cinematic machine voice with rich acoustic depth",
        defaultPitch = 0.70f,
        defaultSpeed = 0.88f,
        warmth = 0.2f,
        metallicResonance = 0.75f,
        syntheticDepth = 0.9f,
        bass = 1.0f,
        glitchAmount = 0.1f
    )
}

data class VoiceParameters(
    var preset: VoicePreset = VoicePreset.OMEGA,
    var pitch: Float = 0.70f,
    var speed: Float = 0.90f,
    var cadence: Float = 0.5f,
    var warmth: Float = 0.4f,
    var metallicResonance: Float = 0.6f,
    var syntheticDepth: Float = 0.7f,
    var bass: Float = 0.8f,
    var clarity: Float = 0.9f,
    var reverb: Float = 0.4f,
    var glitchAmount: Float = 0.05f
)
