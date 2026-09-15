package com.example.core.renderer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.state.AssistantState
import com.example.core.state.PresenceForm

/**
 * Universal Living Core Presence View.
 * Driven exclusively by AssistantState snapshots and PresentationEnvelope parameters.
 * Supports dynamic morphing across all 4 PresenceForms (Full Presence, Compact, Capsule, Edge Agent),
 * left/right docking, reduce motion/transparency accessibility modes, and selectable backend.
 */
@Composable
fun VoidCorePresenceView(
    state: AssistantState,
    form: PresenceForm,
    audioEnergy: Float = 0f,
    quality: RendererQuality = RendererQuality.CINEMATIC,
    reduceMotion: Boolean = false,
    reduceTransparency: Boolean = false,
    dockingSide: DockingSide = DockingSide.NONE,
    backend: RendererBackend = remember { DefaultCoreRendererBackend() },
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val particleSystem = remember { VoidParticleSystem(quality) }

    LaunchedEffect(quality) {
        particleSystem.setQuality(quality)
    }

    // Continuous 60fps frame timer for organic, continuous motion without state reset
    var lastFrameTimeNanos by remember { mutableLongStateOf(0L) }
    var timePhase by remember { mutableStateOf(0f) }

    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            while (true) {
                withFrameNanos { frameNanos ->
                    if (lastFrameTimeNanos != 0L) {
                        val deltaMs = ((frameNanos - lastFrameTimeNanos) / 1_000_000L).coerceIn(1L, 64L)
                        timePhase += deltaMs * 0.002f
                    }
                    lastFrameTimeNanos = frameNanos
                }
            }
        } else {
            timePhase = 0f
        }
    }

    val dimensionModifier = when (form) {
        PresenceForm.FULL_PRESENCE -> modifier.size(280.dp)
        PresenceForm.COMPACT -> modifier.size(160.dp)
        PresenceForm.CAPSULE -> modifier.width(220.dp).height(68.dp)
        PresenceForm.EDGE_AGENT -> modifier.size(80.dp)
    }

    val envelope = PresentationEnvelope(
        state = state,
        form = form,
        audioEnergy = audioEnergy,
        timePhase = timePhase,
        quality = quality,
        reduceMotion = reduceMotion,
        reduceTransparency = reduceTransparency,
        dockingSide = dockingSide
    )

    Box(
        modifier = dimensionModifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            backend.renderFrame(
                scope = this,
                envelope = envelope,
                particleSystem = particleSystem
            )
        }
    }
}
