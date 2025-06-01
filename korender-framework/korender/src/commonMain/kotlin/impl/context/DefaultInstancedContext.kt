package com.zakgof.korender.impl.context

import com.zakgof.korender.context.InstancedBillboardsContext
import com.zakgof.korender.context.InstancedGltfContext
import com.zakgof.korender.context.InstancedRenderablesContext
import com.zakgof.korender.impl.engine.BillboardInstance
import com.zakgof.korender.impl.engine.GltfInstance
import com.zakgof.korender.impl.engine.MeshInstance
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal class DefaultInstancedBillboardsContext internal constructor(private val instances: MutableList<BillboardInstance>) : InstancedBillboardsContext {
    override fun Instance(pos: Vec3, scale: Vec2, rotation: Float) {
        instances.add(BillboardInstance(pos, scale, rotation))
    }
}

internal class DefaultInstancedRenderablesContext internal constructor(private val instances: MutableList<MeshInstance>) : InstancedRenderablesContext {
    override fun Instance(transform: Transform) {
        instances.add(MeshInstance(transform, null))
    }
}

internal class DefaultInstancedGltfContext internal constructor(private val instances: MutableList<GltfInstance>) : InstancedGltfContext {
    override fun Instance(transform: Transform, time: Float?, animation: Int?) {
        instances.add(GltfInstance(transform, time, animation))
    }
}