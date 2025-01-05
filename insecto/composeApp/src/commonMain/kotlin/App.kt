import androidx.compose.runtime.Composable
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.Korender
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.context.PassContext
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.FloatMath.PIdiv2
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlinx.coroutines.Deferred


fun loadImage(imageResource: String) : Deferred<Image>

@Composable
fun App() = Korender {

    val controller = Controller()

    Frame {

        controller.update(frameInfo)
        val bugTransform = controller.characterManager.transform()
        OnTouch { controller.chaseCamera.touch(it) }

        DirectionalLight(Vec3(0f, -1f, 3f).normalize())
        projection = frustum(width = 3f * width / height, height = 3f, near = 3f, far = 10000f)
        camera =
            controller.camera(bugTransform, projection, width, height, frameInfo.dt, frameInfo.time)
        Shadow {
            Cascade(1024, 3f, 20f)
            Cascade(512, 20f, 100f)
            Cascade(512, 100f, 10000f)
        }

        val skyPlugin = fastCloudSky()
        Pass {
            terrain(this@Korender, controller.hfImage, controller.hf, controller.elevationRatio)
            bug(this@Korender, bugTransform)
            controller.missileManager.missiles.forEach { missile(this@Korender, it.transform()) }
            controller.enemyManager.heads.forEach { head(this@Korender, it.transform()) }
            controller.explosionManager.explosions.forEach { explosion(this@Korender, it) }
            splinters(this@Korender, controller.explosionManager)
            controller.skullManager.skulls.forEach { skull(this@Korender, it) }
            Sky(skyPlugin)
        }
        Pass {
            Screen(water(), skyPlugin)
        }
        Pass {
            Screen(fragment("atmosphere.frag"))
            gui(controller)
        }
    }
}

fun PassContext.skull(kc: KorenderContext, skull: SkullManager.Skull) {
    if (!skull.destroyed) {
        Renderable(
            kc.standart {
                baseColorTexture = kc.texture("/skull/skull.jpg")
                pbr.metallic = 0.5f
            },
            mesh = kc.obj("/skull/skull.obj"),
            transform = skull.transform * rotate(1.y, -PIdiv2)
        )
    }
}

private fun PassContext.gui(controller: Controller) {
    val cannonBtm = (controller.characterManager.cannonAngle * 256f).toInt() - 48
    val cannonTop = 52 - cannonBtm
    Gui {
        Text(
            id = "points",
            text = String.format("SCORE: %d", controller.characterManager.score),
            fontResource = "/ubuntu.ttf",
            height = 50,
            color = Color(0xFFFF8080)
        )
        Text(
            id = "fps",
            text = String.format(
                "FPS: %.1f  ${controller.characterManager.transform() * Vec3.ZERO}",
                frameInfo.avgFps
            ),
            fontResource = "/ubuntu.ttf",
            height = 30,
            color = Color(0xFFFF8080)
        )

        if (controller.gameOver) {
            Filler()
            Row {
                Filler()
                Text(
                    id = "gameover",
                    text = "GAME OVER",
                    fontResource = "/ubuntu.ttf",
                    height = 100,
                    color = Color(0xFFFF1234),
                    onTouch = { controller.restart(it) })
                Filler()
            }
            Row {
                Filler()
                Text(
                    id = "restart",
                    text = "click to start new game",
                    fontResource = "/ubuntu.ttf",
                    height = 50,
                    color = Color(0xFF89FF34),
                    onTouch = { controller.restart(it) })
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
                            Image(
                                imageResource = "/icon/left.png",
                                width = 128,
                                height = 128,
                                onTouch = { controller.characterManager.left(it) })
                        }
                        Column {
                            Filler()
                            Image(
                                imageResource = "/icon/accelerate.png",
                                width = 128,
                                height = 128,
                                onTouch = { controller.characterManager.forward(it) })
                            Image(
                                imageResource = "/icon/decelerate.png",
                                width = 128,
                                height = 128,
                                onTouch = { controller.characterManager.backward(it) })
                        }
                        Column {
                            Filler()
                            Image(
                                imageResource = "/icon/right.png",
                                width = 128,
                                height = 128,
                                onTouch = { controller.characterManager.right(it) })
                        }
                    }
                }
                Filler()
                Column {
                    Filler()
                    Image(
                        imageResource = "/icon/angle-up.png",
                        width = 128,
                        height = 128,
                        onTouch = { controller.characterManager.cannonUp(it) })
                    Image(
                        imageResource = "/icon/minus.png",
                        width = 64,
                        height = 64,
                        marginLeft = 32,
                        marginTop = cannonTop,
                        marginBottom = cannonBtm
                    )
                    Image(
                        imageResource = "/icon/angle-down.png",
                        width = 128,
                        height = 128,
                        marginBottom = if (controller.missileManager.canFire(frameInfo.time)) 0 else 128,
                        onTouch = { controller.characterManager.cannonDown(it) })

                    if (controller.missileManager.canFire(frameInfo.time)) {
                        Image(
                            imageResource = "/icon/fire.png",
                            width = 128,
                            height = 128,
                            onTouch = {
                                controller.missileManager.fire(
                                    frameInfo.time,
                                    it,
                                    controller.characterManager.transform(),
                                    controller.characterManager.velocity,
                                    controller.characterManager.cannonAngle
                                )
                            })
                    }
                }
            }
        }
    }
}

private fun PassContext.terrain(
    ck: KorenderContext,
    hf: RgImageHeightField,
    elevationRatio: Float
) {
    Renderable(
        ck.standart {
            baseColorTexture = ck.texture("/terrain/terrainbase.jpg")
            // detailTexture = ck.texture("/sand.jpg")
            // detailRatio = 1.0f
            // detailScale = 1600.0f
        },
        mesh = ck.customMesh("underterrain", 4, 6, POS, NORMAL, TEX) {
            pos(-20480f, -3f, -20480f).normal(1.y).tex(0f, 0f)
            pos(20480f, -3f, -20480f).normal(1.y).tex(1f, 0f)
            pos(20480f, -3f, 20480f).normal(1.y).tex(1f, 1f)
            pos(-20480f, -3f, 20480f).normal(1.y).tex(0f, 1f)
            index(0, 2, 1, 0, 3, 2)
        }
    )
    Renderable(
        ck.plugin("texture", "terrain/texture.plugin.frag"),
        ck.standart {
            baseColorTexture = ck.texture("/terrain/terrainbase.jpg")
            set("tex1", ck.texture("/sand.jpg"))
            set("tex2", ck.texture("/grass.jpg"))
        },
        mesh = ck.heightField(id = "terrain",
            cellsX = hf.width - 1,
            cellsZ = hf.height - 1,
            cellWidth = 20.0f,
            height = { x, y -> hf.pixel(x, y) * elevationRatio - 3.0f }
        )
    )
}

fun PassContext.bug(kc: KorenderContext, bugTransform: Transform) = Renderable(
    kc.standart {
        baseColorTexture = kc.texture("/bug/bug.jpg")
        pbr.metallic = 0.2f
    },
    mesh = kc.obj("/bug/bug.obj"),
    transform = bugTransform * rotate(1.y, -PIdiv2).scale(2.0f).translate(0.3f.y)
)

fun PassContext.missile(kc: KorenderContext, missileTransform: Transform) = Renderable(
    kc.standart {
        baseColorTexture = kc.texture("/missile/missile.jpg")
        pbr.metallic = 0.8f
    },
    mesh = kc.obj("/missile/missile.obj"),
    transform = missileTransform * rotate(1.y, -PIdiv2)
)

fun PassContext.head(kc: KorenderContext, headTransform: Transform) = Renderable(
    kc.standart {
        baseColorTexture = kc.texture("/head/head-high.jpg")
        pbr.metallic = 0.4f
    },
    mesh = kc.obj("/head/head-high.obj"),
    transform = headTransform * rotate(1.y, -PIdiv2).scale(2.0f)
)

fun PassContext.explosion(kc: KorenderContext, explosion: ExplosionManager.Explosion) = Billboard(
    kc.fireball {
        xscale = explosion.finishRadius * explosion.phase
        yscale = explosion.finishRadius * explosion.phase
        power = explosion.phase
    },
    position = explosion.position,
    transparent = true
)

fun PassContext.splinters(kc: KorenderContext, explosionManager: ExplosionManager) =
    InstancedRenderables(
        kc.standart {
            baseColor = Color(0xFF804040)
            baseColorTexture = kc.texture("/sand.jpg")
        },
        id = "splinters",
        count = 5000,
        mesh = kc.customMesh(id = "splinter", vertexCount = 3, indexCount = 3, POS, NORMAL, TEX) {
            pos(-0.1f, 0f, 0f).normal(1.z).tex(0f, 0f)
            pos(0f, 0f, 0f).normal(1.z).tex(1f, 0f)
            pos(0f, 0.2f, 0f).normal(1.z).tex(1f, 1f)
            index(0, 1, 2)
        }) {

        explosionManager.splinters.forEach {
            Instance(rotate(it.orientation).translate(it.position))
        }

    }