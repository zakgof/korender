import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.context.PassContext
import com.zakgof.korender.image.Image
import com.zakgof.korender.material.MaterialModifiers.fragment
import com.zakgof.korender.material.MaterialModifiers.options
import com.zakgof.korender.material.MaterialModifiers.plugin
import com.zakgof.korender.material.MaterialModifiers.standardUniforms
import com.zakgof.korender.material.StandardMaterialOption
import com.zakgof.korender.material.Textures.texture

import com.zakgof.korender.mesh.Attributes.NORMAL
import com.zakgof.korender.mesh.Attributes.POS
import com.zakgof.korender.mesh.Attributes.TEX
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.FloatMath.PIdiv2
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.mesh.Meshes.customMesh
import com.zakgof.korender.mesh.Meshes.heightField
import com.zakgof.korender.mesh.Meshes.obj
import com.zakgof.korender.mesh.Vertex
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
            Cascade(512, 20f, 100f)
            Cascade(512, 100f, 10000f)
        }

        val skyPlugin = plugin("sky", "sky/fastcloud.plugin.frag")
        Pass {
            terrain(controller.hfImage, controller.hf, controller.elevationRatio)
            bug(bugTransform)
            controller.missileManager.missiles.forEach { missile(it.transform()) }
            controller.enemyManager.heads.forEach { head(it.transform()) }
            controller.explosionManager.explosions.forEach { explosion(it) }
            splinters(controller.explosionManager)
            controller.skullManager.skulls.forEach { skull(it) }
            Sky(skyPlugin)
        }
        Pass {
            Screen(fragment("effect/water.frag"), skyPlugin)
        }
        Pass {
            Screen(fragment("atmosphere.frag"))
            gui(controller)
        }
    }
}

fun PassContext.skull(skull: SkullManager.Skull) {
    if (!skull.destroyed) {
        Renderable(
            standardUniforms {
                colorTexture = texture("/skull/skull.jpg")
            },
            mesh = obj("/skull/skull.obj"),
            transform = skull.transform * Transform().rotate(1.y, -PIdiv2)
        )
    }
}

private fun PassContext.gui(controller: Controller) {
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

private fun PassContext.terrain(hfImage: Image, hf: RgImageHeightField, elevationRatio: Float) {
    Renderable(
        options(StandardMaterialOption.NoShadowCast, StandardMaterialOption.Detail),
        standardUniforms {
            colorTexture = texture("/terrain/terrainbase.jpg")
            detailTexture = texture("/sand.jpg")
            detailRatio = 1.0f
            detailScale = 1600.0f
        },
        mesh = customMesh("underterrain", true, 4, 6, POS, NORMAL, TEX) {
            vertex(Vertex(pos = Vec3(-20480f, -3f, -20480f), normal = 1.y, tex = Vec2(0f, 0f)))
            vertex(Vertex(pos = Vec3(20480f, -3f, -20480f), normal = 1.y, tex = Vec2(1f, 0f)))
            vertex(Vertex(pos = Vec3(20480f, -3f, 20480f), normal = 1.y, tex = Vec2(1f, 1f)))
            vertex(Vertex(pos = Vec3(-20480f, -3f, 20480f), normal = 1.y, tex = Vec2(0f, 1f)))
            indices(0, 2, 1, 0, 3, 2)
        }
    )
    Renderable(
        options(StandardMaterialOption.NoShadowCast),
        plugin("texture", "terrain/texture.plugin.frag"),
        standardUniforms {
            colorTexture = texture("/terrain/terrainbase.jpg")
            static("tex1", texture("/sand.jpg"))
            static("tex2", texture("/grass.jpg"))
        },
        mesh = heightField(id = "terrain",
            cellsX = hfImage.width - 1,
            cellsZ = hfImage.height - 1,
            cellWidth = 20.0f,
            height = { x, y -> hf.pixel(x, y) * elevationRatio - 3.0f }
        )
    )
}

fun PassContext.bug(bugTransform: Transform) = Renderable(
    standardUniforms {
        colorTexture = texture("/bug/bug.jpg")
    },
    mesh = obj("/bug/bug.obj"),
    transform = bugTransform * Transform().rotate(1.y, -PIdiv2).scale(2.0f).translate(0.3f.y)
)

fun PassContext.missile(missileTransform: Transform) = Renderable(
    standardUniforms {
        colorTexture = texture("/missile/missile.jpg")
    },
    mesh = obj("/missile/missile.obj"),
    transform = missileTransform * Transform().rotate(1.y, -PIdiv2)
)

fun PassContext.head(headTransform: Transform) = Renderable(
    standardUniforms {
        colorTexture = texture("/head/head-high.jpg")
    },
    mesh = obj("/head/head-high.obj"),
    transform = headTransform * Transform().rotate(1.y, -PIdiv2).scale(2.0f)
)

fun PassContext.explosion(explosion: ExplosionManager.Explosion) = Billboard(
    fragment("effect/fireball.frag"),
    standardUniforms {
        xscale = explosion.finishRadius * explosion.phase
        yscale = explosion.finishRadius * explosion.phase
        static("power", explosion.phase)
    },
    position = explosion.position,
    transparent = true
)

fun PassContext.splinters(explosionManager: ExplosionManager) = InstancedRenderables(
    options(StandardMaterialOption.Color),
    standardUniforms {
        color = Color(0x804040)
        colorTexture = texture("/sand.jpg")
    },
    id = "splinters",
    count = 5000,
    mesh = customMesh(id = "splinter", static = true, vertexCount = 3, indexCount = 3, POS, NORMAL, TEX) {
        vertex(Vertex(pos = Vec3(-0.1f, 0f, 0f), normal = 1.z, tex = Vec2(0f, 0f)))
        vertex(Vertex(pos = Vec3(0f, 0f, 0f), normal = 1.z, tex = Vec2(1f, 0f)))
        vertex(Vertex(pos = Vec3(0f, 0.2f, 0f), normal = 1.z, tex = Vec2(1f, 1f)))
        indices(0, 1, 2)
    }) {

    explosionManager.splinters.forEach {
        Instance(transform = Transform().rotate(it.orientation).translate(it.position))
    }

}