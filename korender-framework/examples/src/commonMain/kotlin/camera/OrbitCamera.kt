package com.zakgof.korender.examples.camera

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y

class OrbitCamera(initialPosition: Vec3, private val targetPosition: Vec3 = Vec3.ZERO) {

    private var deltaX: Float = 0f
    private var deltaY: Float = 0f

    private var startEvent: TouchEvent? = null
    private var startPosition: Vec3? = null
    private var position: Vec3 = initialPosition

    private val r = (targetPosition - initialPosition).length()

    fun KorenderContext.camera(): CameraDeclaration {
        if (startPosition != null) {
            val startDirection = (targetPosition - startPosition!!).normalize()
            val startRight = (startDirection % 1.y).normalize()
            val startUp = (startRight % startDirection).normalize()
            val frustum =  projection

            position = startPosition!! +
                    startRight * (-deltaX / width * frustum.width * 8.0f) +
                    startUp * (deltaY / height * frustum.height * 8.0f)

            position = targetPosition + (position - targetPosition).normalize() * r

        }
        val direction = (targetPosition - position).normalize()
        val right = (direction % 1.y).normalize()
        val up = (right % direction).normalize()
        return camera(position, direction, up)
    }

    fun touch(touchEvent: TouchEvent) {
        if (touchEvent.type == TouchEvent.Type.DOWN) {
            startEvent = touchEvent
            startPosition = position
        }
        if (touchEvent.type == TouchEvent.Type.UP) {
            startEvent = null
            startPosition = null
            deltaX = 0f
            deltaY = 0f
        }
        if (touchEvent.type == TouchEvent.Type.MOVE && startEvent != null) {
            deltaX = touchEvent.x - startEvent!!.x
            deltaY = touchEvent.y - startEvent!!.y
        }

    }

}
