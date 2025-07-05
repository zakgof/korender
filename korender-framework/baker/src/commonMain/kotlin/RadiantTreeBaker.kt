package com.zakgof.korender.baker

import androidx.compose.runtime.Composable
import com.zakgof.korender.Attributes.MODEL0
import com.zakgof.korender.Attributes.MODEL1
import com.zakgof.korender.Attributes.MODEL2
import com.zakgof.korender.Attributes.MODEL3
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.Korender
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.Vec3.Companion.ZERO
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.random.Random

@Composable
fun RadiantTreeBaker() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    val basePath = "D:\\kot\\dev\\assets\\" // TODO

    // val metaball = Metaball(20f, 1.0f) { sqrt(it * 0.05f) * (1f - it * 0.05f) * 10f }
    val metaball = Metaball(20f, 3.0f, 8000, 48) { (it * 0.05f).pow(0.1f) * (1f - it * 0.05f) * 10f }
    // val metaball = Metaball(20f, 4.0f, 4096, 32) { (it * 0.05f) * (1f - it * 0.05f) * 20f }


    val hull = QuickHull(metaball.points.map { it.pos }).run()
    val hullMesh = customMesh("hull", hull.points.size, hull.indexes.size, POS, NORMAL) {
        hull.points.forEach { pos(it.pos).normal(it.normal) }
        hull.indexes.forEach { index(it) }
    }
    val leaf = customMesh("leaf", 4, 6, POS, NORMAL, TEX, MODEL0, MODEL1, MODEL2, MODEL3) {
        pos(Vec3(-0.5f, -0.5f, 0f)).normal(1.z).tex(0f, 0f)
        pos(Vec3(0.5f, -0.5f, 0f)).normal(1.z).tex(1f, 0f)
        pos(Vec3(0.5f, 0.5f, 0f)).normal(1.z).tex(1f, 1f)
        pos(Vec3(-0.5f, 0.5f, 0f)).normal(1.z).tex(0f, 1f)

        index(0, 1, 2, 0, 2, 3)
    }
    val leafInstances = metaball.points.mapIndexed { index, pt ->
        val quaternion = (0..64)
            .map { index * 1023 + it }
            .map { Quaternion.fromAxisAngle(Vec3.random(it), Random(index * 777 + it).nextFloat() * 100f) }
            .maxBy { (it * 1.z) * pt.n }

        rotate(quaternion)
            .scale(0.8f)
            .translate((pt.pos - hull.center))
    }

    val radiantImages = captureEnv(resolution = 128, near = 0.2f, far = 15f, insideOut = true) {
        renderHull(hullMesh, ZERO, radiantCapture(15f))
    }
    saveCubeMap(radiantImages, basePath + "radiant-")

    val radiantNormalImages = captureEnv(resolution = 128, near = 0.2f, far = 15f, insideOut = true) {
        renderHull(hullMesh, ZERO, normalCapture())
    }
    saveCubeMap(radiantNormalImages, basePath + "radiant-normal-")

    val normalImages = captureEnv(resolution = 128, near = 0.2f, far = 15f, insideOut = true) {
        renderTree(leaf, leafInstances, normalCapture())
    }
    saveCubeMap(normalImages, basePath + "normal-")

    val albedoImages = captureEnv(resolution = 256, near = 0.2f, far = 15f, insideOut = true) {
        AmbientLight(white(1f))
        renderTree(leaf, leafInstances)
    }
    saveCubeMap(albedoImages, basePath + "albedo-")

    Frame {
        projection = frustum(3f * width / height, 3f, 3f, 100f)
        camera = camera(40.z, -1.z, 1.y)
        AmbientLight(white(0.2f))
        DirectionalLight(Vec3(2.0f, 0.0f, -2.0f), white(3f))
        Billboard(
            base(metallicFactor = 0f, roughnessFactor = 0.9f),
            billboard(scale = Vec2(30.0f, 30.0f), position = -15.x),
            radiant(
                radiantTexture = cubeTexture("radiant", radiantImages),
                radiantNormalTexture = cubeTexture("radiant-normal", radiantNormalImages),
                colorTexture = cubeTexture("albedo", albedoImages),
                normalTexture = cubeTexture("normal", normalImages)
            )
        )
        renderHull(hullMesh, 15.x)
        renderTree(leaf, leafInstances)
    }
}

fun saveCubeMap(images: CubeTextureImages, pathPrefix: String) {
    images.entries.map {
        val img = it.value
        val bytes = img.toRaw()
        val bi = BufferedImage(img.width, img.height, TYPE_INT_RGB)
        val raster = bi.raster
        val pixel = IntArray(3)
        for (x in 0 until img.width) {
            for (y in 0 until img.height) {
                pixel[0] = bytes[(x + y * img.width) * 4].toUByte().toInt()
                pixel[1] = bytes[(x + y * img.width) * 4 + 1].toUByte().toInt()
                pixel[2] = bytes[(x + y * img.width) * 4 + 2].toUByte().toInt()
                raster.setPixel(x, y, pixel)
            }
        }
        ImageIO.write(bi, "jpg", File("${pathPrefix}${it.key.toString().lowercase()}.jpg"))
    }
}

private fun FrameContext.renderHull(hullMesh: MeshDeclaration, offset: Vec3 = ZERO, vararg mods: MaterialModifier) {
    Renderable(
        base(color = ColorRGBA.Red),
        *mods,
        mesh = hullMesh,
        transform = translate(offset)
    )
}

private fun FrameContext.renderTree(leaf: MeshDeclaration, leafInstances: List<Transform>, vararg mods: MaterialModifier) {
    Renderable(
        base(colorTexture = texture("model/leaf.png")),
        *mods,
        mesh = leaf,
        transparent = true,
        instancing = instancing(
            id = "metapoints",
            count = leafInstances.size,
            dynamic = false
        ) {
            leafInstances.forEach { Instance(it) }
        })
}

