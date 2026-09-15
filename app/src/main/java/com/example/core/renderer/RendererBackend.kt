package com.example.core.renderer

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.core.state.AssistantState
import com.example.core.state.PresenceForm

/**
 * Envelope capturing real-time presentation parameters passed into the renderer.
 * Keeps CoreRenderer strictly isolated from direct application business logic.
 */
data class PresentationEnvelope(
    val state: AssistantState,
    val form: PresenceForm,
    val audioEnergy: Float = 0f,
    val timePhase: Float = 0f,
    val quality: RendererQuality = RendererQuality.CINEMATIC,
    val reduceMotion: Boolean = false,
    val reduceTransparency: Boolean = false,
    val dockingSide: DockingSide = DockingSide.NONE,
    val targetAreaBounds: SafeBounds = SafeBounds(0f, 0f, 1f, 1f)
)

enum class DockingSide {
    NONE,
    LEFT,
    RIGHT
}

data class SafeBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

/**
 * Pluggable graphics backend interface.
 * Allows decoupling Compose DrawScope, high-performance Software canvas,
 * and future Native / OpenGL ES 3.0 shaders.
 */
interface RendererBackend {
    val backendName: String
    val isHardwareAccelerated: Boolean
    val supportsShaders: Boolean

    fun renderFrame(
        scope: DrawScope,
        envelope: PresentationEnvelope,
        particleSystem: VoidParticleSystem
    )
}
