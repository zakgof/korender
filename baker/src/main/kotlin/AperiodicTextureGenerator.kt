package com.zakgof

import java.awt.image.BufferedImage
import java.awt.image.Raster
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

typealias Sampler = (Int, Int) -> FloatArray

fun main() {

    val arr = FloatArray(3)
    val src = ImageIO.read(File("E:\\Business\\render2024\\korender\\input.png")).raster

    val colors = 4

    val metaTile = createTiles(src, 256, 16, colors, 2)
    ImageIO.write(metaTile, "PNG", File("E:\\Business\\render2024\\korender\\meta.png"))


    val indices = generateIndexTexture(1024, 1024, colors)
    val indextex = Images.create(1024, 1024) { x, y, ->
        val index = indices[x][y]
        arr[0] = index.toFloat()
        arr[1] = 0f
        arr[2] = 0f
        arr
    }
    ImageIO.write(indextex, "PNG", File("E:\\Business\\render2024\\korender\\indextex.png"))


    val metaRaster = metaTile.raster

    val xblocks = 8
    val yblocks = 8
    val image = Images.create(xblocks * 256, yblocks * 256) { x, y ->
        val xtile = x / 256
        val ytile = y / 256
        val index = indices[xtile][ytile]
        val xoffset = index / (colors * colors)
        val yoffset = index % (colors * colors)

        metaRaster.getPixel(x - xtile * 256 + xoffset * 256, y - ytile * 256 + yoffset * 256, arr)
    }
    ImageIO.write(image, "PNG", File("E:\\Business\\render2024\\korender\\output.png"))
}

private fun generateIndexTexture(xblocks: Int, yblocks: Int, colors: Int): Array<IntArray> {
    val corners = Array(xblocks + 1) { IntArray(yblocks + 1) { Random.nextInt(colors) } }
    for (x in 0..xblocks) {
        corners[x][yblocks] = corners[x][0]
    }
    for (y in 0..xblocks) {
        corners[xblocks][y] = corners[0][y]
    }
    val indices = Array(xblocks) { x ->
        IntArray(yblocks) { y ->
            corners[x][y] * (colors * colors * colors) + corners[x + 1][y] * (colors * colors) + corners[x][y + 1] * colors + corners[x + 1][y + 1]
        }
    }
    return indices
}

fun createTiles(src: Raster, side: Int, seam: Int, colors: Int, srcRows: Int): BufferedImage {

    val sampleSide = side + seam
    val half = (side - seam) / 2
    println("Expected sample size: at least ${sampleSide * colors / srcRows} x ${sampleSide * srcRows}")

    val arr1 = FloatArray(3)
    val arr2 = FloatArray(3)
    val arrs = Array(colors) { FloatArray(3) }
    val inputs = Array<Sampler>(colors) { { x, y -> src.getPixel(x + sampleSide * (it%srcRows), y+ sampleSide * (it/srcRows), arrs[it]) } }
    val tiles = Array(colors * colors) { Array<Raster?>(colors * colors) { null } }

    for (tl in 0 until colors) {
        for (tr in 0 until colors) {
            for (bl in 0 until colors) {
                for (br in 0 until colors) {
                    val top = minCutStitchHorizontal(
                        { x, y -> inputs[tl].invoke(x + sampleSide / 2, y + sampleSide / 2) },
                        { x, y -> inputs[tr].invoke(x, y + sampleSide / 2) },
                        sampleSide / 2,
                        sampleSide / 2,
                        seam
                    ).raster
                    val bottom = minCutStitchHorizontal(
                        { x, y -> inputs[bl].invoke(x + sampleSide / 2, y) },
                        { x, y -> inputs[br].invoke(x, y) },
                        sampleSide / 2,
                        sampleSide / 2,
                        seam
                    ).raster
                    val tile = minCutStitchVertical(
                        { x, y -> top.getPixel(x, y, arr1) },
                        { x, y -> bottom.getPixel(x, y, arr2) },
                        top.width,
                        top.height,
                        seam
                    ).raster
                    val index = tl * (colors * colors * colors) + tr * (colors * colors) + bl * colors + br
                    val xoffset = index / (colors * colors)
                    val yoffset = index % (colors * colors)
                    tiles[xoffset][yoffset] = tile
                }
            }
        }
    }
    return Images.create(side * colors * colors, side * colors * colors) { x, y ->
        val xoffset = x / side
        val yoffset = y / side
        tiles[xoffset][yoffset]!!.getPixel(x - xoffset * side, y - yoffset * side, arr1)
    }
}

fun minCutStitchHorizontal(
    src1: Sampler,
    src2: Sampler,
    w: Int,
    h: Int,
    seam: Int
): BufferedImage {
    val err = Array(seam) { FloatArray(h) }
    for (y in 0 until h) {
        for (x in 0 until seam) {
            err[x][y] = distance(src1.invoke(h - seam + x, y), src2.invoke(x, y))
        }
    }
    val cut = mincut(seam, h, err)
    return Images.create(w + w - seam, h) { x, y ->
//        if (x == w - seam + cut[y]) {
//            FloatArray(3)
//        } else
        if (x < w - seam + cut[y]) {
            src1.invoke(x, y)
        } else {
            src2.invoke(x - w + seam, y)
        }
    }
}

fun minCutStitchVertical(
    src1: Sampler,
    src2: Sampler,
    w: Int,
    h: Int,
    seam: Int
): BufferedImage {
    val err = Array(seam) { FloatArray(w) }
    for (x in 0 until w) {
        for (y in 0 until seam) {
            err[y][x] = distance(src1.invoke(x, h - seam + y), src2.invoke(x, y))
        }
    }
    val cut = mincut(seam, w, err)
    return Images.create(w, h + h - seam) { x, y ->
//        if (y == h - seam + cut[x]) {
//            FloatArray(3)
//        } else
        if (y < h - seam + cut[x]) {
            src1.invoke(x, y)
        } else {
            src2.invoke(x, y - h + seam)
        }
    }
}

private fun mincut(seam: Int, h: Int, err: Array<FloatArray>): IntArray {
    val cum = Array(seam) { FloatArray(h) }
    for (y in 0 until h) {
        for (x in 0 until seam) {
            cum[x][y] = err[x][y]
            if (y > 0) {
                cum[x][y] += (max(0, x - 1)..min(seam - 1, x + 1))
                    .minOf { cum[it][y - 1] }
            }
        }
    }
    val cut = IntArray(h)
    cut[h - 1] = (0 until seam).minBy { cum[it][h - 1] }
    for (y in h - 2 downTo 0) {
        cut[y] = (max(0, cut[y + 1] - 1)..min(seam - 1, cut[y + 1] + 1)).minBy { cum[it][y] }
    }
    return cut
}

private fun distance(p1: FloatArray, p2: FloatArray): Float {
    return (p1[0] - p2[0]) * (p1[0] - p2[0]) + (p1[1] - p2[1]) * (p1[1] - p2[1]) + (p1[2] - p2[2]) * (p1[2] - p2[2])
}
