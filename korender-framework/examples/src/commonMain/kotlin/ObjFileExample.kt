package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.material.Materials.standard
import com.zakgof.korender.declaration.MeshDeclarations.obj
import com.zakgof.korender.math.FloatMath.PIdiv2
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun ObjFileExample() = Korender {

    camera = DefaultCamera(pos = Vec3(0f, 2f, 20f), dir = -1.z, up = 1.y)
    onResize = {
        projection =
            FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    Scene {
        Renderable(
            mesh = obj("/cat-red.obj"),
            material = standard {
                colorFile = "/cat-red.jpg"
                ambient = 1.0f
                diffuse = 1.0f
                specular = 0.0f
            },
            transform = Transform().scale(0.1f).rotate(1.x, -PIdiv2).rotate(1.y, frameInfo.time * 0.3f)
        )
    }
}