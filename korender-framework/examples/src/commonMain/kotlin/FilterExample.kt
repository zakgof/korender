package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.material.MaterialModifiers.fragment
import com.zakgof.korender.material.MaterialModifiers.standardUniforms
import com.zakgof.korender.mesh.Meshes.sphere
import com.zakgof.korender.material.Textures.texture
import com.zakgof.korender.math.Color

@Composable
fun FilterExample() = Korender {
    Frame {
        Pass {
            Renderable(
                standardUniforms {
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