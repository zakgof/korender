package com.zakgof.korender

import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.WebGLBuffer
import org.khronos.webgl.WebGLObject
import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLRenderingContextBase
import org.khronos.webgl.WebGLTexture
import org.w3c.dom.RenderingContext
import kotlin.js.Promise


abstract external class WebGL2RenderingContext : WebGLRenderingContextBase, RenderingContext, JsAny {
    abstract fun createVertexArray(): WebGLVertexArray?
    abstract fun deleteVertexArray(vertexArray: WebGLVertexArray)
    abstract fun bindVertexArray(vertexArray: WebGLVertexArray?)
    abstract fun vertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, pointer: Int)
    abstract fun drawBuffers(targets: JsArray<JsNumber>)
    abstract fun framebufferTexture(target: Int, attachment: Int, texture: WebGLTexture?, level: Int)
    abstract fun getTexImage(tex: Int, level: Int, format: Int, type: Int, array: Uint8Array)
    abstract fun drawArraysInstanced(mode: Int, starting: Int, count: Int, instances: Int)
    abstract fun drawElementsInstanced(mode: Int, count: Int, type: Int, indices: Int, instances: Int)
    abstract fun vertexAttribDivisor(index: Int, divisor: Int)
    abstract fun getUniformBlockIndex(program: WebGLProgram, uniformBlockName: String): Int
    abstract fun getActiveUniformBlockParameter(program: WebGLProgram, uniformBlockIndex: Int, pname: Int): JsAny
    abstract fun uniformBlockBinding(program: WebGLProgram, uniformBlockIndex: Int, blockBinding: Int)
    abstract fun bindBufferBase(target: Int, blockBinding: Int, buffer: WebGLBuffer)
    abstract fun getActiveUniforms(program: WebGLProgram, uniformIndices: JsArray<JsNumber>, param: Int): JsArray<*>
    abstract fun texSubImage2D(target: Int, level: Int, x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, array: ArrayBufferView)
    abstract fun bindBufferRange(target: Int, blockBinding: Int, buffer: WebGLBuffer, shift: Int, size: Int)
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

internal fun performanceNow(): Double = js("performance.now()")

internal fun typeOf(obj: JsAny): JsAny = js("(typeof obj).toString()")

external class FontFace : JsAny {
    fun load(): Promise<FontFace>
}

internal fun maxTexAniso(ext: JsAny): Int = js("ext.MAX_TEXTURE_MAX_ANISOTROPY_EXT")

internal fun texAniso(ext: JsAny): Int = js("ext.TEXTURE_MAX_ANISOTROPY_EXT")