package com.zakgof.korender.obj

import com.zakgof.korender.Mesh

interface ObjInfo {

    val parts: List<Part>

    interface Part {
        val name: String?
        val mesh: Mesh
    }
}