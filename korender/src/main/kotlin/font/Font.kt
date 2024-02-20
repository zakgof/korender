package com.zakgof.korender.font

import com.zakgof.korender.Gpu
import com.zakgof.korender.Renderable
import com.zakgof.korender.geometry.Attributes.TEX
import com.zakgof.korender.geometry.Attributes.TEX1
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.gpu.GpuTexture
import com.zakgof.korender.material.Materials
import com.zakgof.korender.math.Color

class Font(private val gpu: Gpu, private val gpuTexture: GpuTexture, private val widths: FloatArray) {

    fun renderable(text: String, color: Color, height: Float, x: Float, y: Float): Renderable {

        return Renderable(
            Meshes.create(text.length * 4, text.length * 6, TEX, TEX1) {
                var xx = x
                for (c in text.chars()) {
                    var width = height * widths[c]
                    vertices((c % 16) / 16.0f, (c / 16 + 1f) / 16.0f, xx, y)
                    vertices((c % 16 + widths[c]) / 16.0f, (c / 16 + 1f) / 16.0f, xx + width, y)
                    vertices((c % 16 + widths[c]) / 16.0f, (c / 16 ) / 16.0f, xx + width, y + height)
                    vertices((c % 16) / 16.0f, (c / 16) / 16.0f, xx, y + height)
                    xx += width
                }
                for (i in text.indices) {
                    indices(i * 4 + 0, i * 4 + 1, i * 4 + 2, i * 4 + 0, i * 4 + 2, i * 4 + 3)
                }
            }.build(gpu),
            Materials.text(gpu, gpuTexture, color)
        )
    }

}
