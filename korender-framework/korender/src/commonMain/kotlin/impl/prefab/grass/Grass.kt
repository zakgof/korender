package com.zakgof.korender.impl.prefab.grass

import com.zakgof.korender.Attributes.PHI
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.impl.prefab.InternalPrefab
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3

internal class Grass(
    korenderContext: KorenderContext,
    private val id: String,
    private val segments: Int = 4,
    private val cell: Float = 0.4f,
    private val side: Int = 200,
    private val filter: (Vec3) -> Boolean = { true }
) : InternalPrefab {

    private val mesh = korenderContext.customMesh(id, segments * 4 + 2, segments * 12 - 6, POS, TEX, PHI) {
        for (b1 in 0..1) {
            for (s in 0 until segments) {
                val h = s.toFloat() / segments
                pos(Vec3.ZERO).tex(0f, h).phi(b1.toFloat())
                pos(Vec3.ZERO).tex(1f, h).phi(b1.toFloat())
            }
            pos(Vec3.ZERO).tex(0.5f, 1.0f).phi(b1.toFloat())
        }
        for (s in 0 until segments - 1) {
            val b = s * 2
            index(b + 0, b + 1, b + 2, b + 1, b + 3, b + 2)
        }
        val bt = (segments - 1) * 2
        index(bt + 0, bt + 1, bt + 2)

        val bb = (segments * 2 + 1)
        for (s in 0 until segments - 1) {
            val b = bb + s * 2
            index(b + 1, b + 0, b + 2, b + 3, b + 1, b + 2)
        }
        val bt2 = bb + (segments - 1) * 2
        index(bt2 + 1, bt2 + 0, bt2 + 2)
    }

    override fun render(fc: FrameContext, vararg materialModifiers: MaterialModifier) = with(fc) {

        val depth = cell * side * 0.5f

        Renderable(
            vertex("!shader/effect/grass.vert"),
            defs("VERTEX_COLOR", "VERTEX_OCCLUSION"),
            standart {
                pbr.metallic = 0.0f
                pbr.roughness = 0.9f
                set("grassCutoffDepth", depth)
                set("grassCell", cell)
            },
            *materialModifiers,
            mesh = mesh,
            instancing = positionInstancing("$id-instancing", side * side, true) {
                val xsnap = (fc.camera.position.x / cell).toInt() * cell
                val zsnap = (fc.camera.position.z / cell).toInt() * cell
                for (xx in 0 until side) {
                    for (zz in 0 until side) {
                        val pos = Vec3(
                            xsnap + (xx - side / 2) * cell,
                            0f,
                            zsnap + (zz - side / 2) * cell
                        )
                        if (filter(pos) && (pos - fc.camera.position) * fc.camera.direction > 0f) {
                            Instance(translate(pos))
                        }
                    }
                }
            }
        )
    }
}