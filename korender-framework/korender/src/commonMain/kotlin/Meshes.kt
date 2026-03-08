package com.zakgof.korender

import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3


enum class IndexType {
    Byte,
    Short,
    Int
}

interface MeshAttribute<T>

interface MeshDeclaration

interface MeshInitializer {
    fun <T> attr(attr: MeshAttribute<T>, vararg value: T): MeshInitializer
    fun pos(vararg position: Vec3): MeshInitializer
    fun normal(vararg normal: Vec3): MeshInitializer
    fun tex(vararg tex: Vec2): MeshInitializer
    fun index(vararg indices: Int): MeshInitializer
    fun indexBytes(rawBytes: ByteArray): MeshInitializer
    fun <T> attrBytes(attr: MeshAttribute<T>, rawBytes: ByteArray): MeshInitializer
    fun <T> attrSet(attr: MeshAttribute<T>, index: Int, value: T): MeshInitializer
    fun embed(prototype: Mesh, transform: Transform = Transform.IDENTITY, indexOffset: Long = 0)
}

interface Mesh {

    val vertices: List<Vertex>
    val indices: List<Int>?

    interface Vertex {
        val pos: Vec3?
        val normal: Vec3?
        val tex: Vec2?
        fun <T> value(attribute: MeshAttribute<T>): T?
    }
}

class MutableMesh : Mesh {

    override val vertices = mutableListOf<MutableVertex>()
    override val indices = mutableListOf<Int>()

    class MutableVertex : Mesh.Vertex {

        override var pos: Vec3? = null
        override var normal: Vec3? = null
        override var tex: Vec2? = null

        fun pos(p: Vec3) = apply { pos = p }
        fun normal(n: Vec3) = apply { normal = n }
        fun tex(t: Vec2) = apply { tex = t }

        @Suppress("UNCHECKED_CAST")
        override fun <T> value(attribute: MeshAttribute<T>): T? = when (attribute) {
//            POS -> pos
//            NORMAL -> normal
//            TEX -> tex
            else -> null
        } as T?
    }
}
