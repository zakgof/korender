package city.controller

import city.ChaseCamera
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.KeyEvent
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.Quaternion.Companion.fromAxisAngle
import com.zakgof.korender.math.Quaternion.Companion.lookAt
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

class Controller {

    private var forward = 0f
    private var rota = 0f

    val character = Character()
    val chaseCamera = ChaseCamera()

    fun camera(fc: FrameContext): CameraDeclaration =
        chaseCamera.camera(character.position, character.direction, fc)

    fun touch(touchEvent: TouchEvent) =
        chaseCamera.touch(touchEvent)

    fun key(keyEvent: KeyEvent) {
        if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.key == "w")
            forward = 1f
        if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.key == "s")
            forward = -1f
        if (keyEvent.type == KeyEvent.Type.UP && keyEvent.key == "w")
            forward = 0f
        if (keyEvent.type == KeyEvent.Type.UP && keyEvent.key == "s")
            forward = 0f
        if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.key == "a")
            rota = 1f
        if (keyEvent.type == KeyEvent.Type.UP && keyEvent.key == "a")
            rota = 0f
        if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.key == "d")
            rota = -1f
        if (keyEvent.type == KeyEvent.Type.UP && keyEvent.key == "d")
            rota = 0f
    }

    fun update(dt: Float) {
        character.position += character.direction * (forward * dt) * 0.3f
        character.direction = (fromAxisAngle(1.y, rota * dt * 0.4f) * character.direction).normalize()
    }

    class Character {
        var position = Vec3(3.2f, 0f, -102f)
        var direction = 1.z
        val transform: Transform
            get() = rotate(lookAt(direction, 1.y)).translate(position)
    }
}