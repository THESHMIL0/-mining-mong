package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.example.ui.viewmodel.ParticleEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlin.random.Random

private data class ActiveParticle(
    val id: Long,
    val x: Float,
    val y: Float,
    val text: String,
    val color: Color,
    val progress: Animatable<Float, *> = Animatable(0f)
)

@Composable
fun ParticlesOverlay(
    particleEvents: SharedFlow<ParticleEvent>,
    modifier: Modifier = Modifier,
    particleQuality: String = "HIGH"
) {
    val particles = remember { mutableStateListOf<ActiveParticle>() }

    LaunchedEffect(particleEvents) {
        particleEvents.collect { event ->
            if (particleQuality == "LOW" && particles.size > 5) return@collect

            val id = System.nanoTime()
            val colors = listOf(Color(0xFFFFD700), Color(0xFFFFA000), Color(0xFFFFF59D))
            val p = ActiveParticle(
                id = id,
                x = event.x + Random.nextInt(-20, 20),
                y = event.y + Random.nextInt(-20, 20),
                text = event.amountText,
                color = colors.random()
            )
            particles.add(p)

            p.progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = LinearEasing)
            )
            particles.remove(p)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val paint = android.graphics.Paint().apply {
            textSize = 38f
            isFakeBoldText = true
            setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
        }

        particles.forEach { p ->
            val alpha = (1f - p.progress.value).coerceIn(0f, 1f)
            val currentY = p.y - (p.progress.value * 120f)
            val currentX = p.x + (Random.nextInt(-10, 10) * p.progress.value)

            // Spark circle
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = 8f * (1f - p.progress.value),
                center = Offset(currentX - 15f, currentY)
            )

            // Floating Text
            paint.color = android.graphics.Color.argb(
                (alpha * 255).toInt(),
                (p.color.red * 255).toInt(),
                (p.color.green * 255).toInt(),
                (p.color.blue * 255).toInt()
            )

            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    p.text,
                    currentX,
                    currentY,
                    paint
                )
            }
        }
    }
}
