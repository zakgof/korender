
import com.zakgof.korender.Fonts
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection

fun main(): Unit = korender(LwjglPlatform()) {

    camera = DefaultCamera(pos = 20.z, dir = -1.z, up = 1.y)
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val font = Fonts.load(gpu, "/ubuntu.ttf")
    val text = font.renderable("This is some text", Color(0x30F0FF),32.0f / height, 0.0f, (height - 32.0f) / height)
    add(text)
}