package com.example.core.model

enum class ThemeMode {
    DARK_SYNTH,
    VOID_MIDNIGHT,
    SOLAR_OLED
}

enum class PresenceQualityTier {
    POWER_SAVER,
    BALANCED,
    CINEMATIC_ULTRA
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.DARK_SYNTH,
    val defaultVoicePresetId: String = "preset_neutral_core",
    val presenceQuality: PresenceQualityTier = PresenceQualityTier.BALANCED,
    val autoListenOnWake: Boolean = true,
    val speechInterruptionEnabled: Boolean = true,
    val telemetryOptIn: Boolean = false,
    val localOnlyMode: Boolean = true
)
