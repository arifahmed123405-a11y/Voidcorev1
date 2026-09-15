package com.example.core.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.core.state.AssistantState
import com.example.core.state.PresenceForm
import kotlin.math.cos
import kotlin.math.sin

/**
 * Peak Visual Fidelity VoidCore Renderer.
 * Delivers a true round 3D dark-glass obsidian sphere, depth-sorted multi-plane
 * luminous particle streams with micro-trails, soft volumetric plasma veils,
 * surrounding light caustics, and fine specular reflections.
 */
class DefaultCoreRendererBackend : RendererBackend {
    override val backendName: String = "Peak Fidelity Glass & Orbital Pipeline"
    override val isHardwareAccelerated: Boolean = true
    override val supportsShaders: Boolean = true

    override fun renderFrame(
        scope: DrawScope,
        envelope: PresentationEnvelope,
        particleSystem: VoidParticleSystem
    ) {
        val width = scope.size.width
        val height = scope.size.height
        val centerX = width / 2f
        val centerY = height / 2f

        val state = envelope.state
        val form = envelope.form
        val audioEnergy = envelope.audioEnergy
        val timePhase = if (envelope.reduceMotion) 0f else envelope.timePhase

        // Sphere physical radius is CONSTANT across all normal states
        val baseRadius = when (form) {
            PresenceForm.FULL_PRESENCE -> width * 0.28f
            PresenceForm.COMPACT -> width * 0.26f
            PresenceForm.CAPSULE -> height * 0.32f
            PresenceForm.EDGE_AGENT -> width * 0.35f
        }

        // Only INVOKING and DISMISSING modulate presence entry/exit scale
        val scaleFactor = when (state) {
            AssistantState.INVOKING -> 0.80f + (if (envelope.reduceMotion) 0f else sin(timePhase * 5f) * 0.08f)
            AssistantState.DISMISSING -> 0.35f
            else -> 1.0f
        }

        val sphereRadius = baseRadius * scaleFactor

        // Update particle system with continuous orbital physics
        particleSystem.update(
            state = state,
            audioEnergy = audioEnergy,
            deltaTimeMs = 16L,
            coreRadiusPx = sphereRadius,
            centerX = centerX,
            centerY = centerY
        )

        // 1. Soft Atmospheric Dust & Outer Volumetric Plasma Veil
        drawAtmosphericVeil(
            scope = scope,
            centerX = centerX,
            centerY = centerY,
            radius = sphereRadius,
            state = state,
            audioEnergy = audioEnergy,
            timePhase = timePhase,
            reduceTransparency = envelope.reduceTransparency
        )

        // 2. Background Particles (Behind Sphere, depthZ < 0) - Dimmed & Occluded
        val particles = particleSystem.getParticles()
        val transparencyAlpha = if (envelope.reduceTransparency) 1f else 0.85f

        for (p in particles) {
            if (p.depthZ < 0) {
                // Micro-trail for background flow
                if (p.prevX != 0f && !envelope.reduceMotion) {
                    scope.drawLine(
                        color = p.currentColor.copy(alpha = (p.alpha * 0.35f * transparencyAlpha).coerceIn(0f, 1f)),
                        start = Offset(p.prevX, p.prevY),
                        end = Offset(p.x, p.y),
                        strokeWidth = p.size * 0.9f,
                        cap = StrokeCap.Round
                    )
                }
                // Background point
                scope.drawCircle(
                    color = p.currentColor.copy(
                        alpha = (p.alpha * 0.60f * transparencyAlpha).coerceIn(0f, 1f)
                    ),
                    radius = p.size * 0.9f,
                    center = Offset(p.x, p.y)
                )
            }
        }

        // 3. Volumetric Plasma Shell Membranes (Soft undulating energy ribbons)
        if (!envelope.reduceMotion) {
            drawPlasmaShellMembranes(
                scope = scope,
                centerX = centerX,
                centerY = centerY,
                radius = sphereRadius,
                state = state,
                timePhase = timePhase,
                audioEnergy = audioEnergy,
                reduceTransparency = envelope.reduceTransparency
            )
        }

        // 4. True 3D Dark-Glass Obsidian Sphere with Ambient Caustics & Rim Illumination
        drawDarkGlassSphere(
            scope = scope,
            centerX = centerX,
            centerY = centerY,
            radius = sphereRadius,
            state = state,
            timePhase = timePhase,
            audioEnergy = audioEnergy,
            reduceTransparency = envelope.reduceTransparency
        )

        // 5. Foreground Particles (In Front of Sphere, depthZ >= 0) - Bright Bloom & Trails
        for (p in particles) {
            if (p.depthZ >= 0) {
                // Micro-trail producing natural luminous ribbons
                if (p.prevX != 0f && !envelope.reduceMotion) {
                    scope.drawLine(
                        color = p.currentColor.copy(alpha = (p.alpha * 0.65f * transparencyAlpha).coerceIn(0f, 1f)),
                        start = Offset(p.prevX, p.prevY),
                        end = Offset(p.x, p.y),
                        strokeWidth = p.size * 1.3f,
                        cap = StrokeCap.Round
                    )
                }

                // Volumetric bloom halo
                if (!envelope.reduceTransparency) {
                    val bloomRadius = if (p.isKernel) p.size * 3.2f else p.size * 2.2f
                    scope.drawCircle(
                        color = p.currentColor.copy(alpha = (p.alpha * 0.32f).coerceIn(0f, 1f)),
                        radius = bloomRadius,
                        center = Offset(p.x, p.y)
                    )
                }

                // Crisp luminous kernel/particle core
                scope.drawCircle(
                    color = p.currentColor.copy(
                        alpha = if (envelope.reduceTransparency) 1f else p.alpha.coerceIn(0f, 1f)
                    ),
                    radius = p.size * 1.1f,
                    center = Offset(p.x, p.y)
                )
            }
        }

        // 6. 3D Glass Specular Reflection & Polished Surface Caustics
        drawGlassSpecularCaustics(
            scope = scope,
            centerX = centerX,
            centerY = centerY,
            radius = sphereRadius,
            state = state,
            timePhase = timePhase,
            reduceTransparency = envelope.reduceTransparency
        )
    }

    private fun drawAtmosphericVeil(
        scope: DrawScope,
        centerX: Float,
        centerY: Float,
        radius: Float,
        state: AssistantState,
        audioEnergy: Float,
        timePhase: Float,
        reduceTransparency: Boolean
    ) {
        val veilColor = when (state) {
            AssistantState.SLEEPING -> Color(0xFF003B5C)
            AssistantState.INVOKING -> Color(0xFF00B0FF)
            AssistantState.LISTENING -> Color(0xFF00E5FF)
            AssistantState.THINKING -> Color(0xFF9C27B0)
            AssistantState.PLANNING -> Color(0xFF7C4DFF)
            AssistantState.ACTING -> Color(0xFF00B0FF)
            AssistantState.CONFIRMATION_REQUIRED -> Color(0xFFFF9100)
            AssistantState.SPEAKING -> Color(0xFF00E5FF)
            AssistantState.SUCCESS -> Color(0xFF00E676)
            AssistantState.ERROR_RECOVERY -> Color(0xFFFF3D00)
            AssistantState.DISMISSING -> Color(0xFF263238)
        }

        val pulse = sin(timePhase * 2.8f) * 0.06f + (audioEnergy * 0.18f)
        val outerRadius = radius * (2.15f + pulse)
        val alphaMult = if (reduceTransparency) 0.85f else 1.0f

        scope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    veilColor.copy(alpha = (0.26f + audioEnergy * 0.18f) * alphaMult),
                    Color(0xFF651FFF).copy(alpha = 0.10f * alphaMult),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = outerRadius
            ),
            radius = outerRadius,
            center = Offset(centerX, centerY)
        )
    }

    private fun drawPlasmaShellMembranes(
        scope: DrawScope,
        centerX: Float,
        centerY: Float,
        radius: Float,
        state: AssistantState,
        timePhase: Float,
        audioEnergy: Float,
        reduceTransparency: Boolean
    ) {
        // Soft undulating energy veil bands without harsh wireframe lines
        val membraneColor = when (state) {
            AssistantState.ERROR_RECOVERY -> Color(0xFFFFAB40)
            AssistantState.CONFIRMATION_REQUIRED -> Color(0xFFFFD54F)
            AssistantState.SUCCESS -> Color(0xFFB9F6CA)
            AssistantState.THINKING -> Color(0xFFEA80FC)
            AssistantState.PLANNING -> Color(0xFFB388FF)
            AssistantState.ACTING -> Color(0xFF80D8FF)
            else -> Color(0xFF80D8FF)
        }

        for (i in 0..2) {
            val membraneAngle = i * 2.094f + timePhase * (0.8f + i * 0.25f)
            val wobbleR = radius * (1.10f + i * 0.22f) + sin(timePhase * 3f + i) * (radius * 0.08f)
            val membraneCenterX = centerX + cos(membraneAngle) * (radius * 0.12f)
            val membraneCenterY = centerY + sin(membraneAngle) * (radius * 0.08f)

            scope.drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        membraneColor.copy(alpha = (0.16f + audioEnergy * 0.12f).coerceIn(0f, 1f)),
                        Color(0xFF7C4DFF).copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(membraneCenterX, membraneCenterY),
                    radius = wobbleR
                ),
                topLeft = Offset(membraneCenterX - wobbleR, membraneCenterY - wobbleR * 0.75f),
                size = Size(wobbleR * 2f, wobbleR * 1.5f)
            )
        }
    }

    private fun drawDarkGlassSphere(
        scope: DrawScope,
        centerX: Float,
        centerY: Float,
        radius: Float,
        state: AssistantState,
        timePhase: Float,
        audioEnergy: Float,
        reduceTransparency: Boolean
    ) {
        // 1. Deep Obsidian Blue-Black Solid Core
        scope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0F172A), // Deep obsidian slate
                    Color(0xFF050B18), // Deep navy black
                    Color(0xFF02040A)  // Total void core
                ),
                center = Offset(centerX - radius * 0.18f, centerY - radius * 0.22f),
                radius = radius * 1.15f
            ),
            radius = radius,
            center = Offset(centerX, centerY)
        )

        // 2. Surrounding Light Caustics: Surrounding particles visibly illuminate the dark sphere surface
        val plasmaColor = when (state) {
            AssistantState.SLEEPING -> Color(0xFF005580).copy(alpha = 0.30f)
            AssistantState.INVOKING -> Color(0xFF00B0FF).copy(alpha = 0.55f)
            AssistantState.LISTENING -> Color(0xFF00E5FF).copy(alpha = 0.60f)
            AssistantState.THINKING -> Color(0xFF6A1B9A).copy(alpha = 0.60f)
            AssistantState.PLANNING -> Color(0xFF4527A0).copy(alpha = 0.55f)
            AssistantState.ACTING -> Color(0xFF0091EA).copy(alpha = 0.65f)
            AssistantState.CONFIRMATION_REQUIRED -> Color(0xFFFF6D00).copy(alpha = 0.60f)
            AssistantState.SPEAKING -> Color(0xFF00E5FF).copy(alpha = 0.50f + audioEnergy * 0.28f)
            AssistantState.SUCCESS -> Color(0xFF00BFA5).copy(alpha = 0.65f)
            AssistantState.ERROR_RECOVERY -> Color(0xFFD84315).copy(alpha = 0.60f)
            AssistantState.DISMISSING -> Color(0xFF263238).copy(alpha = 0.35f)
        }

        val internalPulseX = centerX + cos(timePhase * 2.2f) * (radius * 0.22f)
        val internalPulseY = centerY + sin(timePhase * 2.2f) * (radius * 0.22f)

        scope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    plasmaColor,
                    Color(0xFF7C4DFF).copy(alpha = 0.22f),
                    Color.Transparent
                ),
                center = Offset(internalPulseX, internalPulseY),
                radius = radius * 0.88f
            ),
            radius = radius * 0.95f,
            center = Offset(centerX, centerY)
        )

        // 3. Cyan Rim Illumination, Violet Reflection, and Sparse Peach Reflection
        val rimColors = when (state) {
            AssistantState.ERROR_RECOVERY -> listOf(
                Color(0xFFFF3D00).copy(alpha = 0.85f),
                Color(0xFFFF9100).copy(alpha = 0.80f),
                Color(0xFFFFD54F).copy(alpha = 0.45f),
                Color(0xFFFF3D00).copy(alpha = 0.85f)
            )
            AssistantState.CONFIRMATION_REQUIRED -> listOf(
                Color(0xFFFF9100).copy(alpha = 0.85f),
                Color(0xFFFFD600).copy(alpha = 0.80f),
                Color(0xFFFFAB40).copy(alpha = 0.45f),
                Color(0xFFFF9100).copy(alpha = 0.85f)
            )
            AssistantState.SUCCESS -> listOf(
                Color(0xFF00E676).copy(alpha = 0.85f),
                Color(0xFF69F0AE).copy(alpha = 0.80f),
                Color(0xFFB9F6CA).copy(alpha = 0.45f),
                Color(0xFF00E676).copy(alpha = 0.85f)
            )
            AssistantState.THINKING, AssistantState.PLANNING -> listOf(
                Color(0xFFBA68C8).copy(alpha = 0.85f),
                Color(0xFF7C4DFF).copy(alpha = 0.80f),
                Color(0xFF00E5FF).copy(alpha = 0.45f),
                Color(0xFFBA68C8).copy(alpha = 0.85f)
            )
            else -> listOf(
                Color(0xFF00E5FF).copy(alpha = 0.85f),
                Color(0xFF7C4DFF).copy(alpha = 0.80f),
                Color(0xFFFFB74D).copy(alpha = 0.45f),
                Color(0xFF00E5FF).copy(alpha = 0.85f)
            )
        }

        // Soft Fresnel rim glow without harsh sharp edge
        scope.drawCircle(
            brush = Brush.sweepGradient(
                colors = rimColors,
                center = Offset(centerX, centerY)
            ),
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 3.0f)
        )
    }

    private fun drawGlassSpecularCaustics(
        scope: DrawScope,
        centerX: Float,
        centerY: Float,
        radius: Float,
        state: AssistantState,
        timePhase: Float,
        reduceTransparency: Boolean
    ) {
        // Primary Top-Left Specular Glint (Glass highlight)
        val glintOffsetX = centerX - radius * 0.35f
        val glintOffsetY = centerY - radius * 0.38f
        val glintRadius = radius * 0.44f

        scope.drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.58f),
                    Color(0xFF80D8FF).copy(alpha = 0.28f),
                    Color.Transparent
                ),
                center = Offset(glintOffsetX, glintOffsetY),
                radius = glintRadius
            ),
            topLeft = Offset(glintOffsetX - glintRadius * 0.6f, glintOffsetY - glintRadius * 0.35f),
            size = Size(glintRadius * 1.2f, glintRadius * 0.7f)
        )

        // Secondary Violet & Warm Peach Bounce Reflection (Lower right curve)
        val bounceX = centerX + radius * 0.34f
        val bounceY = centerY + radius * 0.34f
        scope.drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE1BEE7).copy(alpha = 0.32f),
                    Color(0xFFFFCC80).copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = Offset(bounceX, bounceY),
                radius = radius * 0.32f
            ),
            topLeft = Offset(bounceX - radius * 0.2f, bounceY - radius * 0.16f),
            size = Size(radius * 0.42f, radius * 0.32f)
        )
    }
}

