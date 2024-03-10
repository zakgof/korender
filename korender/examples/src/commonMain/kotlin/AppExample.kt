package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.SimpleRenderable
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.material.Materials
import com.zakgof.korender.math.FloatMath
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun AppExample() {
    Korender() {
        camera = DefaultCamera(Vec3(-2.0f, 3f, 20f), -1.z, 1.y)
        light = Vec3(1f,-1f,1f).normalize()
        onResize = {
            projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
        }

        val plate = Meshes.cube(1f) {
            transformPos(Transform().scale(8f, 1f, 5f).translate(-1.6f.y)) // TODO: vertex transformation
        }.build(gpu)
        val material = Materials.standard(gpu, "SHADOW_RECEIVER", "PCSS") {
            colorFile = "/sand.jpg"
        }

        val rcube = SimpleRenderable(Meshes.cube(1.5f).build(gpu), material)
        val rsphere = SimpleRenderable(Meshes.sphere(1.5f).build(gpu), material)
        val rplate = SimpleRenderable(plate, material)

        shadower.add(rcube)
        shadower.add(rsphere)
        add(rcube)
        add(rsphere)
        add(rplate)

        onFrame = { frameInfo ->
            rcube.transform = Transform().rotate(1.x, -FloatMath.PIdiv2).rotate(1.y, frameInfo.time * 0.1f)
            rsphere.transform = Transform().translate(Vec3(-4.0f, 2.0f + sin(frameInfo.time), 0.0f));
            println("FPS=~${frameInfo.avgFps}")
        }

    }
}