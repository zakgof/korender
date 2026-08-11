package editor.cache

import androidx.compose.ui.graphics.ImageBitmap
import com.zakgof.korender.Image
import com.zakgof.korender.scope.KorenderScope
import editor.model.Tex
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.decodeToImageBitmap

object TextureImageCache {

    val korender = mutableMapOf<String, Image>()
    val compose = mutableMapOf<String, ImageBitmap>()

    context(context: KorenderScope)
    fun korender(tex: Tex): Image =
        korender.computeIfAbsent(tex.name) {
            runBlocking { context.loadImage(tex.bytes, tex.ext).await() }
        }

    fun compose(tex: Tex): ImageBitmap =
        compose.computeIfAbsent(tex.name) {
            tex.bytes.decodeToImageBitmap()
        }

    fun dispose(name: String) {
        korender.remove(name)
        compose.remove(name)
    }
}