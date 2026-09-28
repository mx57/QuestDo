package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private data class Particle(
    val x: Float,
    val initialY: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val angle: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 70
) {
    val colors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFFFF5722), // Fire Orange
        Color(0xFF8B5CF6), // Purple
        Color(0xFF10B981), // Emerald
        Color(0xFF3B82F6), // Blue
        Color(0xFFEC4899)  // Pink
    )

    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                initialY = Random.nextFloat() * -0.5f,
                speed = 0.3f + Random.nextFloat() * 0.7f,
                size = 12f + Random.nextFloat() * 16f,
                color = colors[Random.nextInt(colors.size)],
                angle = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 10f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        for (p in particles) {
            val yOffset = ((progress * p.speed + (p.initialY + 0.5f)) % 1.2f) * canvasHeight
            val xOffset = (p.x * canvasWidth + kotlin.math.sin(progress * 6.28f + p.angle) * 30f).coerceIn(0f, canvasWidth)

            drawRect(
                color = p.color,
                topLeft = Offset(xOffset, yOffset),
                size = Size(p.size, p.size * 0.6f)
            )
        }
    }
}
