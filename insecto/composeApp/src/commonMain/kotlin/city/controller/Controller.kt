package city.controller

import com.zakgof.korender.KeyEvent
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.z

class Controller {

    private var forward = 0f
    val character = Character()

    fun touch(touchEvent: TouchEvent) {

    }

    fun key(keyEvent: KeyEvent) {
        if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.key == "w")
            forward = 1f
        if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.key == "s")
            forward = -1f
        if (keyEvent.type == KeyEvent.Type.UP && keyEvent.key == "w")
            forward = 0f
        if (keyEvent.type == KeyEvent.Type.UP && keyEvent.key == "s")
            forward = 0f
    }

    fun update(dt: Float) {

    }

    class Character {
        private val position = Vec3(3.2f, 0f, -102f)
        private val direction = 1.z
    }
}