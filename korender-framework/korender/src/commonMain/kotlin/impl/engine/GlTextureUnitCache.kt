package com.zakgof.korender.impl.engine

import com.zakgof.korender.impl.glgpu.GLBindableTexture

internal class GlTextureUnitCache {

    private val cache: Array<GLBindableTexture?>
    private val map = mutableMapOf<GLBindableTexture, Int>()
    private var nextEvict = 0

    init {
        val size: Int = 16 - 1 // TODO: glGetIntegerv(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, &maxCombinedTextureUnits);
        cache = Array(size) { null }
    }

    fun bind(texture: GLBindableTexture): Int {
        var unit = map[texture]
        if (unit == null) {
            unit = cache.indices.firstOrNull { cache[it] == null }
            if (unit == null) {
                unit = nextEvict
                nextEvict = (nextEvict + 1) % cache.size
                map.remove(cache[unit]!!)
            }
            map[texture] = unit
            cache[unit] = texture
            texture.bind(unit + 1)
        }
        return unit + 1
    }

    fun free(texture: GLBindableTexture) {
        map.remove(texture)?.let { cache[it] = null }
    }

}