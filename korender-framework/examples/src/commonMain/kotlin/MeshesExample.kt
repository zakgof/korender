package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.Materials.standard
import com.zakgof.korender.declaration.Meshes.mesh
import com.zakgof.korender.declaration.Textures.texture
import com.zakgof.korender.impl.geometry.Attributes.NORMAL
import com.zakgof.korender.impl.geometry.Attributes.POS
import com.zakgof.korender.impl.geometry.Attributes.TEX
import com.zakgof.korender.impl.geometry.Vertex
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
        val material = standard {
            colorTexture = texture("/sand.jpg")
        }
        Scene {
            Camera(orbitCamera.camera(projection, width, height))
            Renderable(
                mesh = mesh(id = "static", static = true, vertexCount = 3, indexCount = 3, POS, NORMAL, TEX) {
                    vertex(Vertex(pos = Vec3(-5f, 0f, 0f), normal = 1.z, tex = Vec2(0f, 0f)))
                    vertex(Vertex(pos = Vec3(0f, 0f, 0f), normal = 1.z, tex = Vec2(1f, 0f)))
                    vertex(Vertex(pos = Vec3(0f, 5f, 0f), normal = 1.z, tex = Vec2(1f, 1f)))
                    indices(0, 1, 2)
                },
                material = material
            )
            Renderable(
                mesh = mesh(id = "dynamic", static = false, vertexCount = 3, indexCount = 3, POS, NORMAL, TEX) {
                    vertex(Vertex(pos = Vec3(1f, 0f, 0f), normal = 1.z, tex = Vec2(0f, 0f)))
                    vertex(Vertex(pos = Vec3(5f, 0f, 0f), normal = 1.z, tex = Vec2(1f, 0f)))
                    vertex(Vertex(pos = Vec3(5f, 5f + sin(frameInfo.time), 0f), normal = 1.z, tex = Vec2(1f, 1f)))
                    indices(0, 1, 2)
                },
                material = material
            )
            Billboard(position = Vec3(0f, -1f, 0f), defs = arrayOf("NO_LIGHT")) {
                colorTexture = texture("/oak.png")
                xscale = 2.0f
                yscale = 2.0f
                rotation = frameInfo.time
            }
        }
    }

}