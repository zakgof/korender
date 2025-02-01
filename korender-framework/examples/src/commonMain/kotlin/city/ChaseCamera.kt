package com.zakgof.korender.examples.city

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.city.controller.Controller
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import kotlin.math.exp

class ChaseCamera(private val character: Controller.Character) {

    private val distance = 0.8f
    private var deltaY: Float = 0f

    private var dragStartEvent: TouchEvent? = null
    private var dragStartCharacterDirection: Vec3? = null

    fun camera(fc: FrameContext): CameraDeclaration {

        if (dragStartEvent == null) {
            deltaY *= exp(fc.frameInfo.dt * -3.0f)
        }

        val cameraPos =
            if (character.aiming) {
                val right = (character.direction % 1.y).normalize()
                character.position + character.direction * (-0.5f) + 0.3f.y + 0.005f.y * deltaY + right * 0.1f
            } else {
                character.position + character.direction * (-distance) + 0.2f.y + 0.005f.y * deltaY
            }
        val cameraDir = character.direction
        val cameraRight = cameraDir % 1.y
        val cameraUp = cameraRight % cameraDir
        return fc.camera(cameraPos, cameraDir, cameraUp)
    }

    fun touch(touchEvent: TouchEvent) {
        if (touchEvent.type == TouchEvent.Type.DOWN) {
            dragStartEvent = touchEvent
            dragStartCharacterDirection = character.direction
        }
        if (touchEvent.type == TouchEvent.Type.UP) {
            dragStartEvent = null
        }
        if (touchEvent.type == TouchEvent.Type.MOVE && dragStartEvent != null) {
            val deltaX = touchEvent.x - dragStartEvent!!.x
            character.direction = Quaternion.fromAxisAngle(1.y, -deltaX * 0.005f) * dragStartCharacterDirection!!
            deltaY = (touchEvent.y - dragStartEvent!!.y).coerceIn(-30f, 100f)
        }
    }
}
