import com.zakgof.korender.SimpleRenderable
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials

fun main() = korender(LwjglPlatform()) {
    add(SimpleRenderable(
        mesh = Meshes.sphere(2f).build(gpu),
        material = Materials.standard(gpu) {
            colorFile = "/sand.png"
        }
    ))
}