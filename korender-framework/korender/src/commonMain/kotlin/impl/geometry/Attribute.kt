package com.zakgof.korender.impl.geometry

import com.zakgof.korender.buffer.Floater
import com.zakgof.korender.mesh.Vertex

class Attribute internal constructor(
    internal val name: String,
    internal val size: Int,
    internal val writer: (Floater, Vertex) -> Unit,
    internal val reader: (Floater, Vertex) -> Unit
)
