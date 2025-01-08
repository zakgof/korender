package procgen

import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

private fun cube(x: Int, y: Int, z: Int) = x or (y shl 8) or (z shl 16)

private val Int.cx: Int
    get() = this and 0xFF
private val Int.cy: Int
    get() = (this shr 8) and 0xFF
private val Int.cz: Int
    get() = (this shr 16) and 0xFF

class Generator(private val xsize: Int, private val ysize: Int, private val height: Int) {

    fun run(block: Context.() -> Unit): Triangulation {
        val cubes = collectCubes(block)
        return triangulate(cubes)
    }

    private fun collectCubes(block: Context.() -> Unit): MutableSet<Int> {
        val cubes = mutableSetOf<Int>()
        var level = Level(xsize, ysize)
        for (z in 0 until height) {
            block.invoke(level)
            cubes += level.cubes
            level = level.up()
        }
        return cubes
    }

    private fun triangulate(cubes: Set<Int>): Triangulation {
        val points = mutableListOf<Vec3>()
        val normals = mutableListOf<Vec3>()
        val texs = mutableListOf<Vec2>()
        val indexes = mutableListOf<Int>()
        var faces = 0

        fun v3(x: Int, y: Int, z: Int) = Vec3(x.toFloat(), y.toFloat(), z.toFloat())

        cubes.forEach {
            // left
            if (it.cx == 0 || !cubes.contains(cube(it.cx - 1, it.cy, it.cz))) {
                points += v3(it.cx, it.cz, it.cy)
                points += v3(it.cx, it.cz + 1, it.cy)
                points += v3(it.cx, it.cz + 1, it.cy + 1)
                points += v3(it.cx, it.cz, it.cy + 1)
                normals += List(4) { -1.x }
                texs += listOf(Vec2(0f, 0f), Vec2(0f, 1f), Vec2(1f, 1f), Vec2(1f, 0f))
                indexes += listOf(faces * 4 + 0, faces * 4 + 2, faces * 4 + 1, faces * 4 + 0, faces * 4 + 3, faces * 4 + 2)
                faces++
            }
            // right
            if (!cubes.contains(cube(it.cx + 1, it.cy, it.cz))) {
                points += v3(it.cx + 1, it.cz, it.cy + 1)
                points += v3(it.cx + 1, it.cz + 1, it.cy + 1)
                points += v3(it.cx + 1, it.cz + 1, it.cy)
                points += v3(it.cx + 1, it.cz, it.cy)
                normals += List(4) { 1.x }
                texs += listOf(Vec2(0f, 0f), Vec2(0f, 1f), Vec2(1f, 1f), Vec2(1f, 0f))
                indexes += listOf(faces * 4 + 0, faces * 4 + 2, faces * 4 + 1, faces * 4 + 0, faces * 4 + 3, faces * 4 + 2)
                faces++
            }
            // bottom
            if (it.cz == 0 || !cubes.contains(cube(it.cx, it.cy, it.cz - 1))) {
                points += v3(it.cx, it.cz, it.cy)
                points += v3(it.cx, it.cz, it.cy + 1)
                points += v3(it.cx + 1, it.cz, it.cy + 1)
                points += v3(it.cx + 1, it.cz, it.cy)
                normals += List(4) { -1.y }
                texs += listOf(Vec2(0f, 0f), Vec2(0f, 1f), Vec2(1f, 1f), Vec2(1f, 0f))
                indexes += listOf(faces * 4 + 0, faces * 4 + 2, faces * 4 + 1, faces * 4 + 0, faces * 4 + 3, faces * 4 + 2)
                faces++
            }
            // top
            if (!cubes.contains(cube(it.cx, it.cy, it.cz + 1))) {
                points += v3(it.cx, it.cz + 1, it.cy + 1)
                points += v3(it.cx, it.cz + 1, it.cy)
                points += v3(it.cx + 1, it.cz + 1, it.cy)
                points += v3(it.cx + 1, it.cz + 1, it.cy + 1)
                normals += List(4) { 1.y }
                texs += listOf(Vec2(0f, 0f), Vec2(0f, 1f), Vec2(1f, 1f), Vec2(1f, 0f))
                indexes += listOf(faces * 4 + 0, faces * 4 + 2, faces * 4 + 1, faces * 4 + 0, faces * 4 + 3, faces * 4 + 2)
                faces++
            }
            // front
            if (!cubes.contains(cube(it.cx, it.cy + 1, it.cz))) {
                points += v3(it.cx, it.cz, it.cy + 1)
                points += v3(it.cx, it.cz + 1, it.cy + 1)
                points += v3(it.cx + 1, it.cz + 1, it.cy + 1)
                points += v3(it.cx + 1, it.cz, it.cy + 1)
                normals += List(4) { 1.z }
                texs += listOf(Vec2(0f, 0f), Vec2(0f, 1f), Vec2(1f, 1f), Vec2(1f, 0f))
                indexes += listOf(faces * 4 + 0, faces * 4 + 2, faces * 4 + 1, faces * 4 + 0, faces * 4 + 3, faces * 4 + 2)
                faces++
            }
            // back
            if (it.cy == 0 || !cubes.contains(cube(it.cx, it.cy - 1, it.cz))) {
                points += v3(it.cx + 1, it.cz, it.cy)
                points += v3(it.cx + 1, it.cz + 1, it.cy)
                points += v3(it.cx, it.cz + 1, it.cy)
                points += v3(it.cx, it.cz, it.cy)
                normals += List(4) { -1.z }
                texs += listOf(Vec2(0f, 0f), Vec2(0f, 1f), Vec2(1f, 1f), Vec2(1f, 0f))
                indexes += listOf(faces * 4 + 0, faces * 4 + 2, faces * 4 + 1, faces * 4 + 0, faces * 4 + 3, faces * 4 + 2)
                faces++
            }
        }
        return CTriangulation(points, normals, texs, indexes)
    }

    private class CTriangulation(
        override val points: List<Vec3>,
        override val normals: List<Vec3>,
        override val texs: List<Vec2>,
        override val indexes: List<Int>
    ) : Triangulation

    private class Level(override val level: Int, val cubes: MutableSet<Int>) : Context {

        override val left: Int = cubes.minOf { it.cx }
        override val right: Int = cubes.maxOf { it.cx }
        override val bottom: Int = cubes.minOf { it.cy }
        override val top: Int = cubes.maxOf { it.cy }

        fun up() = Level(level + 1, cubes.map { cube(it.cx, it.cy, it.cz + 1) }.toMutableSet())

        override fun corner(x: Int, y: Int) {
            cubes.removeAll {
                ((x < 0 && it.cx < -x) || (x > 0 && it.cx > x))
                        && ((y < 0 && it.cy < -y) || (y > 0 && it.cy > y))
            }
        }

        override fun flatx(x: Int) {
            cubes.removeAll { (x < 0 && it.cx < -x) || (x > 0 && it.cx > x) }
        }

        override fun flaty(y: Int) {
            cubes.removeAll { (y < 0 && it.cy < -y) || (y > 0 && it.cy > y) }
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
    }

    interface Triangulation {
        val points: List<Vec3>
        val normals: List<Vec3>
        val texs: List<Vec2>
        val indexes: List<Int>
    }
}
