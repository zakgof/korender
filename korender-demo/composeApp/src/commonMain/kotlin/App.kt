
import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.FrameContext
import com.zakgof.korender.declaration.Materials.billboardStandard
import com.zakgof.korender.declaration.Materials.standard
import com.zakgof.korender.declaration.Meshes.heightField
import com.zakgof.korender.declaration.Meshes.mesh
import com.zakgof.korender.declaration.Meshes.obj
import com.zakgof.korender.declaration.StandardMaterialOption
import com.zakgof.korender.declaration.Textures.texture
import com.zakgof.korender.impl.geometry.Attributes
import com.zakgof.korender.impl.geometry.Attributes.NORMAL
import com.zakgof.korender.impl.geometry.Attributes.POS
import com.zakgof.korender.impl.geometry.Attributes.TEX
import com.zakgof.korender.impl.geometry.Vertex
import com.zakgof.korender.impl.material.Image
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.FloatMath.PIdiv2
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun App() = Korender {

    val controller = Controller()

    Frame {

        controller.update(frameInfo)
        val bugTransform = controller.characterManager.transform()
        OnTouch { controller.chaseCamera.touch(it) }

        Light(Vec3(0f, -1f, 3f).normalize())
        Projection(FrustumProjection(width = 3f * width / height, height = 3f, near = 3f, far = 10000f))
        Camera(controller.camera(bugTransform, projection, width, height, frameInfo.dt, frameInfo.time))
        Shadow {
            Cascade(1024, 3f, 20f)
            Cascade(512, 18f, 100f)
            Cascade(256, 100f, 10000f)
        }

        terrain(controller.hfImage, controller.hf, controller.elevationRatio)
        bug(bugTransform)
        controller.missileManager.missiles.forEach { missile(it.transform()) }
        controller.enemyManager.heads.forEach { head(it.transform()) }
        controller.explosionManager.explosions.forEach { explosion(it) }
        splinters(controller.explosionManager)
        controller.skullManager.skulls.forEach { skull(it, controller.hf) }

        val skyPlugins = mapOf("sky" to "sky/fastcloud.plugin.frag")
        Sky(plugins = skyPlugins)
        Filter("effect/water.frag", plugins = skyPlugins)
        Filter("atmosphere.frag")
        gui(controller)
    }
}

fun FrameContext.skull(skull: SkullManager.Skull, hf: HeightField) {
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
            transform = skull.transform * Transform().rotate(1.y, -PIdiv2)
        )
    }
}

private fun FrameContext.gui(controller: Controller) {
    val cannonBtm = (controller.characterManager.cannonAngle * 256f).toInt() - 48
    val cannonTop = 52 - cannonBtm
    Gui {
        Text(id = "points", text = String.format("SCORE: %d", controller.characterManager.score), fontResource = "/ubuntu.ttf", height = 50, color = Color(0xFFFFFF))
        Text(id = "fps", text = String.format("FPS: %.1f  ${controller.characterManager.transform() * Vec3.ZERO}", frameInfo.avgFps), fontResource = "/ubuntu.ttf", height = 30, color = Color(0xFFFFFF))

        if (controller.gameOver) {
            Filler()
            Row {
                Filler()
                Text(id = "gameover", text = "GAME OVER", fontResource = "/ubuntu.ttf", height = 100, color = Color(0xFF1234), onTouch = { controller.restart(it) })
                Filler()
            }
            Row {
                Filler()
                Text(id = "restart", text = "click to start new game", fontResource = "/ubuntu.ttf", height = 50, color = Color(0x89FF34), onTouch = { controller.restart(it) })
                Filler()
            }
        }

        Filler()
        if (!controller.gameOver) {
            Row {
                Column {
                    Row {
                        Column {
                            Filler()
                            Image(imageResource = "/icon/left.png", width = 128, height = 128, onTouch = { controller.characterManager.left(it) })
                        }
                        Column {
                            Filler()
                            Image(imageResource = "/icon/accelerate.png", width = 128, height = 128, onTouch = { controller.characterManager.forward(it) })
                            Image(imageResource = "/icon/decelerate.png", width = 128, height = 128, onTouch = { controller.characterManager.backward(it) })
                        }
                        Column {
                            Filler()
                            Image(imageResource = "/icon/right.png", width = 128, height = 128, onTouch = { controller.characterManager.right(it) })
                        }
                    }
                }
                Filler()
                Column {
                    Filler()
                    Image(imageResource = "/icon/angle-up.png", width = 128, height = 128, onTouch = { controller.characterManager.cannonUp(it) })
                    Image(imageResource = "/icon/minus.png", width = 64, height = 64, marginLeft = 32, marginTop = cannonTop, marginBottom = cannonBtm)
                    Image(imageResource = "/icon/angle-down.png", width = 128, height = 128, marginBottom = if (controller.missileManager.canFire(frameInfo.time)) 0 else 128, onTouch = { controller.characterManager.cannonDown(it) })

                    if (controller.missileManager.canFire(frameInfo.time)) {
                        Image(
                            imageResource = "/icon/fire.png",
                            width = 128,
                            height = 128,
                            onTouch = { controller.missileManager.fire(frameInfo.time, it, controller.characterManager.transform(), controller.characterManager.velocity, controller.characterManager.cannonAngle) })
                    }
                }
            }
        }
    }
}

private fun FrameContext.terrain(hfImage: Image, hf: RgImageHeightField, elevationRatio: Float) {
    Renderable(
        mesh = mesh("underterrain", true, 4, 6, POS, NORMAL, TEX) {
            vertex(Vertex(pos = Vec3(-20480f, -3f, -20480f), normal = 1.y, tex = Vec2(0f, 0f)))
            vertex(Vertex(pos = Vec3( 20480f, -3f, -20480f), normal = 1.y, tex = Vec2(1f, 0f)))
            vertex(Vertex(pos = Vec3( 20480f, -3f,  20480f), normal = 1.y, tex = Vec2(1f, 1f)))
            vertex(Vertex(pos = Vec3(-20480f, -3f,  20480f), normal = 1.y, tex = Vec2(0f, 1f)))
            indices(0, 2, 1, 0, 3, 2)
        },
        material = standard(StandardMaterialOption.NoShadowCast, StandardMaterialOption.Detail) {
            colorTexture = texture("/terrain/terrainbase.jpg")
            detailTexture =  texture("/sand.jpg")
            detailRatio = 1.0f
            detailScale = 1600.0f
        }
    )
    Renderable(
        mesh = heightField(id = "terrain",
            cellsX = hfImage.width - 1,
            cellsZ = hfImage.height - 1,
            cellWidth = 20.0f,
            height = { x, y -> hf.pixel(x, y) * elevationRatio - 3.0f }
        ),
        material = standard(StandardMaterialOption.NoShadowCast, plugins = mapOf("texture" to "terrain/texture.plugin.frag")) {
            colorTexture = texture("/terrain/terrainbase.jpg")
            static("tex1", texture("/sand.jpg"))
            static("tex2", texture("/grass.jpg"))
        }
    )
}

fun FrameContext.bug(bugTransform: Transform) = Renderable(
    mesh = obj("/bug/bug.obj"),
    material = standard {
        colorTexture = texture("/bug/bug.jpg")
    },
    transform = bugTransform * Transform().rotate(1.y, -PIdiv2).scale(2.0f).translate(0.3f.y)
)

fun FrameContext.missile(missileTransform: Transform) = Renderable(
    mesh = obj("/missile/missile.obj"),
    material = standard {
        colorTexture = texture("/missile/missile.jpg")
    },
    transform = missileTransform * Transform().rotate(1.y, -PIdiv2)
)

fun FrameContext.alien(alienTransform: Transform) = Renderable(
    mesh = obj("/alien/alien.obj"),
    material = standard {
        colorTexture = texture("/alien/alien.jpg")
    },
    transform = alienTransform * Transform().rotate(1.y, -PIdiv2).scale(10.0f).translate(4.y)
)

fun FrameContext.head(headTransform: Transform) = Renderable(
    mesh = obj("/head/head-high.obj"),
    material = standard {
        colorTexture = texture("/head/head-high.jpg")
    },
    transform = headTransform * Transform().rotate(1.y, -PIdiv2).scale(2.0f)
)

fun FrameContext.explosion(explosion: ExplosionManager.Explosion) = Billboard(
    billboardStandard(fragFile = "effect/fireball.frag") {
        xscale = explosion.radius * explosion.phase
        yscale = explosion.radius * explosion.phase
        static("power", explosion.phase)
    },
    position = explosion.position,
    transparent = true
)

fun FrameContext.splinters(explosionManager: ExplosionManager) = InstancedRenderables(
    id = "splinters",
    material = standard(StandardMaterialOption.Color) {
        color = Color(0x804040)
        colorTexture = texture("/sand.jpg")
    },
    count = 5000,
    mesh = mesh(id = "splinter", static = true, vertexCount = 3, indexCount = 3, POS, Attributes.NORMAL, Attributes.TEX) {
        vertex(Vertex(pos = Vec3(-0.1f, 0f, 0f), normal = 1.z, tex = Vec2(0f, 0f)))
        vertex(Vertex(pos = Vec3(0f, 0f, 0f), normal = 1.z, tex = Vec2(1f, 0f)))
        vertex(Vertex(pos = Vec3(0f, 0.2f, 0f), normal = 1.z, tex = Vec2(1f, 1f)))
        indices(0, 1, 2)
    }) {

    explosionManager.splinters.forEach {
        Instance(transform = Transform().rotate(it.orientation).translate(it.position))
    }

}



