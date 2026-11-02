package com.kidslearning.chutki.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val isStar: Boolean
)

@Composable
fun StarConfettiOverlay(
    trigger: Boolean = true,
    onFinished: () -> Unit = {},
    onAnimationComplete: () -> Unit = onFinished
) {
    if (!trigger) return

    val callback = if (onAnimationComplete !== onFinished) onAnimationComplete else onFinished

    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFF4081), // Pink
            Color(0xFF00E5FF), // Cyan
            Color(0xFF00E676), // Mint
            Color(0xFFFF9100), // Orange
            Color(0xFFD500F9)  // Violet
        )
        List(40) {
            ConfettiParticle(
                startX = 0.5f,
                startY = 0.4f,
                vx = (Random.nextFloat() - 0.5f) * 1.6f,
                vy = (Random.nextFloat() * -1.2f) - 0.2f,
                color = colors.random(),
                size = Random.nextFloat() * 16f + 10f,
                isStar = Random.nextBoolean()
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = LinearEasing)
        )
        callback()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = progress.value
        val gravity = 2.2f

        particles.forEach { p ->
            val curX = (p.startX + (p.vx * t)) * size.width
            val curY = (p.startY + (p.vy * t) + (0.5f * gravity * t * t)) * size.height
            val alpha = (1f - (t * 0.9f)).coerceIn(0f, 1f)

            if (p.isStar) {
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size * (1f - t * 0.4f),
                    center = Offset(curX, curY)
                )
            } else {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(curX, curY),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}

