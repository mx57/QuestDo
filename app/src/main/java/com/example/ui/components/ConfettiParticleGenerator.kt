package com.example.ui.components

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class ParticleShape {
    RIBBON, STAR, CIRCLE, COIN
}

data class Particle(
    val x: Float,
    val initialY: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val angle: Float,
    val rotationSpeed: Float,
    val swayAmplitude: Float,
    val swayFrequency: Float,
    val shape: ParticleShape
)

object ConfettiParticleGenerator {
    val CELEBRATION_PALETTE: List<Color> = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFFFF9100), // Amber
        Color(0xFFFF3D00), // Flame Orange
        Color(0xFF8B5CF6), // Royal Purple
        Color(0xFF38BDF8), // Sky Blue
        Color(0xFF10B981), // Emerald
        Color(0xFFF43F5E), // Rose Red
        Color(0xFFFBBF24)  // Radiant Yellow
    )

    fun generateParticles(particleCount: Int = 85, random: Random = Random): List<Particle> {
        val shapes = ParticleShape.values()
        return List(particleCount) {
            Particle(
                x = random.nextFloat(),
                initialY = -0.15f - (random.nextFloat() * 0.4f),
                speed = 0.45f + random.nextFloat() * 0.65f,
                size = 10f + random.nextFloat() * 16f,
                color = CELEBRATION_PALETTE[random.nextInt(CELEBRATION_PALETTE.size)],
                angle = random.nextFloat() * 360f,
                rotationSpeed = (random.nextFloat() - 0.5f) * 12f,
                swayAmplitude = 18f + random.nextFloat() * 26f,
                swayFrequency = 2.5f + random.nextFloat() * 3.5f,
                shape = shapes[random.nextInt(shapes.size)]
            )
        }
    }
}
