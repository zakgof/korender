package com.zakgof.korender.scope

import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

interface InstancingDeclaration

interface BillboardInstancingDeclaration

interface GltfInstancingDeclaration

interface InstancingScope {
    fun Instance(
        transform: Transform? = null,
        color: ColorRGBA? = null,
        metallic: Float? = null,
        roughness: Float? = null,
        colorTextureIndex: Int? = null,
    )
}

interface BillboardInstancingScope {
    fun Instance(
        pos: Vec3? = null,
        scale: Vec2? = null,
        rotation: Float? = null,
        color: ColorRGBA? = null,
        colorTextureIndex: Int? = null,
    )
}

interface GltfInstancingScope {
    fun Instance(
        transform: Transform,
        time: Float? = null,
        animation: Int? = null,
    )
}

interface InstancingParameter

interface BillboardInstancingParameter