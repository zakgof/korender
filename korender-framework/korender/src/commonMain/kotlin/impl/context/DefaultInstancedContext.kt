package com.zakgof.korender.impl.context

import com.zakgof.korender.context.BillboardInstancingScope
import com.zakgof.korender.context.GltfInstancingScope
import com.zakgof.korender.context.InstancingScope
import com.zakgof.korender.impl.engine.BillboardInstance
import com.zakgof.korender.impl.engine.GltfInstance
import com.zakgof.korender.impl.engine.MeshInstance
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal class DefaultBillboardInstancingScope internal constructor(private val instances: MutableList<BillboardInstance>) : BillboardInstancingScope {
    override fun Instance(pos: Vec3?, scale: Vec2?, rotation: Float?, color: ColorRGBA?, colorTextureIndex: Int?) {
        instances.add(BillboardInstance(pos, scale, rotation, color, colorTextureIndex))
    }
}

internal class DefaultInstancingScope internal constructor(private val instances: MutableList<MeshInstance>) : InstancingScope {
    override fun Instance(transform: Transform?, color: ColorRGBA?, metallic: Float?, roughness: Float?, colorTextureIndex: Int?) {
        instances.add(MeshInstance(transform, null, color, metallic, roughness, colorTextureIndex))
    }
}

internal class DefaultGltfInstancingScope internal constructor(private val instances: MutableList<GltfInstance>) : GltfInstancingScope {
    override fun Instance(transform: Transform, time: Float?, animation: Int?) {
        instances.add(GltfInstance(transform, time, animation))
    }
}