package com.zakgof.korender.examples

import com.zakgof.korender.math.Vec3

class Roam(val levels: Int, val tileSize: Int, val height: (Float, Float) -> Float) {

    val tiles = mutableSetOf<Tile>()
    val mergeables = mutableSetOf<Tile>()

    private val minMaxY = mutableMapOf<Tile, Pair<Float, Float>>()

    init {
        precalculateMaxDelta()
        tiles += Tile(0, 0, levels)
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

    fun update(position: Vec3) {

        val cache = HashMap<Tile, Float>()
        fun pri(tile: Tile) = cache.getOrPut(tile) { tile.size() / (tile.distanceTo(position) + 0.001f) }

        val splitQ = PriorityQueue<Tile> { -pri(it) }
        val mergeQ = PriorityQueue<Tile> { pri(it) }

        tiles.forEach { if (it.level > 0) splitQ.add(it) }
        mergeables.forEach { mergeQ.add(it) }

        println("Iterating ${tiles.size}")

        while (true) {
            val maxSplit = splitQ.peek()?.let { -pri(it) } ?: Float.NEGATIVE_INFINITY
            val minMerge = mergeQ.peek()?.let { pri(it) } ?: Float.POSITIVE_INFINITY

            println("Metrics : ${-maxSplit} ${minMerge}")

            val crazy = -maxSplit > minMerge
            if (!crazy)
                break
            mergeQ.peek()?.let { merge(it, splitQ, mergeQ) }
        }

        while (tiles.size < 64) {
            splitQ.peek()?.let { split(it, splitQ, mergeQ, true) }
        }
        while (tiles.size > 65) {
            mergeQ.peek()?.let { merge(it, splitQ, mergeQ) }
        }
    }


    private fun merge(tile: Tile, splitQ: PriorityQueue<Tile>, mergeQ: PriorityQueue<Tile>) {

        println("Merging $tile")

        mergeQ.remove(tile)
        mergeables.remove(tile)
        tile.children().forEach {
            tiles.remove(it)
            splitQ.remove(it)
        }
        tiles.add(tile)
        if (tile.level > 0) splitQ.add(tile)
        tile.parent()?.let { parent ->
            if (tiles.containsAll(parent.children())) {
                mergeables.add(parent)
                mergeQ.add(parent)
            }
        }
        tile.neighbors()
            .mapNotNull { it.parent() }
            .forEach { parent ->
                if (tiles.containsAll(parent.children())) {
                    mergeables.add(parent)
                    mergeQ.add(parent)
                }
            }
    }

    private fun split(tile: Tile, splitQ: PriorityQueue<Tile>, mergeQ: PriorityQueue<Tile>, isMergeable: Boolean) {

        println("Splitting $tile")

        tiles.remove(tile)
        splitQ.remove(tile)
        if (isMergeable) {
            mergeables.add(tile)
            mergeQ.add(tile)
        }
        tile.parent()?.let {
            mergeables.remove(it)
            mergeQ.remove(it)
        }
        tile.children().forEach {
            tiles.add(it)
            if (it.level > 0) splitQ.add(it)
        }
        tile.neighbors()
            .mapNotNull { it.parent() }
            .filter { tiles.contains(it) }
            .forEach {
                println("... force splitting $it")
                split(it, splitQ, mergeQ, false)
            }
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
        val npx = position.x.coerceIn((x * tileSize).toFloat(), ((x + 1.shl(level)) * tileSize).toFloat())
        val npz = position.z.coerceIn((z * tileSize).toFloat(), ((z + 1.shl(level)) * tileSize).toFloat())
        return (position.x - npx) * (position.x - npx) + (position.z - npz) * (position.z - npz)
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
    ).filter { x >= 0 && z >= 0 && x < 1.shl(levels) && z < 1.shl(levels) }


}

