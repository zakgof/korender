package island

import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.buffer.put
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import island.pixelmap.ChannelMap
import island.pixelmap.Float2PixelMap
import island.pixelmap.FloatPixelMap
import island.pixelmap.channel
import island.pixelmap.channels
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.atan2
import kotlin.random.Random

// 0.1 - water

const val seed = 7
val noise1 = Perlin1D(seed)
val noise2 = Perlin2D(seed)
val random = Random(seed)

fun main() {

    val root = File(System.getProperty("projectRoot") ?: "D:\\p\\dev\\korender\\korender-framework\\baker")
    val islandRoot = File(root, "../examples/src/commonMain/composeResources/files/island")

    val heightMap = Float2PixelMap(512)
    val colorMap = ChannelMap(512)
    val sdf = Sdf(256)


    heightMap.populate { pt -> ht(pt) }

    val runwaySeed = Vec2(0.3f + 0.3f * random.nextFloat(), 0.3f + 0.3f * random.nextFloat())
    val cells = seedCells(heightMap, runwaySeed)
    fillRoads(cells, sdf)

    val blocks = cells.map { it.toBlock() }
    colorMap.populate { pt ->
        color(pt, heightMap, cells)
    }

    val buildings = seedBuildings(blocks)
    val trees = seedTrees(colorMap, heightMap, runwaySeed)

    heightMap.save(File(islandRoot, "terrain/height.png"))
    colorMap.save(File(islandRoot, "terrain/color.png"))
    sdf.save(File(islandRoot, "terrain/sdf.png"))

    saveBuildings(buildings, File(islandRoot, "building/buildings.bin"))
    saveTrees(trees, File(islandRoot, "tree/trees.bin"))

    val runWayDir = Vec2.random(seed)
    saveRunway(runwaySeed + runWayDir * 0.14f, runwaySeed - runWayDir * 0.14f, File(islandRoot, "terrain/runway.bin"))
}

//   0.1 sea level
//   0.11..0.13 flat
fun ht(pt: Vec2): Float {

    val phi = atan2(pt.y - 0.5f, pt.x - 0.5f)
    val radius = (pt - Vec2(0.5f, 0.5f)).length()
    val targetRadius = 0.32f +
            0.02f * noise1.noise(phi * 4f / PI) +
            0.01f * noise1.noise(phi * 8f / PI) +
            0.005f * noise1.noise(phi * 16f / PI)

    val flatIsland =
        0.12f * (1f + 0.2f * noise2.noise(pt.x * 5f, pt.y * 5f)) *
                (1f - smoothstep(
                    targetRadius,
                    targetRadius + 0.23f + 0.05f * noise1.noise(phi * 7f / PI),
                    radius
                ))

    return flatIsland
}


private fun smoothstep(p1: Float, p2: Float, x: Float): Float {
    val t = ((x - p1) / (p2 - p1)).coerceIn(0f, 1f)
    return (t * t * t * t * t) - 5f * (t * t * t * t) + 5f * (t * t * t)
}

fun color(pt: Vec2, heightMap: FloatPixelMap, cells: Set<Cell>): FloatArray {

    val h = heightMap[pt]

    if (h <= 0.11)
        return channels(0, 1, smoothstep(0.109f, 0.110f, h))

    if (h >= 0.15)
        return channels(1, 2, smoothstep(0.145f, 0.150f, h))

    val cell = cells.firstOrNull { it.ptIn(pt) }
    if (cell != null)
        return channel(3, 1f)

    return channel(1, 1f)
}

fun seedCells(heightMap: FloatPixelMap, runwaySeed: Vec2): Set<Cell> {
    val cellz = 16
    val nodes = grid(cellz).filter {
        val pt = Vec2(it.first.toFloat() / cellz, it.second.toFloat() / cellz)
        val h = heightMap[pt]
        h > 0.113f && h < 0.155f && (pt - runwaySeed).length() > 0.15f
    }.toSet()
    return nodes.filter {
        nodes.contains((it.first + 1) to it.second) &&
                nodes.contains(it.first to (it.second + 1)) &&
                nodes.contains((it.first + 1) to (it.second + 1))
    }.map {
        Cell(cellz, it.first, it.second)
    }.toSet()
}

private fun fillRoads(cells: Set<Cell>, sdf: Sdf) {
    cells.flatMap { cell -> cell.edges }
        .distinct()
        .forEach {
            sdf.spline(it.waypoints)
        }
}

private fun perturb(p: Vec2): Vec2 {
    val noiseFreq = 5f
    val noiseAmp = 0.015f
    val xw = p.x + noiseAmp * noise2.noise(p.x * noiseFreq, p.y * noiseFreq)
    val zw = p.y + noiseAmp * noise2.noise(p.x * noiseFreq + 16f, p.y * 10f + 16f)
    return Vec2(xw, zw)
}

fun steppy(a: Float, b: Float) = (0..16)
    .map { a + (it.toFloat() / 16f) * (b - a) }

private fun grid(cells: Int): List<Pair<Int, Int>> = (1..cells - 1)
    .flatMap { xx ->
        (1..cells - 1).map { yy ->
            xx to yy
        }
    }

class Road(val xmin: Int, val xmax: Int, val ymin: Int, val ymax: Int, val waypoints: List<Vec2>) {

    fun isRight(pt: Vec2): Boolean {
        val nearest = waypoints.indices.drop(1).minBy { (waypoints[it] - pt).lengthSquared() }
        val p1 = waypoints[nearest - 1]
        val p2 = waypoints[nearest]
        val a = p2 - p1
        val b = pt - p1
        return a.x * b.y - a.y * b.x > 0
    }

    override fun hashCode() = xmin + xmax * 128 + ymin * 128 * 123 + ymax * 128 * 128 * 128

    override fun equals(other: Any?) = (other is Road) && (xmin == other.xmin) && (xmax == other.xmax) && (ymin == other.ymin) && (ymax == other.ymax)
}


class Cell(cellFactor: Int, xx: Int, yy: Int) {

    val x1 = (xx.toFloat()) / cellFactor
    val x2 = (xx.toFloat() + 1.0f) / cellFactor
    val y1 = (yy.toFloat()) / cellFactor
    val y2 = (yy.toFloat() + 1.0f) / cellFactor

    val edges = mutableListOf<Road>()

    init {
        edges += Road(xx, xx + 1, yy, yy, steppy(x1, x2).map { perturb(Vec2(it, y1)) })
        edges += Road(xx, xx + 1, yy + 1, yy + 1, steppy(x2, x1).map { perturb(Vec2(it, y2)) })
        edges += Road(xx, xx, yy, yy + 1, steppy(y2, y1).map { perturb(Vec2(x1, it)) })
        edges += Road(xx + 1, xx + 1, yy, yy + 1, steppy(y1, y2).map { perturb(Vec2(x2, it)) })
    }

    fun ptIn(pt: Vec2) = edges.all { it.isRight(pt) }

    fun toBlock(): Pair<Vec2, Vec2> {
        return Vec2(x1, y1) to Vec2(x2, y2)
    }
}

fun seedBuildings(blocks: List<Pair<Vec2, Vec2>>) =
    blocks.map {
        Vec3(it.first.x, 0.0f, it.first.y) to Vec3(it.second.x, random.nextFloat() * 0.7f + 0.3f, it.second.y)
    }

fun seedTrees(colorMap: ChannelMap, heightMap: Float2PixelMap, runwaySeed: Vec2): List<Vec3> {
    val count = 80
    return (0 until count).map {
        val pt = generateSequence { Vec2(random.nextFloat(), random.nextFloat()) }
            .first { pt ->
                val color = colorMap.get(colorMap.toPix(pt.x), colorMap.toPix(pt.y))
                color[1] > 0.99f && (pt - runwaySeed).length() > 0.15f
            }
        Vec3(pt.x, heightMap[pt], pt.y)
    }
}

fun saveBuildings(buildings: List<Pair<Vec3, Vec3>>, file: File) = save(buildings, buildings.size * 2 * 3 * 4, file) { trees, nb ->
    buildings.forEach {
        nb.put(it.first)
        nb.put(it.second)
    }
}

fun saveTrees(trees: List<Vec3>, file: File) = save(trees, trees.size * 3 * 4, file) { trees, nb ->
    trees.forEach {
        nb.put(it)
    }
}

fun saveRunway(rw1: Vec2, rw2: Vec2, file: File) = save(rw1 to rw2, 16, file) { pts, nb ->
    nb.put(pts.first.x)
    nb.put(pts.first.y)
    nb.put(pts.second.x)
    nb.put(pts.second.y)
}

fun <T> save(data: T, size: Int, file: File, serializer: (T, NativeByteBuffer) -> Unit) {
    val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN)
    val nb = NativeByteBuffer(buffer)

    serializer(data, nb)

    nb.rewind()
    FileOutputStream(file).use { fos ->
        fos.channel.write(buffer)
    }
}

