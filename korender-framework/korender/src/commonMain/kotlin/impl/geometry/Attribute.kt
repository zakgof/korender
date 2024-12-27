package com.zakgof.korender.impl.geometry

class Attribute internal constructor(
    internal val name: String,
    internal val structSize: Int,
    internal val primitiveSize: Int,
    internal val glPrimitive: Int,
    internal val order: Int
)
