package com.zakgof.korender.impl.context

import com.zakgof.korender.impl.engine.BillboardInstance
import com.zakgof.korender.impl.engine.MeshInstance
import com.zakgof.korender.impl.engine.ModelInstance
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.scope.BillboardInstancingScope
import com.zakgof.korender.scope.InstancingScope
import com.zakgof.korender.scope.ModelInstancingScope

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

internal class DefaultModelInstancingScope internal constructor(private val instances: MutableList<ModelInstance>) : ModelInstancingScope {
    override fun Instance(transform: Transform, time: Float?, animation: Int?) {
        instances.add(ModelInstance(transform, time, animation))
    }
}