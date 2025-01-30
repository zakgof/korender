package com.zakgof.korender.examples.city
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.city.controller.Controller
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

class ChaseCamera(private val character: Controller.Character) {

    private val distance = 0.8f
    private var deltaY: Float = 0f

    private var dragStartEvent: TouchEvent? = null
    private var dragStartCharacterDirection: Vec3? = null

    fun camera(fc: FrameContext): CameraDeclaration {

        if (dragStartEvent == null) {
            deltaY *= exp(fc.frameInfo.dt * -3.0f)
        }

        val cameraPos = character.position + character.direction * (-distance) + 0.2f.y + 0.005f.y * deltaY
        val cameraDir = (character.position + 0.2f.y - cameraPos).normalize()
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
            deltaY = touchEvent.y - dragStartEvent!!.y
            deltaY = min(deltaY, 100.0f)
            deltaY = max(deltaY, -30.0f)
        }
    }

}
