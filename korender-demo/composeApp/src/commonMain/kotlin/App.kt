
import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.Materials.standard
import com.zakgof.korender.declaration.Meshes.heightField
import com.zakgof.korender.declaration.Meshes.obj
import com.zakgof.korender.declaration.SceneContext
import com.zakgof.korender.declaration.StandardMaterialOption
import com.zakgof.korender.declaration.Textures.texture
import com.zakgof.korender.impl.material.Image
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.FloatMath.PIdiv2
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun App() = Korender {

    val controller = Controller()

    Scene {

        controller.update(frameInfo)
        val bugTransform = controller.characterManager.transform()
        OnTouch { controller.chaseCamera.touch(it) }

        Light(Vec3(0f, -1f, 3f).normalize())
        Projection(FrustumProjection(width = 3f * width / height, height = 3f, near = 3f, far = 10000f))
        Camera(controller.chaseCamera.camera(bugTransform, projection, width, height, controller.hf, frameInfo.dt))
        Shadow {
            Cascade(1024, 3f, 20f)
            Cascade(512, 18f, 100f)
            Cascade(256, 100f, 5000f)
        }

        terrain(controller.hfImage, controller.hf, controller.elevationRatio)
        bug(bugTransform)
        controller.missileManager.missiles.forEach { missile(it.transform()) }
        controller.enemyManager.heads.forEach { head(it.transform()) }
        controller.explosionManager.explosions.forEach { explosion(it) }
        controller.skullManager.skulls.forEach { skull(it, controller.hf) }
        Sky("fastcloud")
        Filter("atmosphere.frag")
        gui(controller.characterManager, controller.missileManager)
    }
}

fun SceneContext.skull(skull: SkullManager.Skull, hf: HeightField) {
    Renderable(
        mesh = obj("/obelisk/obelisk.obj"),
        material = standard {
            colorTexture = texture("/obelisk/obelisk.jpg")
        },
        transform = Transform().scale(5.0f, 8.0f, 5.0f).translate(hf.surface(skull.position, 4f))
    )
    if (!skull.destroyed) {
        Renderable(
            mesh = obj("/skull/skull.obj"),
            material = standard {
                colorTexture = texture("/skull/skull.jpg")
            },
            transform = Transform().rotate(1.y, -PIdiv2).rotate(skull.look, 1.y).scale(2.0f).translate(hf.surface(skull.position, 9.5f))
        )
    }
}

private fun SceneContext.gui(characterManager: CharacterManager, missileManager: MissileManager) {
    val cannonBtm = (characterManager.cannonAngle * 256f).toInt() - 48
    val cannonTop = 128 - cannonBtm
    Gui {
        Text(id = "points", text = String.format("SCORE: %d", characterManager.score), fontResource = "/ubuntu.ttf", height = 50, color = Color(0xFFFFFF))
        Text(id = "fps", text = String.format("FPS: %.1f", frameInfo.avgFps), fontResource = "/ubuntu.ttf", height = 20, color = Color(0xFFFFFF))

        if (characterManager.gameOver) {
            Filler()
            Row {
                Filler()
                Text(id = "gameover", text = "GAME OVER", fontResource = "/ubuntu.ttf", height = 50, color = Color(0xFF1234))
                Filler()
            }
        }

        Filler()
        Row {
            Column {
                Row {
                    Column {
                        Filler()
                        Image(imageResource = "/icon/left.png", width = 128, height = 128, onTouch = { characterManager.left(it) })
                    }
                    Column {
                        Filler()
                        Image(imageResource = "/icon/accelerate.png", width = 128, height = 128, onTouch = { characterManager.forward(it) })
                        Image(imageResource = "/icon/decelerate.png", width = 128, height = 128, onTouch = { characterManager.backward(it) })
                    }
                    Column {
                        Filler()
                        Image(imageResource = "/icon/right.png", width = 128, height = 128, onTouch = { characterManager.right(it) })
                    }
                }
            }
            Filler()
            Column {
                Filler()
                Image(imageResource = "/icon/angle-up.png", width = 128, height = 128, onTouch = { characterManager.cannonUp(it) })
                Image(imageResource = "/icon/minus.png", width = 64, height = 64, marginLeft = 32, marginTop = cannonTop, marginBottom = cannonBtm)
                Image(imageResource = "/icon/angle-down.png", width = 128, height = 128, marginBottom = if (missileManager.canFire(frameInfo.time)) 0 else 128 , onTouch = { characterManager.cannonDown(it) })

                if (missileManager.canFire(frameInfo.time)) {
                    Image(imageResource = "/icon/fire.png", width = 128, height = 128, onTouch = { missileManager.fire(frameInfo.time, it, characterManager.transform(), characterManager.velocity, characterManager.cannonAngle) })
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
        material = standard(StandardMaterialOption.Detail, StandardMaterialOption.NoShadowCast) {
            colorTexture = texture("/terrainbase.jpg")
            detailTexture = texture("/sand.jpg")
            detailScale = 800f
            detailRatio = 0.5f
        }
    )
}

fun SceneContext.bug(bugTransform: Transform) = Renderable(
    mesh = obj("/bug/bug.obj"),
    material = standard() {
        colorTexture = texture("/bug/bug.jpg")
    },
    transform = bugTransform * Transform().translate(0.2f.y).scale(2.0f).rotate(1.y, -PIdiv2)
)

fun SceneContext.missile(missileTransform: Transform) = Renderable(
    mesh = obj("/missile/missile.obj"),
    material = standard {
        colorTexture = texture("/missile/missile.jpg")
    },
    transform = missileTransform * Transform().rotate(1.y, -PIdiv2)
)

fun SceneContext.alien(alienTransform: Transform) = Renderable(
    mesh = obj("/alien/alien.obj"),
    material = standard {
        colorTexture = texture("/alien/alien.jpg")
    },
    transform = alienTransform * Transform().rotate(1.y, -PIdiv2).scale(10.0f).translate(4.y)
)

fun SceneContext.head(headTransform: Transform) = Renderable(
    mesh = obj("/head/head-high.obj"),
    material = standard {
        colorTexture = texture("/head/head-high.jpg")
    },
    transform = headTransform * Transform().rotate(1.y, -PIdiv2).scale(2.0f).translate(1.0f.y)
)

fun SceneContext.explosion(explosion: ExplosionManager.Explosion) = Billboard(
    fragment = "effect/fireball.frag",
    position = explosion.position,
    material = {
        xscale = explosion.radius * explosion.phase
        yscale = explosion.radius * explosion.phase
        static("power", explosion.phase)
    },
    transparent = true
)
