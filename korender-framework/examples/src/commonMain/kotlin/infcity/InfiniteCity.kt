package com.zakgof.korender.examples.infcity

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.Korender
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor

private val buildings = (0 until 10).map { generateBuilding() }

@Composable
@OptIn(ExperimentalResourceApi::class)
fun InfiniteCity() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    Frame {

        val z = frameInfo.time * 0.2f

        DeferredShading {
            PostShading (
                bloom(width = width / 4, height = height / 4)
            )
        }

        projection = frustum(0.3f * width / height, 0.3f, 0.3f, 200f)
        camera = camera(Vec3(0.05f, 0.3f, z - 1f), Quaternion.fromAxisAngle(1.y, 0.05f * cos(frameInfo.time)) * 1.z, 1.y)

        light()

        character(z)
        city(z)
        sky()
        gui()
    }

}

private fun FrameContext.light() {
    AmbientLight(white(0.7f))
    DirectionalLight(Vec3(0.1f, -1f, -1f).normalize(), white(2f)) {
        if (target == KorenderContext.TargetPlatform.Desktop) {
            Cascade(1024, 0.3f, 3.0f, 0f to 60f, pcss(12))
            Cascade(1024, 2.5f, 12.0f, 0f to 60f, vsm())
            Cascade(1024, 10.0f, 50.0f, 0f to 60f, vsm())
        } else {
            Cascade(1024, 0.3f, 3.0f, 0f to 60f, hard())
            Cascade(1024, 2.5f, 30.0f, 0f to 60f, vsm())
        }
    }
}

private fun FrameContext.character(z: Float) {
    Gltf(
        resource = "infcity/swat-woman.glb",
        animation = 2,
        transform = scale(0.002f).translate(z.z + 0.1f.x)
    )
}

private fun FrameContext.city(z: Float) {
    val start = floor(z / 32f) * 32f - 4f
    cityChunk(start)
    cityChunk(start + 32f)
    cityChunk(start + 64f)
}

private fun FrameContext.cityChunk(startZ: Float) {
    road(startZ)
    building(abs(startZ.hashCode()) % 10, startZ, -20f)
    building(abs((startZ * 5f).hashCode()) % 10, startZ, 4f)
    sidewalk(startZ, 0.5f)
    sidewalk(startZ, -0.5f - 32f)
    tree(startZ + 8f, 1.2f)
    tree(startZ + 24f, 1.2f)
    tree(startZ + 8f, -1.2f)
    tree(startZ + 24f, -1.2f)
}

private fun FrameContext.tree(z: Float, x: Float) {
    Gltf(
        resource = "infcity/tree.glb",
        transform = rotate(1.y, z + x).translate(x, 0.95f, z)
    )
}

private fun FrameContext.building(buildingId: Int, z: Float, x: Float) {

    val building = buildings[buildingId]

    val roof = standart {
        baseColorTexture = texture("infcity/roof.jpg")
        pbr.metallic = 0.6f
    }
    val windows = standart {
        baseColorTexture = texture("infcity/dw.jpg")
        emissiveFactor = White
        set("windowTexture", texture("infcity/lw.jpg", wrap = TextureWrap.MirroredRepeat))
        pbr.metallic = 0.3f
    }

    fun Triangulation.toCustomMesh(id: String) = customMesh(id, this.points.size, this.indexes.size, POS, NORMAL, TEX) {
        pos(*points.toTypedArray())
        normal(*normals.toTypedArray())
        tex(*texs.toTypedArray())
        index(*indexes.toIntArray())
    }

    Renderable(
        roof,
        mesh = building.rf().toCustomMesh("roof-$buildingId"),
        transform = translate(x, 0f, z + 8f)
    )
    Renderable(
        windows,
        plugin("emission", "infcity/window.emission.plugin.frag"),
        mesh = building.lw().toCustomMesh("wnd-$buildingId"),
        transform = translate(x, 0f, z + 8f)
    )
}

private fun FrameContext.road(startZ: Float) = Renderable(
    standart {
        baseColorTexture = texture("infcity/road.jpg")
    },
    mesh = roadMesh(),
    transform = translate(startZ.z)
)

private fun FrameContext.roadMesh() = customMesh("road", 4, 6, POS, NORMAL, TEX) {
    pos(-0.5f, 0f, 0f).normal(1.y).tex(0f, 0f)
    pos(-0.5f, 0f, 32f).normal(1.y).tex(0f, 32f)
    pos(0.5f, 0f, 32f).normal(1.y).tex(1f, 32f)
    pos(0.5f, 0f, 0f).normal(1.y).tex(1f, 0f)
    index(0, 1, 2, 0, 2, 3)
}

private fun FrameContext.sidewalk(z: Float, x: Float) = Renderable(
    standart {
        baseColorTexture = texture("infcity/roof.jpg")
        triplanarScale = 0.5f
    },
    mesh = grassMesh(),
    transform = translate(x, 0f, z)
)

private fun FrameContext.grassMesh() = customMesh("grass", 4, 6, POS, NORMAL, TEX) {
    pos(0f, 0f, 0f).normal(1.y).tex(0f, 0f)
    pos(0f, 0f, 32f).normal(1.y).tex(0f, 1f)
    pos(32f, 0f, 32f).normal(1.y).tex(1f, 1f)
    pos(32f, 0f, 0f).normal(1.y).tex(1f, 0f)
    index(0, 1, 2, 0, 2, 3)
}

private fun FrameContext.gui() = Gui {
    Column {
        Filler()
        Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}", height = 40, color = ColorRGBA(0x66FF55A0))
    }
}

private fun FrameContext.sky() {
    val sky = starrySky {
        colorness = 0.4f
        density = 20f
        size = 20f
        set("moonTexture", texture("infcity/moon.png"))
    }
    val moon = plugin("secsky", "infcity/moon.secsky.plugin.frag")
    Sky(sky, moon)
    PostProcess(fog())
}