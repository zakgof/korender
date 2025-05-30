package com.zakgof.korender.impl.geometry

import com.zakgof.korender.KorenderException
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.resourceBytes
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal class ObjModel(val vertices: List<Vertex>, val indices: List<Int>) {
    class Vertex(val pos: Vec3, val normal: Vec3, val tex: Vec2)
}

internal object ObjLoader {
    suspend fun load(file: String, appResourceLoader: ResourceLoader): ObjModel {
        val positions = mutableListOf<Vec3>()
        val normals = mutableListOf<Vec3>()
        val texes = mutableListOf<Vec2>()
        val faces = mutableListOf<IntArray>()
        resourceBytes(appResourceLoader, file)
            .decodeToString()
            .lines()
            .forEach {
                val command = it.split(" ", limit = 2).toList()
                if (command.size == 2) {
                    val (header, body) = command
                    when (header) {
                        "v" -> positions += parse3(body)
                        "vn" -> normals += parse3(body)
                        "vt" -> texes += parse2(body)
                        "f" -> faces += parseFace(body)
                    }
                }
            }
        val vertices = mutableListOf<ObjModel.Vertex>()
        val vertmap = mutableMapOf<List<Int>, Int>()
        val indices = faces.map {
            vertmap.getOrPut(it.toList()) {
                vertices += ObjModel.Vertex(
                    positions[it[0] - 1],
                    normals[(if (it.size == 1) it[0] else it[2]) - 1],
                    texes[(if (it.size == 1) it[0] else it[1]) - 1],
                )
                vertices.size - 1
            }
        }
        return ObjModel(vertices, indices)
    }

    private fun parse3(line: String): Vec3 {
        val tokens = line.split(" ").filter { it.isNotBlank() }
        if (tokens.size != 3)
            throw KorenderException("Obj v expects 3 coordinates")
        return Vec3(tokens[0].toFloat(), tokens[1].toFloat(), tokens[2].toFloat())
    }

    private fun parse2(line: String): Vec2 {
        val tokens = line.split(" ").filter { it.isNotBlank() }
        if (tokens.size != 2 && tokens.size != 3)
            throw KorenderException("Obj vt expects 2 (or 3) coordinates")
        return Vec2(tokens[0].toFloat(), 1f - tokens[1].toFloat())
    }

    private fun parseFace(line: String): List<IntArray> {
        val tokens = line.split(" ").filter { it.isNotBlank() }
        if (tokens.size != 3)
            throw KorenderException("Obj f expects 3 vertices, quads are not supported")
        return tokens.map { t ->
            t.split("/")
                .map { it.toInt() }
                .toIntArray()
        }
    }
}
