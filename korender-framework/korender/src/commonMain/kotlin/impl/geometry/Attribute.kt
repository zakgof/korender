package com.zakgof.korender.impl.geometry

import com.zakgof.korender.mesh.Vertex
import java.nio.FloatBuffer

class Attribute internal constructor(
    internal val name: String,
    internal val size: Int,
    internal val writer: (FloatBuffer, Vertex) -> Unit,
    internal val reader: (FloatBuffer, Vertex) -> Unit
)
