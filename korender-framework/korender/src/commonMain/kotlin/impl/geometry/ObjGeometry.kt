package com.zakgof.korender.impl.geometry

// TODO
/*
class ObjGeometry {

    private fun obj(objFile: String): MeshBuilder {
        val obj = ObjReader.read(resourceBytes(objFile))
        return create(objFile, obj)
    }

    private fun create(name: String, obj: Obj): MeshBuilder {

        val mapping = mutableMapOf<String, Int>()
        val vertices = mutableListOf<FloatArray>()
        val indices = mutableListOf<Int>()

        // Rewiring
        for (i in 0 until obj.numFaces) {
            val face = obj.getFace(i)
            val newFaceindices = mutableListOf<Int>()
            for (ii in 0 until face.numVertices) {
                val vertexIndex = face.getVertexIndex(ii)
                val normalIndex = face.getNormalIndex(ii)
                val texIndex = face.getTexCoordIndex(ii)
                val key = "$vertexIndex/$normalIndex/$texIndex"
                val newVertIndex = mapping[key]
                if (newVertIndex == null) {
                    val vertex = obj.getVertex(vertexIndex)
                    val normal = obj.getNormal(normalIndex)
                    val tex = obj.getTexCoord(texIndex)
                    vertices.add(
                        floatArrayOf(
                            vertex.x,
                            vertex.y,
                            vertex.z,
                            normal.x,
                            normal.y,
                            normal.z,
                            tex.x,
                            1f - tex.y
                        )
                    )
                    mapping[key] = vertices.size - 1
                    newFaceindices.add(vertices.size - 1)
                } else {
                    newFaceindices.add(newVertIndex)
                }
            }

            val v1 = vertices[newFaceindices[1]]
            val v2 = vertices[newFaceindices[2]]
            if (abs(v1[0] - v2[0]) > 1.0f)
                println("Index buffer position: ${indices.size - 1} Delta x: ${v1[0] - v2[0]} Delta y: ${v1[1] - v2[1]} Delta z: ${v1[2] - v2[2]}")

            indices.add(newFaceindices[0])
            indices.add(newFaceindices[1])
            indices.add(newFaceindices[2])
            if (newFaceindices.size == 4) {
                indices.add(newFaceindices[0])
                indices.add(newFaceindices[2])
                indices.add(newFaceindices[3])
            }
            if (newFaceindices.size != 3 && newFaceindices.size != 4) {
                throw KorenderException("Only triangles and quads supported in .obj files")
            }
        }
        return Geometry.create(name, vertices.size, indices.size, POS, NORMAL, TEX) {
            vertices.forEach() { vertices(*it) }
            indices(*indices.toIntArray())
        }
    }
}
*/