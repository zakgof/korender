package ltree

import com.zakgof.korender.math.Vec3
import ltree.generator.LTree
import java.io.File
import java.io.FileOutputStream

fun saveBranches(branches: List<LTree.Branch>) {
    val root = File(System.getProperty("projectRoot"))
    val islandRoot = File(root, "../examples/src/commonMain/composeResources/files/island")
    val file = File(islandRoot, "ltree/branches.bin")
    val fos = FileOutputStream(file)
    islandRoot.mkdirs()
    fos.use {
        it.push(branches.size)
        branches.forEach {  branch ->
            it.push(branch.head)
            it.push(branch.tail)
            it.push(branch.raidusAtHead)
            it.push(branch.raidusAtTail)
        }
    }
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
