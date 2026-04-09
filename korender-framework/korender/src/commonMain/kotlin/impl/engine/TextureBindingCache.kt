package com.zakgof.korender.impl.engine

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.glgpu.GlBindableTexture

internal data class UnitSlot(
    var tex: GlBindableTexture? = null,
    var lastUsed: Int = -1
)

internal class TextureBindingCache {

    // TODO
    val MAX_UNITS = 16 - 1
    val slots = Array(MAX_UNITS) { UnitSlot() }
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
        for (unit in 1 until MAX_UNITS) {
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
        for (unit in 1 until MAX_UNITS) {
            if (forbiddenUnitsMask and (1 shl unit) != 0) continue
            val lastUsed = slots[unit].lastUsed
            if (lastUsed < oldest) {
                oldest = lastUsed
                lruUnit = unit
            }
        }

        if (lruUnit == -1) {
            throw KorenderException("No available texture unit for binding")
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