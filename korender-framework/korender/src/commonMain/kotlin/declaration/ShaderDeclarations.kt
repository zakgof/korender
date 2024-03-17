package com.zakgof.korender.declaration


data class ShaderDeclaration(
    val vertFile: String,
    val fragFile: String,
    val defs: Set<String>
)

