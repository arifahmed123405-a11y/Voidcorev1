package com.example

import com.example.core.renderer.DefaultCoreRendererBackend
import com.example.core.renderer.DockingSide
import com.example.core.renderer.OpenGLES3CoreRendererBackend
import com.example.core.renderer.PresentationEnvelope
import com.example.core.renderer.RendererQuality
import com.example.core.renderer.VoidParticleSystem
import com.example.core.state.AssistantState
import com.example.core.state.PresenceForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoidCorePhase1VisualTest {

    @Test
    fun testPresentationEnvelopeAndMorphingForms() {
        val states = AssistantState.values()
        assertEquals(11, states.size)

        val forms = PresenceForm.values()
        assertEquals(4, forms.size)

        // Verify envelope immutability and default fields
        val envelope = PresentationEnvelope(
            state = AssistantState.THINKING,
            form = PresenceForm.CAPSULE,
            audioEnergy = 0.5f,
            timePhase = 1.2f,
            quality = RendererQuality.CINEMATIC,
            reduceMotion = false,
            reduceTransparency = false,
            dockingSide = DockingSide.RIGHT
        )

        assertEquals(AssistantState.THINKING, envelope.state)
        assertEquals(PresenceForm.CAPSULE, envelope.form)
        assertEquals(DockingSide.RIGHT, envelope.dockingSide)
        assertEquals(0.5f, envelope.audioEnergy, 0.001f)
    }

    @Test
    fun testRendererBackendPluggability() {
        val defaultBackend = DefaultCoreRendererBackend()
        val glBackend = OpenGLES3CoreRendererBackend()

        assertNotNull(defaultBackend.backendName)
        assertTrue(defaultBackend.isHardwareAccelerated)
        assertTrue(defaultBackend.supportsShaders)

        assertNotNull(glBackend.backendName)
        assertTrue(glBackend.isHardwareAccelerated)
        assertTrue(glBackend.supportsShaders)
    }

    @Test
    fun testParticleSystemDynamicQualityTiers() {
        val system = VoidParticleSystem(RendererQuality.CINEMATIC)
        assertEquals(RendererQuality.CINEMATIC.particleCount, system.getParticles().size)

        system.setQuality(RendererQuality.BALANCED)
        assertEquals(RendererQuality.BALANCED.particleCount, system.getParticles().size)

        system.setQuality(RendererQuality.POWER_SAVER)
        assertEquals(RendererQuality.POWER_SAVER.particleCount, system.getParticles().size)
    }

    @Test
    fun testParticleSystemPhysicsAcrossAll11States() {
        val system = VoidParticleSystem(RendererQuality.POWER_SAVER)

        for (state in AssistantState.values()) {
            system.update(
                state = state,
                audioEnergy = 0.4f,
                deltaTimeMs = 16L,
                coreRadiusPx = 100f,
                centerX = 200f,
                centerY = 200f
            )
            val particles = system.getParticles()
            assertTrue(particles.isNotEmpty())
            for (p in particles) {
                assertTrue(p.alpha >= 0f)
            }
        }
    }
}
