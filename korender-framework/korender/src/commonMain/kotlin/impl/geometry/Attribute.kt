package com.zakgof.korender.impl.geometry

import java.nio.FloatBuffer

class Attribute(
    val name: String,
    val size: Int,
    val writer: (FloatBuffer, Vertex) -> Unit,
    val reader: (FloatBuffer, Vertex) -> Unit
)