package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.material.MaterialModifiers.options
import com.zakgof.korender.material.MaterialModifiers.standartUniforms
import com.zakgof.korender.mesh.Meshes.customMesh
import com.zakgof.korender.material.StandartMaterialOption
import com.zakgof.korender.material.Textures.texture
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.mesh.Attributes.NORMAL
import com.zakgof.korender.mesh.Attributes.POS
import com.zakgof.korender.mesh.Attributes.TEX
import com.zakgof.korender.mesh.Vertex
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun MeshesExample() {
    val orbitCamera = OrbitCamera(20.z, 2.y)
    Korender {
        OnTouch { orbitCamera.touch(it) }
        val flags = options(StandartMaterialOption.NoLight)
        val uniforms = standartUniforms {
            colorTexture = texture("/sand.jpg")
        }
        Frame {
            Camera(orbitCamera.camera(projection, width, height))
            Renderable(
                flags, uniforms,
                mesh = customMesh(id = "static", static = true, vertexCount = 3, indexCount = 3, POS, NORMAL, TEX) {
                    vertex(Vertex(pos = Vec3(-5f, 0f, 0f), normal = 1.z, tex = Vec2(0f, 0f)))
                    vertex(Vertex(pos = Vec3(0f, 0f, 0f), normal = 1.z, tex = Vec2(1f, 0f)))
                    vertex(Vertex(pos = Vec3(0f, 5f, 0f), normal = 1.z, tex = Vec2(1f, 1f)))
                    indices(0, 1, 2)
                }
            )
            Renderable(
                flags, uniforms,
                mesh = customMesh(id = "dynamic", static = false, vertexCount = 3, indexCount = 3, POS, NORMAL, TEX) {
                    vertex(Vertex(pos = Vec3(1f, 0f, 0f), normal = 1.z, tex = Vec2(0f, 0f)))
                    vertex(Vertex(pos = Vec3(5f, 0f, 0f), normal = 1.z, tex = Vec2(1f, 0f)))
                    vertex(Vertex(pos = Vec3(5f, 5f + sin(frameInfo.time), 0f), normal = 1.z, tex = Vec2(1f, 1f)))
                    indices(0, 1, 2)
                }
            )
        }
    }

}