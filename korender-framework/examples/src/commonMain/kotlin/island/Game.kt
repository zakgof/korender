package com.zakgof.korender.examples.island

import com.zakgof.korender.KeyEvent
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class Game(private val loader: Loader) {

    var started = false
    val heightFunc = loader.heightFunc

    fun touch(event: TouchEvent) {

    }

    fun key(event: KeyEvent) {
        when {
            (event.type == KeyEvent.Type.DOWN && event.key == "W") -> {
                plane.throttle = 1.0f
            }

            (event.type == KeyEvent.Type.UP && event.key == "W") -> {
                plane.throttle = 0.0f
            }

            (event.type == KeyEvent.Type.DOWN && event.key == "S") -> {
                plane.throttle = -1.0f
            }

            (event.type == KeyEvent.Type.UP && event.key == "S") -> {
                plane.throttle = 0.0f
            }

            (event.type == KeyEvent.Type.DOWN && event.key == "D") -> {
                plane.rolling = 1.0f
            }

            (event.type == KeyEvent.Type.UP && event.key == "D") -> {
                plane.rolling = 0.0f
            }

            (event.type == KeyEvent.Type.DOWN && event.key == "A") -> {
                plane.rolling = -1.0f
            }

            (event.type == KeyEvent.Type.UP && event.key == "A") -> {
                plane.rolling = 0.0f
            }

            (event.type == KeyEvent.Type.DOWN && event.key == "UP") -> {
                plane.pitching = 1.0f
            }

            (event.type == KeyEvent.Type.UP && event.key == "UP") -> {
                plane.pitching = 0.0f
            }

            (event.type == KeyEvent.Type.DOWN && event.key == "DOWN") -> {
                plane.pitching = -1.0f
            }

            (event.type == KeyEvent.Type.UP && event.key == "DOWN") -> {
                plane.pitching = 0.0f
            }
        }
    }

    val cameraUp: Vec3
        get() = plane.up
    val cameraDir: Vec3
        get() = plane.look
    val cameraPos: Vec3
        get() = plane.position - cameraDir * 240f + cameraUp * 64f // TODO: above surface

    var plane: Plane = Plane(0.y, 0.y, 0.y, 0.y)

    fun frame(dt: Float) {
        if (!started) {
            val rw = loader.runwaySeedLoading.getCompleted()
            val rw1 = heightFunc.texToWorld(rw.first, 5.0f)
            val rw2 = heightFunc.texToWorld(rw.second, 5.0f)
            val look = (rw2 - rw1).normalize()
            val up = 1.y
            val position = rw1 + look * (rw2 - rw1).length() * 0.1f
            plane = Plane(position, look, up, 0.y)
            started = true
        }
        if (dt > 0.5f) return
        plane.update(dt)
        adjustLandedPlane(dt)
    }

    private fun adjustLandedPlane(dt: Float) {
        val alt = heightFunc.altitute(plane.position)
        val normal = heightFunc.normal(plane.position)

        println(">>>> Height $alt    dot: ${(plane.up * normal)}")

        if (alt < 0.1f && alt > -16.0f && (plane.up * normal) > 0.9f) {
            plane.position += -alt.y
            plane.velocity -= plane.velocity.y.y

            val right = plane.look % plane.up
            plane.up = normal
            plane.look = (plane.up % right).normalize()

            println(">>>> Landed ${plane.position} - UP ${plane.up}")
        }
    }
}

class Plane(
    var position: Vec3,
    var look: Vec3,
    var up: Vec3,
    var velocity: Vec3
) {
    var throttle: Float = 0f
    var rolling: Float = 0f
    var pitching: Float = 0f

    fun update(dt: Float) {

        val right = look % up
        look = (look + up * pitching * dt).normalize()

        val stallFactor = (1000f - (velocity * look) * (velocity * look)).coerceIn(0f, 100f)
        println(">>>> DT $dt STALL $stallFactor")
        look = (look - stallFactor.y * dt * 0.01f).normalize()
        up = (right % look).normalize()

        velocity = velocity + look * (throttle * 50.0f * dt) - velocity * (0.1f * dt) - 1.y * (9.0f * dt)
        position = position + velocity * dt
    }
}