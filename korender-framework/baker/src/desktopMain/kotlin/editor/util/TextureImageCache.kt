package editor.util

import androidx.compose.ui.graphics.ImageBitmap
import com.zakgof.korender.Image
import com.zakgof.korender.context.KorenderContext
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.io.File

object TextureImageCache {

    val korender = mutableMapOf<String, Image>()
    val compose = mutableMapOf<String, ImageBitmap>()

    context(context: KorenderContext)
    fun korender(path: String): Image =
        korender.computeIfAbsent(path) {
            val file = File(path)
            val bytes = file.readBytes()
            runBlocking { context.loadImage(bytes, file.extension).await() }

        }

    fun compose(path: String): ImageBitmap =
        compose.computeIfAbsent(path) {
            val file = File(path)
            val bytes = file.readBytes()
            bytes.decodeToImageBitmap()
        }

    fun dispose(id: String) {
        korender.remove(id)
        compose.remove(id)
    }
}