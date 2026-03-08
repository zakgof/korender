package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.FloatMath.PIdiv2
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun TextureArrayExample() {
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val objMeshDeferred = loadMesh(obj("model/head.obj"))
        val cubeMeshDeferred = loadMesh(cube(1f))
        Frame {
            if (objMeshDeferred.isCompleted && cubeMeshDeferred.isCompleted) {
                val objMesh = objMeshDeferred.getCompleted()
                val cubeMesh = cubeMeshDeferred.getCompleted()
                Renderable(
                    base(),
                    colorTextures(textureArray("model/head.jpg", "texture/asphalt-albedo.jpg")),
                    mesh = customMesh(
                        "combined",
                        objMesh.vertices.size + cubeMesh.vertices.size,
                        objMesh.indices!!.size + cubeMesh.indices!!.size,
                        POS, NORMAL, TEX, COLORTEXINDEX,
                        dynamic = false
                    ) {
                        embed(objMesh, scale(2f).rotate(1.y, -PIdiv2).translate(-2.x), colorTexIndex = 0)
                        embed(cubeMesh, translate(2.x), colorTexIndex = 1)
                    }
                )
                Gui {
                    Column {
                        Filler()
                        Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                    }
                }
            }
        }
    }
}

