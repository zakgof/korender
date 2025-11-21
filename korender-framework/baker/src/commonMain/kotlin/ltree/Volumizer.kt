package ltree

import com.zakgof.korender.Image3D
import com.zakgof.korender.PixelFormat
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlinx.coroutines.runBlocking
import ltree.generator.LTree
import tree.saveImage


fun KorenderContext.volumize(lTree: LTree): Pair<Image3D, Image3D> {

    val reso = 32
    val pixelsInSlice = 64 / 8
    val albedo3d = createImage3D(reso, reso, reso, PixelFormat.RGBA)
    val normal3d = createImage3D(reso, reso, reso, PixelFormat.RGBA)

    val minBB = Vec3(
        lTree.leaves.minOf { it.mount.x },
        lTree.leaves.minOf { it.mount.y },
        lTree.leaves.minOf { it.mount.z }
    )
    val maxBB = Vec3(
        lTree.leaves.maxOf { it.mount.x },
        lTree.leaves.maxOf { it.mount.y },
        lTree.leaves.maxOf { it.mount.z }
    )

    for (z in 0 until reso) {
        val pixelDepth = (maxBB.z - minBB.z) / reso
        val sliceCenter = Vec3((minBB.x + maxBB.x) * 0.5f, (minBB.y + maxBB.y) * 0.5f, minBB.z + (maxBB.z - minBB.z) * ((z + 0.5f) / reso))
        val camera = camera(sliceCenter - 5.z, 1.z, 1.y)
        val projection = projection((maxBB.x - minBB.x) * 1.1f, (maxBB.y - minBB.y) * 1.1f, 5f - pixelDepth * pixelsInSlice * 0.5f, 5f + pixelDepth * pixelsInSlice * 0.5f)

        println("Z=$z: z range: ${minBB.z}..${maxBB.z} NEAR: ${projection.near} FAR: ${projection.far}")

        val image = runBlocking {
            captureFrame(reso, reso, camera, projection) {
                AmbientLight(White)
                renderLTree(lTree, "capture-$z", "ltree/oak.png")
            }.await()
        }
        saveImage(image, "png", "D:/p/test-$z.png")

        for (x in 0 until reso) {
            for (y in 0 until reso) {
                val color = image.pixel(x, reso - 1 - y)
                albedo3d.setPixel(x, y, z, color)
            }
        }
    }
    for (x in 0 until reso) {
        for (y in 0 until reso) {
            for (z in 0 until reso) {
                val center = Vec3(
                    minBB.x + (maxBB.x - minBB.x) * ((x + 0.5f) / reso),
                    minBB.y + (maxBB.y - minBB.y) * ((y + 0.5f) / reso),
                    minBB.z + (maxBB.z - minBB.z) * ((z + 0.5f) / reso)
                )
                val nearbyLeavesCenter = lTree.leaves
                    .filter { (it.mount - center).lengthSquared() < 9f }
                    .fold(0.y) { a, l -> l.mount * (1.0f / (l.mount - center).length()) }
                val n = (center - nearbyLeavesCenter).normalize()
                val color = ColorRGBA(n.x * 0.5f + 0.5f, n.y * 0.5f + 0.5f, n.z * 0.5f + 0.5f, 1.0f)
                normal3d.setPixel(x, y, z, color)
            }
        }
    }
    return albedo3d to normal3d
}