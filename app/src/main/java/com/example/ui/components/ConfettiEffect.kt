package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class ParticleShape {
    RIBBON, STAR, CIRCLE, COIN
}

private data class Particle(
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

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 85
) {
    val celebrationPalette = remember {
        listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFF9100), // Amber
            Color(0xFFFF3D00), // Flame Orange
            Color(0xFF8B5CF6), // Royal Purple
            Color(0xFF38BDF8), // Sky Blue
            Color(0xFF10B981), // Emerald
            Color(0xFFF43F5E), // Rose Red
            Color(0xFFFBBF24)  // Radiant Yellow
        )
    }

    val particles = remember {
        val shapes = ParticleShape.values()
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                initialY = -0.15f - (Random.nextFloat() * 0.4f),
                speed = 0.45f + Random.nextFloat() * 0.65f,
                size = 10f + Random.nextFloat() * 16f,
                color = celebrationPalette[Random.nextInt(celebrationPalette.size)],
                angle = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 12f,
                swayAmplitude = 18f + Random.nextFloat() * 26f,
                swayFrequency = 2.5f + Random.nextFloat() * 3.5f,
                shape = shapes[Random.nextInt(shapes.size)]
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confettiTransition")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiLoop"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        for (p in particles) {
            // Smooth cyclic loop falling
            val yOffset = ((progress * p.speed + (p.initialY + 0.3f)) % 1.25f) * canvasHeight
            val sway = sin(progress * p.swayFrequency * Math.PI.toFloat() * 2f + p.angle) * p.swayAmplitude
            val xOffset = (p.x * canvasWidth + sway).coerceIn(0f, canvasWidth)
            val currentRotation = p.angle + progress * 360f * p.rotationSpeed
            val flipPerspective = cos((progress * 10f * p.rotationSpeed) + p.angle).coerceIn(-1f, 1f)

            rotate(degrees = currentRotation, pivot = Offset(xOffset, yOffset)) {
                when (p.shape) {
                    ParticleShape.RIBBON -> {
                        // Fluttering rectangular ribbon with 3D flip effect
                        val effectiveWidth = (p.size * 0.6f * kotlin.math.abs(flipPerspective)).coerceAtLeast(1.5f)
                        drawRect(
                            color = p.color,
                            topLeft = Offset(xOffset - effectiveWidth / 2f, yOffset - p.size / 2f),
                            size = Size(effectiveWidth, p.size)
                        )
                    }
                    ParticleShape.CIRCLE -> {
                        drawCircle(
                            color = p.color,
                            radius = p.size * 0.35f,
                            center = Offset(xOffset, yOffset)
                        )
                    }
                    ParticleShape.COIN -> {
                        val effectiveRadius = (p.size * 0.45f * kotlin.math.abs(flipPerspective)).coerceAtLeast(1.5f)
                        // Shiny Gold coin
                        drawCircle(
                            color = Color(0xFFFFD700),
                            radius = effectiveRadius,
                            center = Offset(xOffset, yOffset)
                        )
                        // Inner ring
                        drawCircle(
                            color = Color(0xFFFFF07A),
                            radius = effectiveRadius * 0.6f,
                            center = Offset(xOffset, yOffset)
                        )
                    }
                    ParticleShape.STAR -> {
                        drawStar(
                            center = Offset(xOffset, yOffset),
                            radius = p.size * 0.45f,
                            color = p.color
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val innerRadius = radius * 0.45f
    val points = 5
    val step = Math.PI / points

    for (i in 0 until (points * 2)) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = i * step - Math.PI / 2.0
        val px = (center.x + r * cos(angle)).toFloat()
        val py = (center.y + r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path = path, color = color)
}
