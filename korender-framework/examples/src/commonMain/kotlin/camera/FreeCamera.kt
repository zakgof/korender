package com.zakgof.korender.examples.camera

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.KeyEvent
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y

class FreeCamera(private val context: KorenderContext, initialPosition: Vec3, initialDirection: Vec3, private val velocity: Float = 3.0f) {

    private var deltaX: Float = 0f
    private var deltaY: Float = 0f

    private var startEvent: TouchEvent? = null
    private var startDirection: Vec3? = null
    private var direction: Vec3 = initialDirection
    private var position: Vec3 = initialPosition

    private var thrust = 0.0f

    fun camera(projection: ProjectionDeclaration, width: Int, height: Int, dt: Float): CameraDeclaration {

        if (startDirection != null) {
            val startRight = (startDirection!! % 1.y).normalize()
            val startUp = (startRight % startDirection!!).normalize()
            val frustum =  projection // TODO !!!
            direction = (startDirection!! +
                    startRight * (-deltaX / width * frustum.width / frustum.near) +
                    startUp * (deltaY / height * frustum.height / frustum.near)).normalize()
        }
        val right = (direction % 1.y).normalize()
        val up = (right % direction).normalize()

        position += direction * (velocity * dt * thrust)

        return context.camera(position, direction, up)
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

    fun handle(keyEvent: KeyEvent) {
        if (keyEvent.key == "W" && keyEvent.type == KeyEvent.Type.DOWN) {
            thrust = 1.0f
        }
        if (keyEvent.key == "W" && keyEvent.type == KeyEvent.Type.UP) {
            thrust = 0.0f
        }
        if (keyEvent.key == "S" && keyEvent.type == KeyEvent.Type.DOWN) {
            thrust = -1.0f
        }
        if (keyEvent.key == "S" && keyEvent.type == KeyEvent.Type.UP) {
            thrust = 0.0f
        }
    }

}
