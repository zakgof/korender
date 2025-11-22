package com.zakgof.korender

import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.WebGLBuffer
import org.khronos.webgl.WebGLObject
import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLRenderingContextBase
import org.khronos.webgl.WebGLTexture
import org.w3c.dom.RenderingContext
import kotlin.js.Promise


@OptIn(ExperimentalWasmJsInterop::class)
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
    abstract fun texImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, border: Int, format: Int, type: Int, pixels: ArrayBufferView?)
    abstract fun texSubImage3D(target: Int, level: Int, x: Int, y: Int, z: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, array: ArrayBufferView)
}

@OptIn(ExperimentalWasmJsInterop::class)
abstract external class WebGLVertexArray : WebGLObject, JsAny

@OptIn(ExperimentalWasmJsInterop::class)
internal fun jsLoadFont(fontArray: js.typedarrays.Int8Array<js.buffer.ArrayBuffer>): FontFace = js(
    """
        {
            const fontBlob = new Blob([fontArray], { type: 'font/ttf' })
            const fontUrl = URL.createObjectURL(fontBlob)
            return new FontFace('KorenderFont', 'url(' + fontUrl + ')')
        }
    """
)

@OptIn(ExperimentalWasmJsInterop::class)
internal fun jsAddFont(fontFace: FontFace): JsAny =
    js(
        """
      {
          document.fonts.add(fontFace)
          return 0
      }
    """
    )

@OptIn(ExperimentalWasmJsInterop::class)
internal fun performanceNow(): Double = js("performance.now()")

@OptIn(ExperimentalWasmJsInterop::class)
external class FontFace : JsAny {
    fun load(): Promise<FontFace>
}

@OptIn(ExperimentalWasmJsInterop::class)
internal fun maxTexAniso(ext: JsAny): Int = js("ext.MAX_TEXTURE_MAX_ANISOTROPY_EXT")

@OptIn(ExperimentalWasmJsInterop::class)
internal fun texAniso(ext: JsAny): Int = js("ext.TEXTURE_MAX_ANISOTROPY_EXT")