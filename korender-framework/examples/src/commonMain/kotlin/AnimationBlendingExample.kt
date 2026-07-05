package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.AnimationDeclaration
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.onClick

@Composable
fun AnimationBlendingExample() = Korender(resourceLoader = { Res.readBytes("files/$it") }) {

    var currentAnimation = 0
    var previousAnimation = 0
    var transitionTimestamp = 0f

    val orbitCamera = OrbitCamera(7.z, Vec3.ZERO)
    OnTouch { orbitCamera.touch(it) }
    Frame {

        fun setAnimation(animation: Int) {
            if (animation != currentAnimation) {
                previousAnimation = currentAnimation
                currentAnimation = animation
                transitionTimestamp = frameInfo.time
            }
        }

        fun calculateAnimation(): AnimationDeclaration =
            if (frameInfo.time - transitionTimestamp > 3f) {
                animation(currentAnimation)
            } else {
                blendedAnimation(previousAnimation, currentAnimation, (frameInfo.time - transitionTimestamp) / 3f)
            }


        TestExchange.report(frameInfo)
        camera = orbitCamera.run { camera() }
        projection = projection(5f * width/height, 5f, 5f, 100f, frustum())
        AmbientLight(white(0.6f))
        DirectionalLight(Vec3(1.0f, -1.0f, -1.0f), white(3f))
        Model(
            resource = "infcity/swat-woman.glb",
            animation = calculateAnimation(),
            transform = scale(0.02f).translate((-1.8f).y)
        )
        Gui {
            Column {
                Row {
                    Filler()
                    Column (padding = 12f){
                        Text("walk", "HANDS DOWN", onTouch = { onClick(it) { setAnimation(0) }})
                        Text("carry", "HANDS UP", onTouch = { onClick(it) { setAnimation(2) }})
                    }
                }
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}
