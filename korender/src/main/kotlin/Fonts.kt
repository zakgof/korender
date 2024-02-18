package com.zakgof.korender

import com.zakgof.korender.material.Textures
import java.awt.Color
import java.awt.Font.TRUETYPE_FONT
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.round


object Fonts {

    fun load(gpu: Gpu, fontFile: String): Font {

        val cell = 64
        val stream: InputStream = Fonts.javaClass.getResourceAsStream(fontFile)!!
        val originalFont = java.awt.Font.createFont(TRUETYPE_FONT, stream)
        val img = BufferedImage(cell * 16, cell * 16, BufferedImage.TYPE_3BYTE_BGR)
        val graphics = img.graphics
        graphics.font = originalFont
        val fmOriginal = graphics.fontMetrics

        val width = (0 until 128).maxOf { fmOriginal.getStringBounds("" + it.toChar(), graphics).width }
        val height = (0 until 128).maxOf { fmOriginal.getStringBounds("" + it.toChar(), graphics).height }

        val fontSize = cell / max(width, height)

        val font = originalFont.deriveFont(round(fontSize.toFloat()))
        graphics.font = font
        graphics.color = Color.WHITE
        val widths = FloatArray(128)
        val fm = graphics.fontMetrics
        for (c in 0 until 128) {
            widths[c] = fm.charWidth(c).toFloat() / cell
            if (font.canDisplay(c)) {
                graphics.drawString("" + c.toChar(), (c % 16) * cell, (c / 16) * cell + cell - fm.maxDescent-1)
            }
        }
        graphics.dispose()
        val gpuTexture = Textures.create(img).build(gpu)
        return Font(gpu, gpuTexture, widths)
    }
}