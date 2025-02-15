package com.zakgof.korender.examples

import com.zakgof.korender.math.Vec3
import kotlin.math.hypot

class Roam(private val levels: Int, private val tileSize: Int, val height: (Float, Float) -> Float) {

    private val minMaxY = mutableMapOf<Tile, Pair<Float, Float>>()

    init {
        precalculateMaxDelta()
    }

    private fun precalculateMaxDelta() {
        for (l in 0..levels) {
            l.tiles().forEach {
                minMaxY[it] = if (l == 0) minMaxTile(it.x, it.z) else minMaxChildren(it)
            }
        }
    }

    private fun minMaxTile(x: Int, z: Int): Pair<Float, Float> {
        var min = Float.MAX_VALUE
        var max = Float.MIN_VALUE
        for (xx in 0 until tileSize) {
            for (zz in 0 until tileSize) {
                val h = height((x * tileSize + xx).toFloat(), (z * tileSize + zz).toFloat())
                if (h < min) min = h
                if (h > max) max = h
            }
        }
        return min to max
    }

    private fun minMaxChildren(tile: Tile): Pair<Float, Float> {
        val ch = tile.children()
        return ch.minOf { minMaxY[it]!!.first } to ch.maxOf { minMaxY[it]!!.second }
    }

    fun update(position: Vec3, error: Float): Set<Tile> {
        val tiles = mutableSetOf<Tile>()
        val togo = mutableSetOf<Tile>()
        val force = mutableSetOf<Tile>()

        fun pri(tile: Tile) = tile.size() / (tile.distanceTo(position) + 0.001f)

        togo += Tile(0, 0, levels)

        while (togo.isNotEmpty()) {
            val t = togo.first()
            togo.remove(t)
            if (t.level == 0 || pri(t) < error && !force.contains(t)) {
                tiles += t
            } else {
                togo += t.children()
                t.neighbors()
                    .mapNotNull { it.parent() }
                    .filter { it != t.parent() }
                    .forEach {
                        if (tiles.remove(it)) {
                            togo += it
                        }
                        force += it
                    }
            }
        }
        return tiles
    }

    data class Tile(val x: Int, val z: Int, val level: Int) {
        fun children() = listOf(
            Tile(x, z, level - 1),
            Tile(x + 1.shl(level - 1), z, level - 1),
            Tile(x, z + 1.shl(level - 1), level - 1),
            Tile(x + 1.shl(level - 1), z + 1.shl(level - 1), level - 1)
        )

        fun size() = 1.shl(level)

        override fun toString(): String = "$level: $x,$z"
    }

    private fun Int.tiles(): List<Tile> = (0 until 1.shl(levels - this)).flatMap { x ->
        (0 until 1.shl(levels - this)).map { z ->
            Tile(x * 1.shl(this), z * 1.shl(this), this)
        }
    }

    private fun Tile.distanceTo(position: Vec3): Float {
        val npx = position.x.coerceIn((x * tileSize).toFloat(), ((x + size()) * tileSize).toFloat())
        val npz = position.z.coerceIn((z * tileSize).toFloat(), ((z + size()) * tileSize).toFloat())
        return hypot(position.x - npx, position.z - npz)
    }

    private fun Tile.parent(): Tile? = if (level == levels)
        null
    else
        Tile(x.shr(level + 1).shl(level + 1), z.shr(level + 1).shl(level + 1), level + 1)

    private fun Tile.neighbors() = listOf(
        Tile(x - size(), z, level),
        Tile(x + size(), z, level),
        Tile(x, z - size(), level),
        Tile(x, z + size(), level)
    ).filter { it.x >= 0 && it.z >= 0 && it.x < 1.shl(levels) && it.z < 1.shl(levels) }

}

