package island

import com.zakgof.korender.math.Vec2
import kotlin.math.exp
import kotlin.random.Random


class Erosion(val map: Mapa, seed: Int = 0) {

    val random = Random(seed)
    val noise2 = Perlin2D(seed)

    init {
        initialize()
        (0 until 100000).forEach { _ ->
            runDroplet()
        }
    }

    fun initialize() {
        map.populate { pt ->
            exp(-12f * (pt - Vec2(0.5f, 0.5f)).lengthSquared()) +
                    0.1f * noise2.noise(pt.x * 16f, pt.y * 16f)
        }
    }

    fun runDroplet() {
        val position = Vec2(random.nextFloat(), random.nextFloat())
        val droplet = Droplet(position, 1f, 0f)
        do {
            applyDroplet(droplet)
        } while (updateDroplet(droplet))
    }

    private fun applyDroplet(droplet: Droplet) {
        val radius = droplet.water * 0.003f
        val pixels = 0 // (radius * map.side * 3).toInt()
        val basex = map.toPix(droplet.position.x)
        val basey = map.toPix(droplet.position.y)

        val maxDelta = map.gradient(droplet.position).length() * 0.5f / map.side
        val delta = (-droplet.water * 0.01f).coerceIn(-maxDelta, maxDelta)

        for (xx in basex - pixels..basex + pixels) {
            for (yy in basey - pixels..basey + pixels) {
                if (xx >= 0 && xx < map.side && yy >= 0 && yy < map.side) {
                    val p = map.toVec2(xx, yy)
                    val w = exp(-(droplet.position - p).lengthSquared() / (4f * radius * radius))
                    val diff = delta * w
                    map.set(xx, yy, map.get(xx, yy) + diff)
                }
            }
        }
    }

    private fun updateDroplet(droplet: Droplet): Boolean {
        val gradient = map.gradient(droplet.position)
        if (gradient.lengthSquared() < 0.5f)
            return false // pit

        droplet.position -= gradient.normalize() * (1f / map.side)
        droplet.water -= 0.01f

        // println("Droplet ${droplet.position}  water ${droplet.water}")

        return (droplet.position.x >= 0f && droplet.position.x <= 1f &&
                droplet.position.y >= 0f && droplet.position.y <= 1f &&
                droplet.water > 0f)
    }

    private class Droplet(
        var position: Vec2,
        var water: Float,
        var deposit: Float
    )

}

