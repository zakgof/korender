@file:Suppress("UNCHECKED_CAST")

package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.buffer.put
import com.zakgof.korender.impl.checkGlError
import com.zakgof.korender.impl.gl.GL.glBindBuffer
import com.zakgof.korender.impl.gl.GL.glBindBufferBase
import com.zakgof.korender.impl.gl.GL.glBindBufferRange
import com.zakgof.korender.impl.gl.GL.glBufferData
import com.zakgof.korender.impl.gl.GL.glBufferSubData
import com.zakgof.korender.impl.gl.GL.glDeleteBuffers
import com.zakgof.korender.impl.gl.GL.glGenBuffers
import com.zakgof.korender.impl.gl.GLConstants.GL_DYNAMIC_DRAW
import com.zakgof.korender.impl.gl.GLConstants.GL_UNIFORM_BUFFER
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal typealias UniformPack = Array<UniformSupplier?>

internal interface UniformSupplier {
    fun uniform(name: String): UniformGetter<*>?
}

internal interface CompositeSupplier {
    val child: InternalMaterialModifier? // TODO: UGLY!
        get() = null
}

internal class CompiledBlockBinding(
    val offset: Int,
    val name: String,
    val supplierIndex: Int,
    val getter: UniformGetter<Any>,
) {
    fun write(buffer: NativeByteBuffer, baseOffset: Int, uniformPack: UniformPack, materialName: String, ignoreMissing: Boolean) {
        buffer.position(baseOffset + offset)
        val obj = uniformPack[supplierIndex]!!
        getter.writeTo(buffer, obj) { if (!ignoreMissing) throw KorenderException("Material $materialName does not provide blocked uniform $name") }
    }
}

internal class IntGetter<T>(private val f: (T) -> Int?) : UniformGetter<T> {
    override fun writeTo(buffer: NativeByteBuffer, obj: T, missing: () -> Unit) {
        safe(f, obj, missing) { v ->
            buffer.put(v)
        }
    }
}

internal class FloatGetter<T>(private val f: (T) -> Float?) : UniformGetter<T> {
    override fun writeTo(buffer: NativeByteBuffer, obj: T, missing: () -> Unit) =
        safe(f, obj, missing) { v ->
            buffer.put(v)
        }
}

internal class Vec2Getter<T>(private val f: (T) -> Vec2?) : UniformGetter<T> {
    override fun writeTo(buffer: NativeByteBuffer, obj: T, missing: () -> Unit) =
        safe(f, obj, missing) { v ->
            buffer.put(v.x)
            buffer.put(v.y)
        }
}

internal class Vec3Getter<T>(private val f: (T) -> Vec3?) : UniformGetter<T> {
    override fun writeTo(buffer: NativeByteBuffer, obj: T, missing: () -> Unit) =
        safe(f, obj, missing) { v ->
            buffer.put(v.x)
            buffer.put(v.y)
            buffer.put(v.z)
        }
}

internal class ColorRGBGetter<T>(private val f: (T) -> ColorRGB?) : UniformGetter<T> {
    override fun writeTo(buffer: NativeByteBuffer, obj: T, missing: () -> Unit) =
        safe(f, obj, missing) { v ->
            buffer.put(v.r)
            buffer.put(v.g)
            buffer.put(v.b)
        }
}

internal class ColorRGBAGetter<T>(private val f: (T) -> ColorRGBA) : UniformGetter<T> {
    override fun writeTo(buffer: NativeByteBuffer, obj: T, missing: () -> Unit) =
        safe(f, obj, missing) { v ->
            buffer.put(v.r)
            buffer.put(v.g)
            buffer.put(v.b)
            buffer.put(v.a)
        }
}

internal class Mat4Getter<T>(private val f: (T) -> Mat4) : UniformGetter<T> {
    override fun writeTo(buffer: NativeByteBuffer, obj: T, missing: () -> Unit) =
        safe(f, obj, missing) { v ->
            buffer.put(v.asArray())
        }
}

internal class IntListGetter<T>(private val f: (T) -> List<Int>) : UniformGetter<T> {
    override fun writeTo(buffer: NativeByteBuffer, obj: T, missing: () -> Unit) =
        safe(f, obj, missing) { v ->
            v.forEach {
                buffer.put(it)
                buffer.put(0)
                buffer.put(0)
                buffer.put(0)
            }
        }
}

internal class FloatListGetter<T>(private val f: (T) -> List<Float>) : UniformGetter<T> {
    override fun writeTo(buffer: NativeByteBuffer, obj: T, missing: () -> Unit) =
        safe(f, obj, missing) { v ->
            v.forEach {
                buffer.put(it)
                buffer.put(0f)
                buffer.put(0f)
                buffer.put(0f)
            }
        }
}

internal class Vec3ListGetter<T>(private val f: (T) -> List<Vec3>) : UniformGetter<T> {
    override fun writeTo(buffer: NativeByteBuffer, obj: T, missing: () -> Unit) =
        safe(f, obj, missing) { v ->
            v.forEach {
                buffer.put(it.x)
                buffer.put(it.y)
                buffer.put(it.z)
                buffer.put(0f)
            }
        }
}

internal class Color3ListGetter<T>(private val f: (T) -> List<ColorRGB>) : UniformGetter<T> {
    override fun writeTo(buffer: NativeByteBuffer, obj: T, missing: () -> Unit) =
        safe(f, obj, missing) { v ->
            v.forEach {
                buffer.put(it.r)
                buffer.put(it.g)
                buffer.put(it.b)
                buffer.put(0f)
            }
        }
}

internal class Color4ListGetter<T>(private val f: (T) -> List<ColorRGBA>) : UniformGetter<T> {
    override fun writeTo(buffer: NativeByteBuffer, obj: T, missing: () -> Unit) =
        safe(f, obj, missing) { v ->
            v.forEach {
                buffer.put(it.r)
                buffer.put(it.g)
                buffer.put(it.b)
                buffer.put(it.a)
            }
        }
}

internal class Mat4ListGetter<T>(private val f: (T) -> List<Mat4>?) : UniformGetter<T> {
    override fun writeTo(buffer: NativeByteBuffer, obj: T, missing: () -> Unit) =
        safe(f, obj, missing) { v ->
            v.forEach {
                buffer.put(it.asArray())
            }
        }
}

internal class GlGpuUniformBuffer(size: Int) : AutoCloseable {

    private val ubo = glGenBuffers()
    private val uboBuffer = NativeByteBuffer(size)

    init {
        println("Creating GPU UBO : $ubo $size bytes")
        glBindBuffer(GL_UNIFORM_BUFFER, ubo)
        glBufferData(GL_UNIFORM_BUFFER, uboBuffer.size().toLong(), GL_DYNAMIC_DRAW)
    }

    fun bindBase(binding: Int) {
        glBindBuffer(GL_UNIFORM_BUFFER, ubo)
        glBindBufferBase(GL_UNIFORM_BUFFER, binding, ubo)
    }

    fun bindRange(binding: Int, shift: Int, size: Int) {
        glBindBuffer(GL_UNIFORM_BUFFER, ubo)
        glBindBufferRange(GL_UNIFORM_BUFFER, binding, ubo, shift, size)
    }

    fun populate(
        uniformPack: UniformPack,
        bufferShift: Int,
        bindings: List<CompiledBlockBinding>,
        materialName: String,
        ignoreMissing: Boolean = false,
    ) {
        bindings.forEach { binding ->
            binding.write(uboBuffer, bufferShift, uniformPack, materialName, ignoreMissing)
        }
    }

    fun upload() {
        glBindBuffer(GL_UNIFORM_BUFFER, ubo)
        // glBufferData(GL_UNIFORM_BUFFER, uboBuffer.size().toLong(), GL_DYNAMIC_DRAW);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, uboBuffer.rewind());
        checkGlError("upload")
    }

    override fun close() {
        println("Destroying GPU UBO [$ubo]")
        glDeleteBuffers(ubo)
    }
}
