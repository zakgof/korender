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
    Korender(resourceLoader = { Res.readBytes("files/$it") }) {
        val objMeshDeferred = loadMesh(obj("model/head.obj"))
        val cubeMeshDeferred = loadMesh(cube(1f))
        Frame {
            if (objMeshDeferred.isCompleted && cubeMeshDeferred.isCompleted) {
                val objMesh = objMeshDeferred.getCompleted()
                val cubeMesh = cubeMeshDeferred.getCompleted()
                Renderable(
                    base {
                        colorTextures = textureArray(
                            "model/head.jpg",
                            "texture/asphalt-albedo.jpg",
                            "texture/grass.jpg"
                        )
                    },
                    mesh = compositeMesh(
                        "combined",
                        listOf(
                            objMesh to 2,
                            cubeMesh to 3
                        ),
                        POS, NORMAL, TEX, MODEL0, MODEL1, MODEL2, MODEL3, INSTCOLORTEXINDEX,
                        dynamic = false
                    ) {
                        Instance(
                            transform = scale(2f).rotate(1.y, -PIdiv2).translate(-4.x),
                            colorTextureIndex = 0
                        )
                        Instance(
                            transform = scale(2f).rotate(1.y, -PIdiv2).translate(-2.x),
                            colorTextureIndex = 0
                        )
                        Instance(
                            transform = translate(2.x),
                            colorTextureIndex = 1
                        )
                        Instance(
                            transform = translate(2.x + 2.y),
                            colorTextureIndex = 2
                        )
                        Instance(
                            transform = translate(2.x - 2.y),
                            colorTextureIndex = 2
                        )
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


