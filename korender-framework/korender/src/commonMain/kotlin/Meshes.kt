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
    fun embed(prototype: Mesh, transform: Transform = Transform.IDENTITY, colorTexIndex: Int? = null)
}

interface Mesh {

    val vertices: List<Vertex>
    val indices: List<Int>?

    interface Vertex {
        val pos: Vec3?
        val normal: Vec3?
        val tex: Vec2?
        operator fun <T> get(attribute: MeshAttribute<T>): T?
    }
}

interface MutableMesh : Mesh {

    override val vertices: MutableList<MutableVertex>
    override val indices: MutableList<Int>

    fun createVertex(): MutableVertex
    fun appendVertex(): MutableVertex

    interface MutableVertex : Mesh.Vertex {

        override var pos: Vec3?
        override var normal: Vec3?
        override var tex: Vec2?

        operator fun <T> set(attribute: MeshAttribute<T>, value: T)

        fun pos(pos: Vec3): MutableVertex = apply { this.pos = pos }
        fun normal(normal: Vec3): MutableVertex = apply { this.normal = normal }
        fun tex(tex: Vec2): MutableVertex = apply { this.tex = tex }
    }
}


