package island

import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec2
import kotlin.math.atan2
import kotlin.random.Random

// 0.1 - water
// 0.2 - flat island

private val Int.p: Float
    get() = (this.toFloat() + 0.5f) / 255f

val seed = 7
val noise1 = Perlin1D(seed)
val noise2 = Perlin2D(seed)
val random = Random(seed)

fun main() {

    val rootPath = "D:\\p\\dev\\korender\\korender-framework\\examples\\src\\commonMain\\composeResources\\files\\hybridterrain"

    val mountainMapa = Mapa(512)
    Erosion(mountainMapa)
    mountainMapa.save("$rootPath\\mountain.png")

    val heightMapa = Mapa(512)
    val colorMapa = Mapa(512)
    val sdf = Sdf(256)

    val mt = seedMountain(random)
    heightMapa.populate { pt ->
        ht(pt, mt, mountainMapa)
    }

    val cells = seedCells(random, heightMapa)
    fillRoads(cells, sdf)
    colorMapa.populate { pt ->
        color(pt, heightMapa, cells)
    }

    heightMapa.save2("$rootPath\\height.png")
    colorMapa.save("$rootPath\\color.png")
    sdf.save("$rootPath\\sdf.png")
}


fun seedMountain(random: Random) =
    Vec2(0.3f + 0.4f * random.nextFloat(), 0.3f + 0.4f * random.nextFloat())

//   0.1 sea level
//   0.11..0.13 flat
fun ht(pt: Vec2, mtCenter: Vec2, mountain: Mapa): Float {

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

    val c = (pt - mtCenter).lengthSquared()

    return flatIsland + mountain[(pt - mtCenter) * 2.0f + Vec2(0.5f, 0.5f)]
}


private fun smoothstep(p1: Float, p2: Float, x: Float): Float {
    val t = ((x - p1) / (p2 - p1)).coerceIn(0f, 1f)
    return (t * t * t * t * t) - 5f * (t * t * t * t) + 5f * (t * t * t)
}

fun color(pt: Vec2, heightMapa: Mapa, cells: Set<Cell>): Float {

    val h = heightMapa[pt]
    if (h < 0.11f)
        return 0.p
    if (h > 0.160f)
        return 20.p

    val cell = cells.firstOrNull { it.ptIn(pt) }
    if (cell != null)
        return (1 + cell.color).p

    return 10.p
}

fun seedCells(random: Random, heightMapa: Mapa): Set<Cell> {
    val cellz = 10
    val nodes = grid(cellz).filter {
        val h = heightMapa[Vec2(it.first.toFloat() / cellz, it.second.toFloat() / cellz)]
        h > 0.113f && h < 0.155f
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
    val noiseAmp = 0.035f
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

    val edges = mutableListOf<Road>()

    val color = (xx * 31 + yy * 19) % 4

    init {
        val x1 = (xx.toFloat()) / cellFactor
        val x2 = (xx.toFloat() + 1.0f) / cellFactor
        val y1 = (yy.toFloat()) / cellFactor
        val y2 = (yy.toFloat() + 1.0f) / cellFactor
        edges += Road(xx, xx + 1, yy, yy, steppy(x1, x2).map { perturb(Vec2(it, y1)) })
        edges += Road(xx, xx + 1, yy + 1, yy + 1, steppy(x2, x1).map { perturb(Vec2(it, y2)) })
        edges += Road(xx, xx, yy, yy + 1, steppy(y2, y1).map { perturb(Vec2(x1, it)) })
        edges += Road(xx + 1, xx + 1, yy, yy + 1, steppy(y1, y2).map { perturb(Vec2(x2, it)) })
    }

    fun ptIn(pt: Vec2) = edges.all { it.isRight(pt) }
}