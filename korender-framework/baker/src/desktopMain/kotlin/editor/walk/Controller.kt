package com.zakgof.korender.baker.editor.walk

import androidx.compose.ui.input.key.Key
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.KeyEvent
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

class Controller(bvhBytes: ByteArray) {


    private val collider = Collider(bvhBytes)

    private var player: Vec3 = Vec3(0f, 1.0f, 0f)
    private var up: Vec3 = 1.y
    private var look: Vec3 = 1.z
    private val horzLook
        get() = (look - (look.y).y).normalize()
    private var keys = mutableSetOf<Key>()
    private var touchDown: Vec3? = null

    fun key(keyEvent: KeyEvent) {
        if (keyEvent.type == KeyEvent.Type.DOWN) {
            keys += keyEvent.composeKey
        }
        if (keyEvent.type == KeyEvent.Type.UP) {
            keys -= keyEvent.composeKey
        }
    }

    context(kc: KorenderContext)
    fun touch(touchEvent: TouchEvent) {
        if (touchEvent.type == TouchEvent.Type.DOWN) {
            touchDown = screenToLook(touchEvent)
        }
        if (touchEvent.type == TouchEvent.Type.MOVE && touchDown != null) {
            val newLook = screenToLook(touchEvent)
            val rot = Quaternion.shortestArc(newLook, touchDown!!)
            look = (rot * look).normalize()
            up = (rot * up).normalize()
        }
        if (touchEvent.type == TouchEvent.Type.UP) {
            touchDown = null
        }
    }

    context(kc: KorenderContext)
    fun camera(): CameraDeclaration {
        return kc.camera(player - look * 2f + up * 1f, look, up)
    }

    fun player(): Transform {
        return scale(0.01f)
            .translate(-1.0f.y)
            .rotate(-horzLook, 1.y)
            .translate(player)
    }

    fun update(dt: Float, time: Float) {
        val v = 3.0f
        val omega = 4.0f
        when {
            keys.contains(Key.DirectionLeft) -> look = (Quaternion.fromAxisAngle(1.y, dt * omega) * look).normalize()
            keys.contains(Key.DirectionRight) -> look = (Quaternion.fromAxisAngle(1.y, -dt * omega) * look).normalize()
        }

        var delta = Vec3.ZERO
        if (keys.contains(Key.W)) delta += look * dt * v
        if (keys.contains(Key.S)) delta -= look * dt * v
        if (keys.contains(Key.D)) delta += (look cross up) * dt * v
        if (keys.contains(Key.A)) delta -= (look cross up) * dt * v

        val hit = collider.test(player, delta, Vec3(0.3f, 1.75f, 0.3f))
        player += (hit?.let { delta * it.t - delta.normalize() * 0.1f } ?: delta)

    }

    context(kc: KorenderContext)
    private fun screenToLook(e: TouchEvent): Vec3 {
        val right = kc.camera.direction.cross(kc.camera.up)

        val nx = (e.x + 0.5f) / kc.width * 2f - 1f
        val ny = 1f - (e.y + 0.5f) / kc.height * 2f

        return (kc.camera.direction * kc.projection.near +
                right * (nx * kc.projection.width * 0.5f) +
                kc.camera.up * (ny * kc.projection.height * 0.5f)
                ).normalize()
    }


}