package com.zakgof.korender.impl.font

import com.zakgof.korender.impl.gpu.GpuTexture

class Font(val gpuTexture: GpuTexture, val widths: FloatArray) :
    AutoCloseable {

    override fun close() = gpuTexture.close()

    fun textWidth(height: Int, text: String): Int =
        text.toCharArray()
            .map { widths[it.code] * height }
            .sum()
            .toInt()

//    fun renderable(text: String, color: Color, height: Float, x: Float, y: Float): Renderable {
//
//        return Renderable(
//            Meshes.create(text.length * 4, text.length * 6, TEX, SCREEN) {
//                var xx = x
//                for (c in text.chars()) {
//                    val ratio = widths[c]
//                    val width = height * ratio
//                    vertices((c % 16) / 16.0f, (c / 16 + 1f) / 16.0f, xx, y)
//                    vertices((c % 16 + ratio) / 16.0f, (c / 16 + 1f) / 16.0f, xx + width, y)
//                    vertices((c % 16 + ratio) / 16.0f, (c / 16) / 16.0f, xx + width, y + height)
//                    vertices((c % 16) / 16.0f, (c / 16) / 16.0f, xx, y + height)
//                    xx += width
//                }
//                for (i in text.indices) {
//                    indices(i * 4 + 0, i * 4 + 1, i * 4 + 2, i * 4 + 0, i * 4 + 2, i * 4 + 3)
//                }
//            }.build(gpu),
//            Shaders.f
//            Materials.text(gpu, gpuTexture, color)
//        )
//    }

//    fun dynamic(gpu: Gpu, reservedLength: Int, color: Color): TextRenderable {
//        val mesh = Meshes.create(reservedLength * 4, reservedLength * 6, TEX, SCREEN) {
//            for (i in 0..<reservedLength) {
//                indices(i * 4 + 0, i * 4 + 1, i * 4 + 2, i * 4 + 0, i * 4 + 2, i * 4 + 3)
//            }
//        }.build(gpu, true)
//        return TextRenderable(gpu, this, mesh, color)
//    }
}

//class TextRenderable(
//    gpu: Gpu,
//    private val font: Font,
//    override val mesh: Meshes.DefaultMesh,
//    color: Color
//) :
//    Renderable {
//
//    override val material = Materials.text(gpu, font.gpuTexture, color)
//    override val transform = Transform()
//    fun update(text: String, height: Float, x: Float, y: Float) {
//        var xx = x
//        for (i in text.indices) {
//            val c = text[i].code
//            val ratio = font.widths[c]
//            val width = height * ratio
//            mesh.updateVertex(i * 4 + 0) {
//                it.tex = Vec2((c % 16) / 16.0f, (c / 16 + 1f) / 16.0f)
//                it.screen = Vec2(xx, y)
//            }
//            mesh.updateVertex(i * 4 + 1) {
//                it.tex = Vec2((c % 16 + ratio) / 16.0f, (c / 16 + 1f) / 16.0f)
//                it.screen = Vec2(xx + width, y)
//            }
//            mesh.updateVertex(i * 4 + 2) {
//                it.tex = Vec2((c % 16 + ratio) / 16.0f, (c / 16) / 16.0f)
//                it.screen = Vec2(xx + width, y + height)
//            }
//            mesh.updateVertex(i * 4 + 3) {
//                it.tex = Vec2((c % 16) / 16.0f, (c / 16) / 16.0f)
//                it.screen = Vec2(xx, y + height)
//            }
//            xx += width
//        }
//        mesh.vertices = text.length * 4
//        mesh.indices = text.length * 6
//        mesh.updateGpu()
//    }
//
//    override val worldBoundingBox: BoundingBox? = null
//
//}
