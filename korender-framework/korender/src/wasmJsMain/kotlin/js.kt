import org.khronos.webgl.Int8Array
import kotlin.js.Promise

internal fun jsLoadFont(fontArray: Int8Array): FontFace = js(
    """
        {
            const fontBlob = new Blob([fontArray], { type: 'font/ttf' })
            const fontUrl = URL.createObjectURL(fontBlob)
            return new FontFace('KorenderFont', 'url(' + fontUrl + ')')
        }
    """
)

internal fun jsAddFont(fontFace: FontFace): JsAny =
    js(
        """
      {
          console.log("d.f", document.fonts)
          document.fonts.add(fontFace)
          console.log("added", document.fonts)
          return 0
      }
    """
    )

internal fun performanceNow(): Double =
    js("performance.now()")

external class FontFace : JsAny {
    fun load(): Promise<FontFace>
}