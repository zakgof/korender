package com.zakgof.korender.baker.editor.walk

import androidx.compose.ui.input.key.Key
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.KeyEvent
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
    private var look: Vec3 = 1.z
    private var keys = mutableSetOf<Key>()

    fun key(keyEvent: KeyEvent) {
        if (keyEvent.type == KeyEvent.Type.DOWN) {
            keys += keyEvent.composeKey
        }
        if (keyEvent.type == KeyEvent.Type.UP) {
            keys -= keyEvent.composeKey
        }
    }

    context(kc: KorenderContext)
    fun camera(): CameraDeclaration {
        return kc.camera(player - look * 3f, look, 1.y)
    }

    fun player(): Transform {
        return scale(0.01f)
            .translate(-1.0f.y)
            .rotate(-look, 1.y)
            .translate(player)
    }

    fun update(dt: Float, time: Float) {
        val v = 3.0f
        val omega = 4.0f
        when {
            keys.contains(Key.DirectionLeft) -> look = (Quaternion.fromAxisAngle(1.y, dt * omega) * look).normalize()
            keys.contains(Key.DirectionRight) -> look = (Quaternion.fromAxisAngle(1.y, -dt * omega) * look).normalize()
        }

        val delta = when {
            keys.contains(Key.W) -> look * dt * v
            keys.contains(Key.S) -> -look * dt * v
            keys.contains(Key.D) -> (look cross 1.y) * dt * v
            keys.contains(Key.A) -> -(look cross 1.y) * dt * v
            else -> null
        }

        delta?.let {
            val hit = collider.test(player, delta, Vec3(0.3f, 1.75f, 0.3f))
            player += (hit?.let { delta * it.t - delta.normalize() * 0.1f } ?: delta)
        }

    }
}