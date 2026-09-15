package com.example.core.renderer

import androidx.compose.ui.graphics.Color
import com.example.core.state.AssistantState
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    var planeTiltX: Float,
    var planeTiltY: Float,
    var baseRadius: Float,
    var currentRadius: Float,
    var angle: Float,
    var angularSpeed: Float,
    var size: Float,
    var baseColor: Color,
    var currentColor: Color,
    var alpha: Float,
    var depthZ: Float = 0f,
    var x: Float = 0f,
    var y: Float = 0f,
    var prevX: Float = 0f,
    var prevY: Float = 0f,
    var ribbonPhase: Float = 0f,
    var planeIndex: Int = 0,
    var isKernel: Boolean = false
)

/**
 * Peak Fidelity Particle System:
 * Generates hundreds to thousands of tiny luminous particles across 16+ inclined orbital planes.
 * Particles are sized small (0.8f - 2.2f px) so the human eye perceives coherent luminous ribbons
 * and flowing plasma density rather than discrete coarse dots.
 */
class VoidParticleSystem(private var quality: RendererQuality = RendererQuality.CINEMATIC) {

    private val particles = ArrayList<Particle>()
    private val rng = Random(1337)

    // Palette: Dominant electric cyan / cyan primary, secondary violet / lavender / magenta, sparse warm peach / white kernels
    private val cyanPrimary = Color(0xFF00E5FF)
    private val electricBlue = Color(0xFF00B0FF)
    private val iceCyan = Color(0xFF80D8FF)
    private val violetSecondary = Color(0xFF7C4DFF)
    private val lavender = Color(0xFFB388FF)
    private val magentaPlasma = Color(0xFFE040FB)
    private val warmPeach = Color(0xFFFFB74D)
    private val warmAmber = Color(0xFFFFCC80)
    private val radiantWhite = Color(0xFFFFFFFF)
    private val errorDestabilize = Color(0xFFFF5252)

    init {
        rebuildParticles(quality.particleCount)
    }

    fun setQuality(newQuality: RendererQuality) {
        if (quality != newQuality) {
            quality = newQuality
            rebuildParticles(newQuality.particleCount)
        }
    }

    private fun rebuildParticles(count: Int) {
        particles.clear()
        val numPlanes = 16

        for (i in 0 until count) {
            val planeIdx = i % numPlanes
            // Distinct inclined orbital planes with harmonic inclinations
            val planeAngle = (planeIdx.toFloat() / numPlanes) * 3.14159f
            val tiltX = cos(planeAngle) * 0.95f + (rng.nextFloat() * 0.2f - 0.1f)
            val tiltY = sin(planeAngle) * 0.95f + (rng.nextFloat() * 0.2f - 0.1f)

            // Orbital band radii with Gaussian concentration around 0.8f - 1.4f core radius
            val bandType = rng.nextInt(10)
            val baseRad = when {
                bandType < 4 -> 0.75f + rng.nextFloat() * 0.35f // Inner shell flow
                bandType < 8 -> 1.05f + rng.nextFloat() * 0.45f // Mid orbital ribbons
                else -> 1.45f + rng.nextFloat() * 0.40f         // Outer atmospheric dust
            }

            // Direction: Both clockwise and counter-clockwise orbital streams
            val direction = if (planeIdx % 2 == 0) 1f else -1f
            val speedMagnitude = 0.012f + rng.nextFloat() * 0.028f
            val speed = direction * speedMagnitude

            // Tiny size to prioritize luminous ribbon density over individual dots
            val isKernel = rng.nextInt(15) == 0
            val size = if (isKernel) {
                1.8f + rng.nextFloat() * 1.2f
            } else {
                0.8f + rng.nextFloat() * 1.4f
            }

            // Chromatic distribution
            val colorRoll = rng.nextInt(100)
            val colorChoice = when {
                colorRoll < 55 -> cyanPrimary
                colorRoll < 72 -> electricBlue
                colorRoll < 85 -> lavender
                colorRoll < 92 -> violetSecondary
                colorRoll < 97 -> warmPeach
                else -> radiantWhite
            }

            particles.add(
                Particle(
                    planeTiltX = tiltX,
                    planeTiltY = tiltY,
                    baseRadius = baseRad,
                    currentRadius = baseRad,
                    angle = rng.nextFloat() * 6.283185f,
                    angularSpeed = speed,
                    size = size,
                    baseColor = colorChoice,
                    currentColor = colorChoice,
                    alpha = 0.35f + rng.nextFloat() * 0.65f,
                    ribbonPhase = (planeIdx * 0.392f) + rng.nextFloat() * 0.5f,
                    planeIndex = planeIdx,
                    isKernel = isKernel
                )
            )
        }
    }

    fun update(
        state: AssistantState,
        audioEnergy: Float,
        deltaTimeMs: Long,
        coreRadiusPx: Float,
        centerX: Float,
        centerY: Float
    ) {
        val dtFactor = (deltaTimeMs / 16.6f).coerceIn(0.2f, 2.5f)

        // Kinetic state modulation
        val speedMultiplier = when (state) {
            AssistantState.SLEEPING -> 0.35f
            AssistantState.INVOKING -> 2.4f
            AssistantState.LISTENING -> 1.3f + audioEnergy * 2.2f // Heightened cool activity
            AssistantState.THINKING -> 2.8f                       // High-speed counter-rotations
            AssistantState.PLANNING -> 2.0f                       // Complex multi-plane resonance
            AssistantState.ACTING -> 2.5f                         // Directional kinetic bias
            AssistantState.CONFIRMATION_REQUIRED -> 0.7f          // Pulsing containment
            AssistantState.SPEAKING -> 1.4f + audioEnergy * 2.0f  // Rhythmic acoustic flow
            AssistantState.SUCCESS -> 1.6f                        // Radiant harmonic release
            AssistantState.ERROR_RECOVERY -> 3.2f                 // Destabilization perturbation
            AssistantState.DISMISSING -> 0.5f
        }

        val targetRadialScale = when (state) {
            AssistantState.SLEEPING -> 0.88f
            AssistantState.INVOKING -> 1.25f
            AssistantState.LISTENING -> 1.05f + audioEnergy * 0.30f
            AssistantState.THINKING -> 1.12f
            AssistantState.PLANNING -> 1.28f
            AssistantState.ACTING -> 1.32f
            AssistantState.CONFIRMATION_REQUIRED -> 0.95f
            AssistantState.SPEAKING -> 1.10f + audioEnergy * 0.25f
            AssistantState.SUCCESS -> 1.40f
            AssistantState.ERROR_RECOVERY -> 1.22f
            AssistantState.DISMISSING -> 0.45f
        }

        for (p in particles) {
            p.prevX = p.x
            p.prevY = p.y

            // State specific angular velocity tweaks (e.g. counter-rotating inner flows in THINKING)
            val stateAngularBias = if (state == AssistantState.THINKING) {
                if (p.planeIndex % 2 == 0) 1.6f else -1.6f
            } else 1.0f

            // Continuous time progression (no visible loop resets)
            p.angle += p.angularSpeed * speedMultiplier * stateAngularBias * dtFactor
            if (p.angle > 6.283185f) p.angle -= 6.283185f
            if (p.angle < 0f) p.angle += 6.283185f

            // Radial interpolation towards target
            val targetRad = p.baseRadius * targetRadialScale * coreRadiusPx
            p.currentRadius += (targetRad - p.currentRadius) * 0.09f * dtFactor

            // 3D Orbital Coordinates
            val cosA = cos(p.angle)
            val sinA = sin(p.angle)

            val rawX = cosA * p.currentRadius
            val rawY = sinA * p.currentRadius
            val rawZ = sin(p.angle + p.ribbonPhase) * (p.currentRadius * 0.55f)

            // 3D Matrix Euler Tilt Rotation
            val tiltedY = rawY * cos(p.planeTiltX) - rawZ * sin(p.planeTiltX)
            val tiltedZ = rawY * sin(p.planeTiltX) + rawZ * cos(p.planeTiltX)
            val finalX = rawX * cos(p.planeTiltY) + tiltedZ * sin(p.planeTiltY)

            // Dynamic state biases
            var biasX = 0f
            var biasY = 0f
            when (state) {
                AssistantState.ACTING -> {
                    // Biased energy projection along upper-right vector
                    biasX = coreRadiusPx * 0.40f
                    biasY = -coreRadiusPx * 0.22f
                }
                AssistantState.PLANNING -> {
                    // Multi-plane harmonic orbital weave
                    biasX = sin(p.angle * 2.5f + p.ribbonPhase) * (coreRadiusPx * 0.18f)
                    biasY = cos(p.angle * 2.5f + p.ribbonPhase) * (coreRadiusPx * 0.18f)
                }
                AssistantState.ERROR_RECOVERY -> {
                    // Jitter / temporary perturbation
                    biasX = (rng.nextFloat() - 0.5f) * (coreRadiusPx * 0.15f)
                    biasY = (rng.nextFloat() - 0.5f) * (coreRadiusPx * 0.15f)
                }
                else -> Unit
            }

            p.x = centerX + finalX + biasX
            p.y = centerY + tiltedY + biasY
            p.depthZ = tiltedZ

            // Depth Projection: Back-side (depthZ < 0) particles dim and occlude; front-side (depthZ >= 0) brighten and bloom
            val depthRatio = (p.depthZ / (coreRadiusPx * 1.1f)).coerceIn(-1f, 1f)
            val depthFactor = if (p.depthZ < 0) {
                0.35f + (depthRatio + 1f) * 0.30f // Backside occlusion / dimming
            } else {
                0.75f + depthRatio * 0.45f        // Frontside brightening
            }

            val baseStateAlpha = when (state) {
                AssistantState.SLEEPING -> 0.30f
                AssistantState.INVOKING -> 0.95f
                AssistantState.LISTENING -> 0.85f + audioEnergy * 0.15f
                AssistantState.THINKING -> 0.90f
                AssistantState.PLANNING -> 0.88f
                AssistantState.ACTING -> 0.95f
                AssistantState.CONFIRMATION_REQUIRED -> 0.70f
                AssistantState.SPEAKING -> 0.85f + audioEnergy * 0.15f
                AssistantState.SUCCESS -> 1.0f
                AssistantState.ERROR_RECOVERY -> 0.92f
                AssistantState.DISMISSING -> 0.35f
            }

            p.alpha = (baseStateAlpha * depthFactor).coerceIn(0.12f, 1.0f)

            // State Chromatic Response
            p.currentColor = when (state) {
                AssistantState.ERROR_RECOVERY -> {
                    if (rng.nextBoolean()) errorDestabilize else warmPeach
                }
                AssistantState.SUCCESS -> {
                    if (p.isKernel || p.depthZ > 0) radiantWhite else cyanPrimary
                }
                AssistantState.THINKING -> {
                    if (p.planeIndex % 3 == 0) magentaPlasma else electricBlue
                }
                AssistantState.PLANNING -> {
                    if (p.planeIndex % 2 == 0) lavender else iceCyan
                }
                AssistantState.SPEAKING -> {
                    if (audioEnergy > 0.45f && p.isKernel) radiantWhite else p.baseColor
                }
                else -> p.baseColor
            }
        }
    }

    fun getParticles(): List<Particle> = particles
}

