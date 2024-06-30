package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.material.MaterialModifiers.fragment
import com.zakgof.korender.material.MaterialModifiers.standartUniforms
import com.zakgof.korender.material.Textures.texture
import com.zakgof.korender.math.Color
import com.zakgof.korender.mesh.Meshes.sphere

@Composable
fun FilterExample() = Korender {
    Frame {
        Pass {
            Renderable(
                standartUniforms {
                    colorTexture = texture("/sand.jpg")
                },
                mesh = sphere(2.2f),
            )
        }
        Pass {
            Screen(fragment("bw.frag"))
            Gui {
                Filler()
                Text(id = "fps", fontResource = "/ubuntu.ttf", height = 50, text = "FPS ${frameInfo.avgFps}", color = Color(0xFF66FF55))
            }
        }
    }
}