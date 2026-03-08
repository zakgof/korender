package com.zakgof.korender.impl.geometry

import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MutableMesh
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal class InternalMutableMesh : MutableMesh {

    override val vertices = mutableListOf<MutableMesh.MutableVertex>()
    override val indices = mutableListOf<Int>()

    override fun createVertex(): MutableMesh.MutableVertex = InternalMutableVertex()

    override fun appendVertex(): MutableMesh.MutableVertex =
        InternalMutableVertex().also { vertices += it }

    internal class InternalMutableVertex : MutableMesh.MutableVertex {

        private val attributes = mutableMapOf<MeshAttribute<*>, Any?>()

        override var pos: Vec3?
            get() = attributes[MeshAttributes.POS] as Vec3?
            set(value) {
                attributes[MeshAttributes.POS] = value
            }

        override var normal: Vec3?
            get() = attributes[MeshAttributes.NORMAL] as Vec3?
            set(value) {
                attributes[MeshAttributes.NORMAL] = value
            }

        override var tex: Vec2?
            get() = attributes[MeshAttributes.TEX] as Vec2?
            set(value) {
                attributes[MeshAttributes.TEX] = value
            }

        override operator fun <T> set(attribute: MeshAttribute<T>, value: T) {
            attributes[attribute] = value
        }

        @Suppress("UNCHECKED_CAST")
        override operator fun <T> get(attribute: MeshAttribute<T>): T? =
            attributes[attribute] as T?
    }
}




