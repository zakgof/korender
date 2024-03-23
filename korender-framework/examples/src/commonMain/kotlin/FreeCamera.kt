package com.zakgof.korender.examples

import com.zakgof.korender.KorenderContext
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.projection.FrustumProjection

class FreeCamera(initialPosition: Vec3, initialDirection: Vec3, private val velocity: Float = 3.0f) {

    private var deltaX: Float = 0f
    private var deltaY: Float = 0f

    private var startEvent: TouchEvent? = null
    private var startDirection: Vec3? = null
    private var direction: Vec3 = initialDirection
    private var position: Vec3 = initialPosition

    private var thrust = 0.0f

    fun camera(korenderContext: KorenderContext, dt: Float): Camera {

        if (startDirection != null) {
            val startRight = (startDirection!! % 1.y).normalize()
            val startUp = (startRight % startDirection!!).normalize()
            val frustum =  korenderContext.projection as FrustumProjection // TODO !!!
            direction = (startDirection!! +
                    startRight * (-deltaX / korenderContext.width * frustum.width / frustum.near) +
                    startUp * (deltaY / korenderContext.height * frustum.height / frustum.near)).normalize()
        }
        val right = (direction % 1.y).normalize()
        val up = (right % direction).normalize()

        position += direction * (velocity * dt * thrust)

        return DefaultCamera(position, direction, up)
    }

    fun touch(touchEvent: TouchEvent) {
        if (touchEvent.type == TouchEvent.Type.DOWN) {
            startEvent = touchEvent
            startDirection = direction
        }
        if (touchEvent.type == TouchEvent.Type.UP) {
            startEvent = null
            startDirection = null
            deltaX = 0f
            deltaY = 0f
        }
        if (touchEvent.type == TouchEvent.Type.MOVE && startEvent != null) {
            deltaX = touchEvent.x - startEvent!!.x
            deltaY = touchEvent.y - startEvent!!.y
        }

    }

    fun forward(touchEvent: TouchEvent) {
        if (touchEvent.type == TouchEvent.Type.DOWN) {
            thrust = 1.0f
        }
        if (touchEvent.type == TouchEvent.Type.UP) {
            thrust = 0.0f
        }
    }
    fun backward(touchEvent: TouchEvent) {
        if (touchEvent.type == TouchEvent.Type.DOWN) {
            thrust = -1.0f
        }
        if (touchEvent.type == TouchEvent.Type.UP) {
            thrust = 0.0f
        }
    }

}
