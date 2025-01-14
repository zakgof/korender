package city

import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z


class Recto(val xmin: Int, val xmax: Int, val ymin: Int, val ymax: Int, val zmin: Int, val zmax: Int)

private fun cube(x: Int, y: Int, z: Int) = x or (y shl 8) or (z shl 16)

private fun Int.withX(x: Int): Int = cube(x, this.cy, this.cz)
private fun Int.withY(y: Int): Int = cube(this.cx, y, this.cz)
private fun Int.withZ(z: Int): Int = cube(this.cx, this.cy, z)
private fun Set<Int>.inRect(x1: Int, x2: Int, y1: Int, y2: Int, z1: Int, z2: Int) =
    this.filter { (it.cx in x1..x2) && (it.cy in y1..y2) && (it.cz in z1..z2) }.toSet()

private fun Set<Int>.filled(x1: Int, x2: Int, y1: Int, y2: Int, z1: Int, z2: Int) =
    inRect(x1, x2, y1, y2, z1, z2).size == (x2 - x1 + 1) * (y2 - y1 + 1) * (z2 - z1 + 1)

private val Int.cx: Int
    get() = this and 0xFF
private val Int.cy: Int
    get() = (this shr 8) and 0xFF
private val Int.cz: Int
    get() = (this shr 16) and 0xFF

class Generator {

    private val lightWindow = CTriangulation()
    private val roof = CTriangulation()

    fun building(xoffset: Int, yoffset: Int, xsize: Int, ysize: Int, height: Int, block: Context.() -> Unit): Generator {
        val cubes = collectCubes(xsize, ysize, height, block)
        triangulate(xoffset, yoffset, cubes)
        return this
    }

    fun lw(): Triangulation = lightWindow
    fun rf(): Triangulation = roof

    private fun collectCubes(xsize: Int, ysize: Int, height: Int, block: Context.() -> Unit): MutableSet<Int> {
        val cubes = mutableSetOf<Int>()
        var level = Level(xsize, ysize)
        for (z in 0 until height) {
            block.invoke(level)
            cubes += level.cubes

            println("Level $z    cubes ${level.cubes.size}")

            level = level.up()
        }
        println("=======")
        return cubes
    }

    private fun triangulate(xoffset: Int, yoffset: Int, cubes: Set<Int>) {

        fun v3(cx: Int, cz: Int, cy: Int) = Vec3((cx + xoffset).toFloat(), cz.toFloat(), (cy + yoffset).toFloat())

        // FRONT
        cubes.filter { !cubes.contains(cube(it.cx, it.cy + 1, it.cz)) }
            .groupBy { it.cy }.values.forEach { layer ->
                val cells = layer.toMutableSet()
                while (cells.isNotEmpty()) {
                    val cell = cells.first()
                    var minz = cell.cz
                    var maxz = cell.cz
                    var minx = cell.cx
                    var maxx = cell.cx
                    while (cells.contains(cell.withZ(minz - 1))) {
                        minz--
                    }
                    while (cells.contains(cell.withZ(maxz + 1))) {
                        maxz++
                    }
                    while (cells.filled(minx - 1, minx - 1, cell.cy, cell.cy, minz, maxz)) {
                        minx--
                    }
                    while (cells.filled(maxx + 1, maxx + 1, cell.cy, cell.cy, minz, maxz)) {
                        maxx++
                    }
                    cells.removeAll(cells.inRect(minx, maxx, cell.cy, cell.cy, minz, maxz))

                    lightWindow.face(
                        v3(minx, minz, cell.cy + 1),
                        v3(minx, maxz + 1, cell.cy + 1),
                        v3(maxx + 1, maxz + 1, cell.cy + 1),
                        v3(maxx + 1, minz, cell.cy + 1),
                        1.z,
                        maxx + 1 - minx, maxz + 1 - minz
                    )
                }
            }

        // BACK
        cubes.filter { (it.cy == 0 || !cubes.contains(cube(it.cx, it.cy - 1, it.cz))) }
            .groupBy { it.cy }.values.forEach { layer ->
                val cells = layer.toMutableSet()
                while (cells.isNotEmpty()) {
                    val cell = cells.first()
                    var minz = cell.cz
                    var maxz = cell.cz
                    var minx = cell.cx
                    var maxx = cell.cx
                    while (cells.contains(cell.withZ(minz - 1))) {
                        minz--
                    }
                    while (cells.contains(cell.withZ(maxz + 1))) {
                        maxz++
                    }
                    while (cells.filled(minx - 1, minx - 1, cell.cy, cell.cy, minz, maxz)) {
                        minx--
                    }
                    while (cells.filled(maxx + 1, maxx + 1, cell.cy, cell.cy, minz, maxz)) {
                        maxx++
                    }
                    cells.removeAll(cells.inRect(minx, maxx, cell.cy, cell.cy, minz, maxz))

                    lightWindow.face(
                        v3(maxx + 1, minz, cell.cy),
                        v3(maxx + 1, maxz + 1, cell.cy),
                        v3(minx, maxz + 1, cell.cy),
                        v3(minx, minz, cell.cy),
                        -1.z,
                        maxx + 1 - minx, maxz + 1 - minz
                    )
                }
            }

        // LEFT
        cubes.filter { (it.cx == 0 || !cubes.contains(cube(it.cx - 1, it.cy, it.cz))) }
            .groupBy { it.cx }.values.forEach { layer ->
                val cells = layer.toMutableSet()
                while (cells.isNotEmpty()) {
                    val cell = cells.first()
                    var minz = cell.cz
                    var maxz = cell.cz
                    var miny = cell.cy
                    var maxy = cell.cy
                    while (cells.contains(cell.withZ(minz - 1))) {
                        minz--
                    }
                    while (cells.contains(cell.withZ(maxz + 1))) {
                        maxz++
                    }
                    while (cells.filled(cell.cx, cell.cx, miny - 1, miny - 1, minz, maxz)) {
                        miny--
                    }
                    while (cells.filled(cell.cx, cell.cx, maxy + 1, maxy + 1, minz, maxz)) {
                        maxy++
                    }
                    cells.removeAll(cells.inRect(cell.cx, cell.cx, miny, maxy, minz, maxz))

                    lightWindow.face(
                        v3(cell.cx, minz, miny),
                        v3(cell.cx, maxz + 1, miny),
                        v3(cell.cx, maxz + 1, maxy + 1),
                        v3(cell.cx, minz, maxy + 1),
                        -1.x,
                        maxy + 1 - miny, maxz + 1 - minz
                    )
                }
            }

        // RIGHT
        cubes.filter { !cubes.contains(cube(it.cx + 1, it.cy, it.cz)) }
            .groupBy { it.cx }.values.forEach { layer ->
                val cells = layer.toMutableSet()
                while (cells.isNotEmpty()) {
                    val cell = cells.first()
                    var minz = cell.cz
                    var maxz = cell.cz
                    var miny = cell.cy
                    var maxy = cell.cy
                    while (cells.contains(cell.withZ(minz - 1))) {
                        minz--
                    }
                    while (cells.contains(cell.withZ(maxz + 1))) {
                        maxz++
                    }
                    while (cells.filled(cell.cx, cell.cx, miny - 1, miny - 1, minz, maxz)) {
                        miny--
                    }
                    while (cells.filled(cell.cx, cell.cx, maxy + 1, maxy + 1, minz, maxz)) {
                        maxy++
                    }
                    cells.removeAll(cells.inRect(cell.cx, cell.cx, miny, maxy, minz, maxz))

                    lightWindow.face(
                        v3(cell.cx + 1, minz, maxy + 1),
                        v3(cell.cx + 1, maxz + 1, maxy + 1),
                        v3(cell.cx + 1, maxz + 1, miny),
                        v3(cell.cx + 1, minz, miny),
                        1.x,
                        maxy + 1 - miny, maxz + 1 - minz
                    )
                }
            }

        // ROOF
        cubes.filter { !cubes.contains(cube(it.cx, it.cy, it.cz + 1)) }
            .groupBy { it.cz }.values.forEach { layer ->
                val cells = layer.toMutableSet()
                while (cells.isNotEmpty()) {
                    val cell = cells.first()
                    var minx = cell.cx
                    var maxx = cell.cx
                    var miny = cell.cy
                    var maxy = cell.cy
                    while (cells.contains(cell.withY(miny - 1))) {
                        miny--
                    }
                    while (cells.contains(cell.withY(maxy + 1))) {
                        maxy++
                    }
                    while (cells.filled(minx, maxx, miny - 1, miny - 1, cell.cz, cell.cz)) {
                        miny--
                    }
                    while (cells.filled(minx, maxx, maxy + 1, maxy + 1, cell.cz, cell.cz)) {
                        maxy++
                    }
                    cells.removeAll(cells.inRect(minx, maxx, miny, maxy, cell.cz, cell.cz))

                    roof.face(
                        v3(minx, cell.cz + 1, maxy + 1),
                        v3(minx, cell.cz + 1, miny),
                        v3(maxx + 1, cell.cz + 1, miny),
                        v3(maxx + 1, cell.cz + 1, maxy + 1),
                        1.y,
                        maxx + 1 - minx, maxy + 1 - miny
                    )
                }
            }
    }

    private class CTriangulation(
        override val points: MutableList<Vec3> = mutableListOf(),
        override val normals: MutableList<Vec3> = mutableListOf(),
        override val texs: MutableList<Vec2> = mutableListOf(),
        override val indexes: MutableList<Int> = mutableListOf()
    ) : Triangulation {
        var faces = 0

        fun face(pos1: Vec3, pos2: Vec3, pos3: Vec3, pos4: Vec3, normal: Vec3, ucells: Int, vcells: Int) {
            points += pos1
            points += pos2
            points += pos3
            points += pos4
            normals += List(4) { normal }
            texs += listOf(Vec2(0f, 0f), Vec2(0f, vcells.toFloat()), Vec2(ucells.toFloat(), vcells.toFloat()), Vec2(ucells.toFloat(), 0f))
            indexes += listOf(faces * 4 + 0, faces * 4 + 2, faces * 4 + 1, faces * 4 + 0, faces * 4 + 3, faces * 4 + 2)
            faces++
        }
    }

    private class Level(override val level: Int, val cubes: MutableSet<Int>) : Context {

        override val left: Int = cubes.minOfOrNull { it.cx } ?: -1
        override val right: Int = cubes.maxOfOrNull { it.cx + 1 } ?: -1
        override val bottom: Int = cubes.minOfOrNull { it.cy } ?: -1
        override val top: Int = cubes.maxOfOrNull { it.cy + 1 } ?: -1

        fun up() = Level(level + 1, cubes.map { cube(it.cx, it.cy, it.cz + 1) }.toMutableSet())

        override fun corner(x: Int, y: Int) {
            cubes.removeAll {
                ((x < 0 && it.cx < -x) || (x >= 0 && it.cx >= x))
                        && ((y < 0 && it.cy < -y) || (y >= 0 && it.cy >= y))
            }
        }

        override fun flatx(x: Int) {
            cubes.removeAll { (x < 0 && it.cx < -x) || (x >= 0 && it.cx >= x) }
        }

        override fun flaty(y: Int) {
            cubes.removeAll { (y < 0 && it.cy < -y) || (y >= 0 && it.cy >= y) }
        }

        constructor(xsize: Int, ysize: Int) : this(
            0,
            IntArray(xsize * ysize) { cube(it % xsize, it / xsize, 0) }.toMutableSet()
        )

    }

    interface Context {
        val level: Int
        val left: Int
        val right: Int
        val top: Int
        val bottom: Int

        fun corner(x: Int, y: Int)
        fun flatx(x: Int)
        fun flaty(y: Int)

        fun symcorner(x: Int, y: Int) {
            corner(-left - x, -bottom - y)
            corner(right - x, -bottom - y)
            corner(-left - x, top - y)
            corner(right - x, top - y)
        }

        fun square(x: Int, y: Int) {
            flatx(-left - x)
            flatx(right - x)
            flaty(-bottom - y)
            flaty(top - y)
        }

        fun squareTo(x: Int, y: Int) {
            val centx = (left + right) / 2
            val centy = (top + bottom) / 2
            flatx(-centx - x)
            flatx(centx + x)
            flaty(-centy - y)
            flaty(centy + y)
        }
    }
}
