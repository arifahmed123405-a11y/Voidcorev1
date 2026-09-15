package com.example.core.renderer

enum class RendererQuality(val displayName: String, val particleCount: Int, val description: String) {
    CINEMATIC("Cinematic (High)", 550, "Hundreds of depth particles, multi-plane orbital ribbons, plasma haze and specular caustics"),
    BALANCED("Balanced", 250, "Smooth 60fps rendering with rich particle density and soft veil"),
    POWER_SAVER("Power Saver", 90, "Optimized particle density for extended battery life")
}
