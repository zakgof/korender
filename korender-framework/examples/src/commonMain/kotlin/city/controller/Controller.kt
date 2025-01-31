package com.zakgof.korender.examples.city.controller

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.KeyEvent
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.city.ChaseCamera
import com.zakgof.korender.math.Quaternion.Companion.fromAxisAngle
import com.zakgof.korender.math.Quaternion.Companion.lookAt
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

typealias HeightField = (Float, Float) -> Float

fun HeightField.land(x: Float, z: Float) = Vec3(x, this(x, z), z)
fun HeightField.land(vec: Vec3) = Vec3(vec.x, this(vec.x, vec.z), vec.z)

class Controller {

    val heightField: HeightField = { xx, zz ->
        1.0f + 0.0f *
                (1.0f - xx * xx / (196f * 196f)) *
                (1.0f - zz * zz / (196f * 196f))
    }
    val character = Character()

    private var forward = 0f
    private var rota = 0f
    private val chaseCamera = ChaseCamera(character)

    fun camera(fc: FrameContext): CameraDeclaration =
        chaseCamera.camera(fc)

    fun joystick(offset: Pair<Float, Float>, dt: Float) {
        character.position = heightField.land(character.position + character.direction * (-offset.second * dt) * 10.3f)
        character.direction = (fromAxisAngle(1.y, -offset.first * dt * 2.4f) * character.direction).normalize()
    }

    fun touch(touchEvent: TouchEvent) =
        chaseCamera.touch(touchEvent)

    fun key(keyEvent: KeyEvent) {

    }

    fun update(dt: Float) {
        character.position = heightField.land(character.position + character.direction * (forward * dt) * 10.3f)
        character.direction = (fromAxisAngle(1.y, rota * dt * 2.4f) * character.direction).normalize()
    }

    inner class Character {
        var position = heightField.land(3.2f, -102f)
        var direction = 1.z
        val transform: Transform
            get() = rotate(lookAt(direction, 1.y)).translate(position)
    }

}

