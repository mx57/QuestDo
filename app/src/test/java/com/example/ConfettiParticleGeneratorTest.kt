package com.example

import com.example.ui.components.ConfettiParticleGenerator
import com.example.ui.components.ParticleShape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ConfettiParticleGeneratorTest {

    @Test
    fun testGenerateParticlesCount() {
        val count = 50
        val particles = ConfettiParticleGenerator.generateParticles(count)
        assertEquals(count, particles.size)
    }

    @Test
    fun testGeneratedParticlesPropertiesWithinExpectedBounds() {
        val count = 100
        val particles = ConfettiParticleGenerator.generateParticles(count, Random(12345))

        for (p in particles) {
            assertTrue("x coordinate should be in [0, 1]", p.x in 0f..1f)
            assertTrue("initialY should be negative offset", p.initialY in -0.55f..-0.15f)
            assertTrue("speed should be between 0.45 and 1.10", p.speed in 0.45f..1.10f)
            assertTrue("size should be between 10 and 26", p.size in 10f..26f)
            assertTrue("angle should be between 0 and 360", p.angle in 0f..360f)
            assertTrue("rotationSpeed should be between -6 and 6", p.rotationSpeed in -6f..6f)
            assertTrue("swayAmplitude should be between 18 and 44", p.swayAmplitude in 18f..44f)
            assertTrue("swayFrequency should be between 2.5 and 6.0", p.swayFrequency in 2.5f..6.0f)
            assertTrue("color should be in celebration palette", ConfettiParticleGenerator.CELEBRATION_PALETTE.contains(p.color))
            assertTrue("shape should be a valid ParticleShape", ParticleShape.values().contains(p.shape))
        }
    }
}
