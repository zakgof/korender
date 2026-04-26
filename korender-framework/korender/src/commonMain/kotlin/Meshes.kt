package com.zakgof.korender

import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

/**
 * Data type for mesh indices.
 * - Byte: 8-bit indices (0-255)
 * - Short: 16-bit indices (0-65535)
 * - Int: 32-bit indices (0-4294967295)
 */
enum class IndexType {
    Byte,
    Short,
    Int
}

/**
 * Marker interface for custom mesh attributes (e.g., tangents, colors, custom vertex data).
 * @param T the type of the attribute value
 */
interface MeshAttribute<T>

/**
 * Immutable mesh declaration for use in rendering.
 * Implementations provide geometry data for 3D objects.
 */
interface MeshDeclaration

/**
 * Builder interface for constructing mesh geometry.
 * Provides a fluent API to define vertices, normals, texture coordinates, and indices.
 */
interface MeshInitializer {
    /**
     * Adds custom attribute values to vertices.
     * @param attr the mesh attribute to set
     * @param value attribute values (one per vertex)
     * @return this builder for chaining
     */
    fun <T> attr(attr: MeshAttribute<T>, vararg value: T): MeshInitializer

    /**
     * Adds vertex positions.
     * @param position vertex positions
     * @return this builder for chaining
     */
    fun pos(vararg position: Vec3): MeshInitializer

    /**
     * Adds vertex normals for lighting calculations.
     * @param normal vertex normals (should be normalized)
     * @return this builder for chaining
     */
    fun normal(vararg normal: Vec3): MeshInitializer

    /**
     * Adds texture coordinates for each vertex.
     * @param tex texture coordinates in UV space (0.0-1.0)
     * @return this builder for chaining
     */
    fun tex(vararg tex: Vec2): MeshInitializer

    /**
     * Adds vertex indices to define triangle topology.
     * @param indices vertex indices (groups of 3 form triangles)
     * @return this builder for chaining
     */
    fun index(vararg indices: Int): MeshInitializer

    /**
     * Sets vertex indices from raw byte data.
     * @param rawBytes raw index data in platform byte order
     * @return this builder for chaining
     */
    fun indexBytes(rawBytes: ByteArray): MeshInitializer

    /**
     * Sets custom attribute data from raw byte data.
     * @param attr the mesh attribute to set
     * @param rawBytes raw attribute data in platform byte order
     * @return this builder for chaining
     */
    fun <T> attrBytes(attr: MeshAttribute<T>, rawBytes: ByteArray): MeshInitializer

    /**
     * Sets a custom attribute value for a specific vertex.
     * @param attr the mesh attribute to set
     * @param index vertex index (0-based)
     * @param value attribute value for the vertex
     * @return this builder for chaining
     */
    fun <T> attrSet(attr: MeshAttribute<T>, index: Int, value: T): MeshInitializer

    /**
     * Embeds another mesh into this one with optional transform and color texture index mapping.
     * Useful for combining meshes or creating complex geometries.
     * @param prototype the mesh to embed
     * @param transform transformation to apply to embedded mesh vertices
     * @param colorTexIndex optional color texture index for the embedded mesh
     */
    fun embed(prototype: Mesh, transform: Transform = Transform.IDENTITY, colorTexIndex: Int? = null)
}

/**
 * Immutable mesh geometry.
 * Contains vertex data and optional triangle indices.
 */
interface Mesh {

    /**
     * List of mesh vertices. Accessible via indices if they exist.
     */
    val vertices: List<Vertex>

    /**
     * Optional list of triangle vertex indices.
     * If null, vertices define triangles in order (0,1,2), (3,4,5), etc.
     * If present, indices group vertices into triangles (in sets of 3).
     */
    val indices: List<Int>?

    /**
     * Represents a single vertex in the mesh.
     */
    interface Vertex {
        /**
         * Vertex position in local mesh space. May be null for some mesh types.
         */
        val pos: Vec3?

        /**
         * Vertex normal for lighting calculations (should be normalized). May be null if auto-calculated.
         */
        val normal: Vec3?

        /**
         * Texture coordinate (UV) in [0.0, 1.0] range. May be null if not texture-mapped.
         */
        val tex: Vec2?

        /**
         * Gets a custom attribute value for this vertex.
         * @param attribute the mesh attribute to retrieve
         * @return the attribute value or null if not set
         */
        operator fun <T> get(attribute: MeshAttribute<T>): T?
    }
}

/**
 * Mutable mesh geometry that can be modified after creation.
 * Extends [Mesh] with the ability to add/modify vertices and indices.
 */
interface MutableMesh : Mesh {

    /**
     * Mutable list of vertices. Can be directly modified.
     */
    override val vertices: MutableList<MutableVertex>

    /**
     * Mutable list of triangle indices. Can be directly modified.
     */
    override val indices: MutableList<Int>

    /**
     * Creates a new vertex in the mesh (initially uninitialized).
     * Vertex must be added to vertices list manually.
     * @return a new mutable vertex
     */
    fun createVertex(): MutableVertex

    /**
     * Creates and appends a new vertex to the mesh.
     * @return the newly appended vertex (already added to vertices list)
     */
    fun appendVertex(): MutableVertex

    /**
     * Mutable vertex that can have its properties modified.
     */
    interface MutableVertex : Mesh.Vertex {

        /**
         * Mutable vertex position in local mesh space.
         */
        override var pos: Vec3?

        /**
         * Mutable vertex normal (should be normalized for lighting).
         */
        override var normal: Vec3?

        /**
         * Mutable texture coordinate.
         */
        override var tex: Vec2?

        /**
         * Sets a custom attribute value for this vertex.
         * @param attribute the mesh attribute to set
         * @param value the attribute value
         */
        operator fun <T> set(attribute: MeshAttribute<T>, value: T)

        /**
         * Builder method to set position and return this vertex.
         * @param pos vertex position
         * @return this vertex for chaining
         */
        fun pos(pos: Vec3): MutableVertex = apply { this.pos = pos }

        /**
         * Builder method to set normal and return this vertex.
         * @param normal vertex normal
         * @return this vertex for chaining
         */
        fun normal(normal: Vec3): MutableVertex = apply { this.normal = normal }

        /**
         * Builder method to set texture coordinate and return this vertex.
         * @param tex texture coordinate
         * @return this vertex for chaining
         */
        fun tex(tex: Vec2): MutableVertex = apply { this.tex = tex }
    }
}


