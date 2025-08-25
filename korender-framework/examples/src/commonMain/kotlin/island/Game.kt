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
        }
    }

    val cameraUp: Vec3
        get() = plane.up
    val cameraDir: Vec3
        get() = plane.look
    val cameraPos: Vec3
        get() = plane.position - plane.look * 240f + plane.up * 30f

    var plane: Plane = Plane(0.y, 0.y, 0.y, 0.y)

    fun frame(dt: Float) {
        if (!started) {
            val rw = loader.runwaySeedLoading.getCompleted()
            val rw1 = heightFunc.texToWorld(rw.first, 32.0f)
            val rw2 = heightFunc.texToWorld(rw.second, 32.0f)
            val look = (rw2 - rw1).normalize()
            val up = 1.y
            val position = rw1 + look * (rw2 - rw1).length() * 0.1f
            plane = Plane(position, look, up, 0.y)
            started = true
        }
        plane.update(dt)
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

    fun update(dt: Float) {
        velocity = velocity + look * (throttle * 30.0f * dt) - velocity * (0.01f * dt)
        position = position + velocity * dt
    }
}