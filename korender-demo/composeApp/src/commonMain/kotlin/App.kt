import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.Materials.standard
import com.zakgof.korender.declaration.Meshes.heightField
import com.zakgof.korender.declaration.Textures.texture
import com.zakgof.korender.image.Images.image
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun App() = Korender {
    val elevationRatio = 300.0f
    val hfImage = image("/heightmap.png")
    val hf = ImageHeightField(hfImage, 10.0f, elevationRatio)
    val freeCamera = FreeCamera(
        initialPosition = Vec3(0f, hf.elevation(5f, 5f) + 25.0f, 0f),
        initialDirection = Vec3(0f, 0f, -1f),
        velocity = 60f
    )
    Scene {
        Light(Vec3(0f, -1f, 8f).normalize())
        OnTouch { freeCamera.touch(it) }
        Projection(FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 10000f))
        Camera(freeCamera.camera(projection, width, height, frameInfo.dt))
        Renderable(
            mesh = heightField(id = "terrain",
                cellsX = hfImage.width - 1,
                cellsZ = hfImage.height - 1,
                cellWidth = 10.0f,
                height = { x, y -> hfImage.pixel(x, y).r * elevationRatio }
            ),
            material = standard() {
                colorTexture = texture("/terrainbase.jpg")
            }
        )
        Sky("fastcloud")
        // Filter("atmosphere.frag")
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
                    Image(imageResource = "/accelerate.png", width = 128, height = 128, onTouch = { freeCamera.forward(it) })
                    Image(imageResource = "/decelerate.png", width = 128, height = 128, onTouch = { freeCamera.backward(it) })
                }
            }
        }
    }
}