package com.zakgof

import java.io.File
import javax.imageio.ImageIO

fun main() {
    val input = File("E:\\Business\\render2024\\heightmap.png")
    val output = File("E:\\Business\\render2024\\korender\\korender-demo\\composeApp\\src\\commonMain\\composeResources\\hf-rg16.png")


    val bi16 = ImageIO.read(input).raster

    var mx = 0
    val ia = IntArray(3)
    val img = Images.createi(bi16.width, bi16.height) { x, y ->
        bi16.getPixel(x, y, ia)
        val gray = ia[0]        // 0..65535
        if (gray > 58000) {
            println("$x $y")
        }
        ia[0] = gray shr 8      // R: high byte
        ia[1] = gray and 0xFF   // G: low bute
        ia[2] = 0               // B
        ia
    }
    ImageIO.write(img, "png", output)
}