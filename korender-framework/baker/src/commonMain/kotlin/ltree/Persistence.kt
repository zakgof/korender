package ltree

import com.zakgof.korender.math.Vec3
import ltree.generator.LTree
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO

val root = File(System.getProperty("projectRoot"))
val islandRoot = File(root, "../examples/src/commonMain/composeResources/files/island")

fun saveBranches(branches: List<LTree.Branch>) {
    val file = File(islandRoot, "tree/branches.bin")
    val fos = FileOutputStream(file)
    islandRoot.mkdirs()
    fos.use {
        it.push(branches.size)
        branches.forEach { branch ->
            it.push(branch.head)
            it.push(branch.tail)
            it.push(branch.raidusAtHead)
            it.push(branch.raidusAtTail)
        }
    }
}

fun saveCards(cards: List<Card>) {
    saveCardsMetadata(cards)
    saveCardsAtlas(cards)
}

private fun saveCardsMetadata(cards: List<Card>) {
    val file = File(islandRoot, "tree/cards.bin")
    val fos = FileOutputStream(file)
    islandRoot.mkdirs()
    fos.use {
        it.push(cards.size)
        cards.forEach { card ->
            it.push(card.center)
            it.push(card.normal)
            it.push(card.up)
            it.push(card.size)
        }
    }
}

private fun saveCardsAtlas(cards: List<Card>) {
    val xImages = 4
    val yImages = 4
    val side = cards[0].image.width
    val bi = BufferedImage(side * xImages, side * yImages, BufferedImage.TYPE_INT_ARGB)
    val raster = bi.raster
    val pixel = IntArray(4)
    for (xx in 0 until side * xImages) {
        for (yy in 0 until side * yImages) {
            val card = cards[(xx / side) + (yy / side) * xImages]
            val inCardX = xx % side
            val inCardY = yy % side
            val color = card.image.pixel(inCardX, inCardY)
            pixel[0] = (color.r * 255.0f).toInt()
            pixel[1] = (color.g * 255.0f).toInt()
            pixel[2] = (color.b * 255.0f).toInt()
            pixel[3] = (color.a * 255.0f).toInt()
            raster.setPixel(xx, yy, pixel)
        }
    }
    ImageIO.write(bi, "png", File(islandRoot, "tree/atlas.png"))
}

private fun FileOutputStream.push(b: Vec3) {
    push(b.x)
    push(b.y)
    push(b.z)
}

private fun FileOutputStream.push(b: Float) {
    push(b.toBits())
}

private fun FileOutputStream.push(b: Int) {
    write(b and 0xFF)
    write((b shr 8) and 0xFF)
    write((b shr 16) and 0xFF)
    write((b shr 24) and 0xFF)
}