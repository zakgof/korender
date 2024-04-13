import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.Materials.standard
import com.zakgof.korender.declaration.Meshes.heightField
import com.zakgof.korender.declaration.Meshes.obj
import com.zakgof.korender.declaration.SceneContext
import com.zakgof.korender.declaration.ShadowContext
import com.zakgof.korender.declaration.Textures.texture
import com.zakgof.korender.image.Images.image
import com.zakgof.korender.impl.material.Image
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.FloatMath.PIdiv2
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun App() = Korender {

    val elevationRatio = 300.0f
    val hfImage = image("/hf-rg16-512.png")

    val hf = RgImageHeightField(hfImage, 20.0f, elevationRatio)

    val bugPosition = hf.surface(Vec3.ZERO, -1.0f)
    val bugPhysics = Physics(hf, bugPosition)
    val chaseCamera = ChaseCamera(bugPhysics.transform())
    val missileManager = MissileManager(hf)

    Scene {

        bugPhysics.update(frameInfo.dt)
        missileManager.update(frameInfo.time, frameInfo.dt)
        val bugTransform = bugPhysics.transform()
        OnTouch { chaseCamera.touch(it) }

        Light(Vec3(0f, -1f, 3f).normalize())
        Projection(FrustumProjection(width = 3f * width / height, height = 3f, near = 3f, far = 10000f))
        Camera(chaseCamera.camera(bugTransform, projection, width, height, hf, frameInfo.dt))

        terrain(hfImage, hf, elevationRatio)
        Shadow(mapSize = 1024) {
            bug(bugTransform)
            missileManager.missiles().forEach { missile(it) }
            alien(Transform().translate(hf.surface(-50.z)))
            head(Transform().translate(hf.surface(-50.z - 10.x)))
        }
        missileManager.explosions(frameInfo.time).forEach { explosion(it.first, it.second) }
        Sky("fastcloud")
        Filter("atmosphere.frag")
        gui(bugPhysics, missileManager)
    }
}

private fun SceneContext.gui(bugPhysics: Physics, missileManager: MissileManager) {
    Gui {
        Text(id = "coords", text = String.format("Eye: %.1f %.1f %.1f", camera.position.x, camera.position.y, camera.position.z), fontResource = "/ubuntu.ttf", height = 30, color = Color(0xFFFFFF))
        Text(id = "fps", text = String.format("FPS: %.1f", frameInfo.avgFps), fontResource = "/ubuntu.ttf", height = 30, color = Color(0xFFFFFF))
        Filler()
        Row {
            Column {
                Row {
                    Column {
                        Filler()
                        Image(imageResource = "/left.png", width = 128, height = 128, onTouch = { bugPhysics.left(it) })
                    }
                    Column {
                        Filler()
                        Image(imageResource = "/accelerate.png", width = 128, height = 128, onTouch = { bugPhysics.forward(it) })
                        Image(imageResource = "/decelerate.png", width = 128, height = 128, onTouch = { bugPhysics.backward(it) })
                    }
                    Column {
                        Filler()
                        Image(imageResource = "/right.png", width = 128, height = 128, onTouch = { bugPhysics.right(it) })
                    }
                }
            }
            Filler()
            Column {
                Filler()
                if (missileManager.canFire(frameInfo.time)) {
                    Image(imageResource = "/fire.png", width = 128, height = 128, onTouch = { missileManager.fire(frameInfo.time, it, bugPhysics.transform(), bugPhysics.velocity) })
                }
            }
        }
    }
}

private fun SceneContext.terrain(hfImage: Image, hf: RgImageHeightField, elevationRatio: Float) {
    Renderable(
        mesh = heightField(id = "terrain",
            cellsX = hfImage.width - 1,
            cellsZ = hfImage.height - 1,
            cellWidth = 20.0f,
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
    transform = bugTransform * Transform().translate(0.2f.y).scale(2.0f).rotate(1.y, -PIdiv2)
)

fun ShadowContext.missile(missileTransform: Transform) = Renderable(
    mesh = obj("/missile/missile.obj"),
    material = standard {
        colorTexture = texture("/missile/missile.jpg")
    },
    transform = missileTransform * Transform().rotate(1.y, -PIdiv2)
)

fun ShadowContext.alien(alienTransform: Transform) = Renderable(
    mesh = obj("/alien/alien.obj"),
    material = standard {
        colorTexture = texture("/alien/alien.jpg")
    },
    transform = alienTransform * Transform().rotate(1.y, -PIdiv2).scale(10.0f).translate(4.y)
)

fun ShadowContext.head(alienTransform: Transform) = Renderable(
    mesh = obj("/head/head-high.obj"),
    material = standard {
        colorTexture = texture("/head/head-high.jpg")
    },
    transform = alienTransform * Transform().rotate(1.y, -PIdiv2).scale(10.0f).translate(4.y)
)

fun SceneContext.explosion(position: Vec3, phase: Float) = Billboard (
    fragment = "effect/fireball.frag",
    position = position,
    material = {
        xscale = 12f * phase
        yscale = 12f * phase
        static("power", phase)
    }
)
