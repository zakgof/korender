package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.MeshDeclarations.cube
import com.zakgof.korender.material.Materials.standard
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun InstancedMeshesExample() = Korender {

    onResize = {
        projection =
            FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    Scene {
        InstancedRenderables(
            id = "particles",
            count = 21 * 21,
            mesh = cube(0.4f),
            material = standard {
                colorFile = "/sand.jpg"
            }
        ) {
            for (x in -10..10) {
                for (y in -10..10) {
                    Instance(
                        transform = Transform().translate(Vec3(x.toFloat(), y.toFloat(), 0f))
                    )
                }
            }
        }
    }
}