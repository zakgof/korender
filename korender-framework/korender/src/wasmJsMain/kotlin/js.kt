package com.zakgof.korender

import org.khronos.webgl.Int8Array
import org.khronos.webgl.WebGLObject
import org.khronos.webgl.WebGLRenderingContextBase
import org.w3c.dom.RenderingContext
import kotlin.js.Promise


abstract external class WebGL2RenderingContext : WebGLRenderingContextBase, RenderingContext, JsAny {
    abstract fun createVertexArray(): WebGLVertexArray?
    abstract fun deleteVertexArray(vertexArray: WebGLVertexArray)
    abstract fun bindVertexArray(vertexArray: WebGLVertexArray?)
    abstract fun vertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, pointer: Int)
}

abstract external class WebGLVertexArray : WebGLObject, JsAny

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