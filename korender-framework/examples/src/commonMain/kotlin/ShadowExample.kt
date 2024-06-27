package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.material.MaterialModifiers.options
import com.zakgof.korender.material.MaterialModifiers.standardUniforms
import com.zakgof.korender.mesh.Meshes.cube
import com.zakgof.korender.mesh.Meshes.sphere
import com.zakgof.korender.material.StandardMaterialOption
import com.zakgof.korender.material.Textures.texture
import com.zakgof.korender.math.FloatMath
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun ShadowExample() =
    Korender {
        val pcss = options(StandardMaterialOption.Pcss)
        val uniforms = standardUniforms {
            colorTexture = texture("/sand.jpg")
        }
        Frame {
            Light(Vec3(1f, -1f, 1f).normalize())
            Camera(DefaultCamera(Vec3(-2.0f, 3f, 20f), -1.z, 1.y))
            Shadow {
                Cascade(mapSize = 1024, near = 5.0f, far = 15.0f)
            }
            Renderable(
                pcss, uniforms,
                mesh = cube(1f),
                transform = Transform().scale(8f, 1f, 8f)
            )
            Renderable(
                pcss, uniforms,
                mesh = cube(1.0f),
                transform = Transform().translate(2.y).rotate(1.y, frameInfo.time * 0.1f),
            )
            Renderable(
                pcss, uniforms,
                mesh = sphere(1.5f),
                transform = Transform().translate(Vec3(-5.0f, 3.5f + FloatMath.sin(frameInfo.time), 0.0f)),
            )
        }
    }