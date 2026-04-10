package com.zakgof.korender.impl.engine

import com.zakgof.korender.impl.gl.GL.glGetInteger
import com.zakgof.korender.impl.gl.GLConstants.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS
import com.zakgof.korender.impl.gl.GLConstants.GL_MAX_TEXTURE_IMAGE_UNITS
import com.zakgof.korender.impl.glgpu.GlBindableTexture
import kotlin.math.min

internal data class UnitSlot(
    var tex: GlBindableTexture? = null,
    var lastUsed: Int = -1
)

internal class NoTexUnitsAvailableException : RuntimeException()

internal class TextureBindingCache {

    val maxUnits = min(
        glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS),
        glGetInteger(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS),
    ).coerceIn(8, 128)

    val slots = Array(maxUnits) { UnitSlot() }
    var currentDrawIndex = 0
    var forbiddenUnitsMask: Int = 0

    fun nextDraw() {
        currentDrawIndex++
        forbiddenUnitsMask = 0
    }

    fun bind(tex: GlBindableTexture): Int {
        // 1. Already bound?
        val existingUnit = tex.unit
        if (existingUnit != -1) {
            slots[existingUnit].lastUsed = currentDrawIndex
            forbiddenUnitsMask = forbiddenUnitsMask or (1 shl existingUnit)
            // println("Texture [$tex] cache hit (unit=$existingUnit)")
            return existingUnit
        }

        // 2. Try find free slot
        for (unit in 1 until maxUnits) {
            if (slots[unit].tex == null) {
                assign(unit, tex)
                forbiddenUnitsMask = forbiddenUnitsMask or (1 shl unit)
                // println("Texture [$tex] -> free slot (unit=$unit)")
                return unit
            }
        }

        // 3. Evict LRU (but not forbidden)
        var lruUnit = -1
        var oldest = Int.MAX_VALUE
        for (unit in 1 until maxUnits) {
            if (forbiddenUnitsMask and (1 shl unit) != 0) continue
            val lastUsed = slots[unit].lastUsed
            if (lastUsed < oldest) {
                oldest = lastUsed
                lruUnit = unit
            }
        }

        if (lruUnit == -1) {
            throw NoTexUnitsAvailableException()
        }

        // 4. Evict + assign
        val oldTex = slots[lruUnit].tex!!
        oldTex.bind(-1)
        // println("Texture [$tex] -> evict slot (unit=$lruUnit) oldTex=[$oldTex], metric=$oldest")

        assign(lruUnit, tex)
        forbiddenUnitsMask = forbiddenUnitsMask or (1 shl lruUnit)
        return lruUnit
    }

    private fun assign(unit: Int, tex: GlBindableTexture) {
        tex.bind(unit)
        slots[unit].tex = tex
        slots[unit].lastUsed = currentDrawIndex
    }
}