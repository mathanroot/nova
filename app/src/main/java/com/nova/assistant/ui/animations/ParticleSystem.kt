package com.nova.assistant.ui.animations

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

data class Particle(
    var x: Float,
    var y: Float,
    var radius: Float,
    var alpha: Float,
    var speed: Float,
    var angle: Double,
    var color: Color
)

class ParticleField(private val count: Int = 24) {
    private val random = Random(42)
    val particles = ArrayList<Particle>()

    fun init(width: Float, height: Float, colors: List<Color>) {
        particles.clear()
        for (i in 0 until count) {
            val angle = random.nextDouble() * 2 * Math.PI
            val dist = 80f + random.nextFloat() * 140f
            particles.add(
                Particle(
                    x = width / 2 + (cos(angle) * dist).toFloat(),
                    y = height / 2 + (sin(angle) * dist).toFloat(),
                    radius = 1.5f + random.nextFloat() * 2.5f,
                    alpha = 0.2f + random.nextFloat() * 0.6f,
                    speed = 0.5f + random.nextFloat() * 1.2f,
                    angle = angle,
                    color = colors[random.nextInt(colors.size)]
                )
            )
        }
    }

    fun update(centerX: Float, centerY: Float, delta: Float) {
        for (p in particles) {
            p.angle += (0.015 * p.speed) * delta
            val radius = 90f + (sin(p.angle * 2).toFloat() * 35f)
            p.x = centerX + (cos(p.angle) * radius).toFloat()
            p.y = centerY + (sin(p.angle) * radius).toFloat()
        }
    }
}
