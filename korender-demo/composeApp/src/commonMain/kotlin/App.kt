
import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.Materials.standard
import com.zakgof.korender.declaration.Meshes.heightField
import com.zakgof.korender.declaration.Meshes.obj
import com.zakgof.korender.declaration.SceneContext
import com.zakgof.korender.declaration.ShadowContext
import com.zakgof.korender.declaration.Textures.texture
import com.zakgof.korender.image.Images.image
import com.zakgof.korender.impl.gpu.GpuTexture
import com.zakgof.korender.impl.material.Image
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.FloatMath.PIdiv2
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.projection.FrustumProjection
import java.nio.ByteBuffer

@Composable
fun App() = Korender {
    val elevationRatio = 300.0f
    val hfImage = image("/hf-rg16.png")

    val hfImage2 = object : Image {
        override val bytes: ByteBuffer
            get() = TODO("Not yet implemented")
        override val format: GpuTexture.Format
            get() = TODO("Not yet implemented")
        override val height: Int = 2

        override val width: Int = 2

        override fun pixel(x: Int, y: Int): Color {
            return Color(0f, (x+y).toFloat(), 0f)
        }

    }


    val hf = RgImageHeightField(hfImage, 10.0f, elevationRatio)

    val bugPhysics = Physics(hf, Vec3(41f, hf.elevation(41f, -4f), -4f))
    val frozenCamera = FrozenCamera()

    Scene {

        val bugTransform = bugPhysics.update(frameInfo.dt)

        Light(Vec3(0f, -1f, 8f).normalize())
        Projection(FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 10000f))
        Camera(frozenCamera.camera(bugTransform))

        terrain(hfImage, hf, elevationRatio)
        Shadow(mapSize = 1024) {
            bug(bugTransform)
        }
        Sky("fastcloud")
        Filter("atmosphere.frag")
        gui(bugPhysics)
    }
}

private fun SceneContext.gui(bugPhysics: Physics) {
    Gui {
        Row {
            Column {
                Filler()
                Text(id = "coords", text = String.format("Eye: %.1f %.1f %.1f", camera.position.x, camera.position.y, camera.position.z), fontResource = "/ubuntu.ttf", height = 30, color = Color(0xFFFFFF))
                Text(id = "fps", text = String.format("FPS: %.1f", frameInfo.avgFps), fontResource = "/ubuntu.ttf", height = 30, color = Color(0xFFFFFF))
            }
            Filler()
            Column {
                Filler()
                Image(imageResource = "/accelerate.png", width = 128, height = 128, onTouch = { bugPhysics.forward(it) })
                Image(imageResource = "/decelerate.png", width = 128, height = 128, onTouch = { bugPhysics.backward(it) })
            }
        }
    }
}

private fun SceneContext.terrain(hfImage: Image, hf: RgImageHeightField, elevationRatio: Float) {
    Renderable(
        mesh = heightField(id = "terrain",
            cellsX = hfImage.width - 1,
            cellsZ = hfImage.height - 1,
            cellWidth = 10.0f,
            height = { x, y -> hf.pixel(x, y) * elevationRatio }
        ),
        material = standard("DETAIL", "SHADOW_RECEIVER", "PCSS") {
            colorTexture = texture("/terrainbase.jpg")
            detailTexture = texture("/sand.jpg")
            detailScale = 800f
            detailRatio = 0.5f
        }
    )
}

fun ShadowContext.bug(bugTransform: Transform) = Renderable(
    mesh = obj("/bug/bug.obj"),
    material = standard {
        colorTexture = texture("/bug/bug.jpg")
    },
    transform = Transform(bugTransform.mat4() * Transform().rotate(1.y, -PIdiv2).mat4())
)
