package com.zakgof.korender.baker

import com.zakgof.korender.Attributes.MODEL0
import com.zakgof.korender.Attributes.MODEL1
import com.zakgof.korender.Attributes.MODEL2
import com.zakgof.korender.Attributes.MODEL3
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.z
import kotlin.random.Random

class MetaballTree(kc: KorenderContext, metaball: Metaball, private val id: String, center: Vec3 = Vec3.ZERO) {

    private val leafMesh = kc.customMesh("leaf", 4, 6, POS, NORMAL, TEX, MODEL0, MODEL1, MODEL2, MODEL3) {
        pos(Vec3(-0.5f, -0.5f, 0f)).normal(1.z).tex(0f, 0f)
        pos(Vec3(0.5f, -0.5f, 0f)).normal(1.z).tex(1f, 0f)
        pos(Vec3(0.5f, 0.5f, 0f)).normal(1.z).tex(1f, 1f)
        pos(Vec3(-0.5f, 0.5f, 0f)).normal(1.z).tex(0f, 1f)

        index(0, 1, 2, 0, 2, 3)
    }
    private val leafInstances = metaball.points.mapIndexed { index, pt ->
        val quaternion = (0..64)
            .map { index * 1023 + it }
            .map { Quaternion.fromAxisAngle(Vec3.random(it), Random(index * 777 + it).nextFloat() * 100f) }
            .maxBy { (it * 1.z) * pt.n }

        rotate(quaternion)
            .scale(0.8f)
            .translate((pt.pos - center))
    }

    fun render(fc: FrameContext, vararg mods: MaterialModifier) = with(fc) {
        Renderable(
            base(colorTexture = texture("model/leaf.png")),
            *mods,
            mesh = leafMesh,
            transparent = true,
            instancing = instancing(
                id = id,
                count = leafInstances.size,
                dynamic = false
            ) {
                leafInstances.forEach { Instance(it) }
            })
    }

}