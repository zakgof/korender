package island

import com.zakgof.korender.math.Vec2
import kotlin.math.exp
import kotlin.math.min
import kotlin.random.Random


class Erosion(val map: Mapa, seed: Int = 0) {

    val random = Random(seed)
    val noise2 = Perlin2D(seed)

    init {
        initialize()
        (0 until 60000).forEach { _ ->
            runDroplet()
        }
    }

    fun initialize() {
        map.populate { pt ->
            0.4f * exp(-24f * (pt - Vec2(0.5f, 0.5f)).lengthSquared()) *
                    (1f +
                    0.1f * noise2.noise(pt.x * 4f, pt.y * 4f) +
                    0.02f * noise2.noise(pt.x * 16f, pt.y * 16f) +
                    0.005f * noise2.noise(pt.x * 64f, pt.y * 64f))
        }
    }

    fun runDroplet() {
        val droplet = Droplet(
            island.random.nextInt(map.side),
            island.random.nextInt(map.side),
            1f,
            0f
        )
        while (applyDroplet(droplet)) {}
    }

    private fun applyDroplet(droplet: Droplet): Boolean {
        val height = map.get(droplet.xx, droplet.yy)
        val minNeighborHeight = minNeighborHeight(droplet.xx, droplet.yy)
        val delta = min((minNeighborHeight - height) * 0.4f, 0.1f)
        // val delta = max(-0.01f * droplet.water, minDelta)
        map.set(droplet.xx, droplet.yy, height + delta)

        val next = neighbors(droplet.xx, droplet.yy)
            .minBy {
                map.get(it.first, it.second) + random.nextFloat() * 0.06f
            }
        if (next.first == 0 || next.first == map.side - -1 || next.second == 0 || next.second == map.side - 1) {
            return false
        }
        droplet.water -= 0.01f
        droplet.xx = next.first
        droplet.yy = next.second

        // println("Droplet ${droplet.position}  water ${droplet.water}")

        return droplet.water > 0f
    }


    private fun minNeighborHeight(xx: Int, yy: Int) =
        neighbors(xx, yy)
            .minOf { map.get(it.first, it.second) }


    private fun neighbors(xx: Int, yy: Int): List<Pair<Int, Int>> = listOf(
        xx - 1 to yy,
        xx + 1 to yy,
        xx to yy - 1,
        xx to yy + 1
    ).filter { it.first >= 0 && it.first < map.side && it.second >= 0 && it.second < map.side }


//    private fun neighbors(xx: Int, yy: Int): List<Pair<Int, Int>> =
//        (xx - 1..xx + 1).flatMap { x ->
//            (yy - 1..yy + 1).map { y -> x to y }
//        }.filter { (it.first != xx || it.second != yy) && it.first >= 0 && it.first < map.side && it.second >= 0 && it.second < map.side }

    private class Droplet(
        var xx: Int,
        var yy: Int,
        var water: Float,
        var deposit: Float
    )

}

