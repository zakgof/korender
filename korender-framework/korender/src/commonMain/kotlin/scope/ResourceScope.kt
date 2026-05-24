package com.zakgof.korender.scope

import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.CubeTextureResources
import com.zakgof.korender.Image
import com.zakgof.korender.Image3D
import com.zakgof.korender.IndexType
import com.zakgof.korender.Mesh
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.PostProcessingEffect
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.Texture3DDeclaration
import com.zakgof.korender.TextureArrayDeclaration
import com.zakgof.korender.TextureArrayImages
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import kotlinx.coroutines.Deferred

/**
 * Scope for declaring and loading GPU resources such as textures, meshes, and materials.
 */
interface ResourceScope {

    /** Current object retention policy. */
    var retentionPolicy: RetentionPolicy

    /**
     * Helper method to load a resource file into an object.
     *
     * @param resource resource file name
     * @param mapper function to instantiate object representation from the raw file's bytes
     * @return deferred object
     */
    fun <T> load(resource: String, mapper: (ByteArray) -> T): Deferred<T>

    /**
     * Creates a texture declaration from a resource image file.
     *
     * @param textureResource resource file name (png, jpg)
     * @param filter texture filtering mode
     * @param wrap texture wrapping mode
     * @param aniso anisotropy factor
     * @return texture declaration
     */
    fun texture(textureResource: String, filter: TextureFilter = TextureFilter.MipMap, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024): TextureDeclaration

    /**
     * Creates a texture declaration from an Image object.
     *
     * @param id unique declaration id
     * @param image Image object
     * @param filter texture filtering mode
     * @param wrap texture wrapping mode
     * @param aniso anisotropy factor
     * @return texture declaration
     */
    fun texture(id: String, image: Image, filter: TextureFilter = TextureFilter.MipMap, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024): TextureDeclaration

    /**
     * Creates a texture array declaration from resource image files.
     *
     * @param textureResources resource file names (png, jpg)
     * @param filter texture filtering mode
     * @param wrap texture wrapping mode
     * @param aniso anisotropy factor
     * @return texture array declaration
     */
    fun textureArray(vararg textureResources: String, filter: TextureFilter = TextureFilter.MipMap, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024): TextureArrayDeclaration

    /**
     * Creates a texture array declaration from Image objects.
     *
     * @param id unique declaration id
     * @param images image list
     * @param filter texture filtering mode
     * @param wrap texture wrapping mode
     * @param aniso anisotropy factor
     * @return texture array declaration
     */
    fun textureArray(id: String, images: TextureArrayImages, filter: TextureFilter = TextureFilter.MipMap, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024): TextureArrayDeclaration

    /**
     * Creates a 3D texture from an Image3D object.
     *
     * @param id unique declaration id
     * @param image Image3D object
     * @param filter texture filtering mode
     * @param wrap texture wrapping mode
     * @param aniso anisotropy factor
     * @return 3D texture declaration
     */
    fun texture3D(id: String, image: Image3D, filter: TextureFilter = TextureFilter.MipMap, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024): Texture3DDeclaration

    /**
     * Creates a cube texture declaration from resource files.
     *
     * Cube texture is a set of 6 textures representing cube sides.
     *
     * @param resources CubeTextureResources map containing image resource file name for every cube side
     * @return cube texture declaration
     */
    fun cubeTexture(resources: CubeTextureResources): CubeTextureDeclaration

    /**
     * Creates a cube texture declaration from Image objects.
     *
     * Cube texture is a set of 6 textures representing cube sides.
     *
     * @param id unique declaration id
     * @param images CubeTextureImages map containing image object for every cube side
     * @return cube texture declaration
     */
    fun cubeTexture(id: String, images: CubeTextureImages): CubeTextureDeclaration

    /**
     * Creates a quad mesh declaration.
     *
     * Creates z-axis facing quad consisting of two triangles.
     *
     * @param halfSideX half of quad width (in x dimension)
     * @param halfSideY half of quad height (in y dimension)
     * @return mesh declaration
     */
    fun quad(halfSideX: Float = 0.5f, halfSideY: Float = 0.5f): MeshDeclaration

    /**
     * Creates a two-sided quad mesh declaration.
     *
     * Creates a quad in XY visible from both sides.
     *
     * @param halfSideX half of quad width (in x dimension)
     * @param halfSideY half of quad height (in y dimension)
     * @return mesh declaration
     */
    fun biQuad(halfSideX: Float = 0.5f, halfSideY: Float = 0.5f): MeshDeclaration

    /**
     * Creates a cube mesh declaration.
     *
     * @param halfSide half of cube edge length
     * @return mesh declaration
     */
    fun cube(halfSide: Float = 0.5f): MeshDeclaration

    /**
     * Creates a sphere mesh declaration.
     *
     * @param radius sphere radius
     * @param slices number of horizontal slices (along parallels)
     * @param sectors number of vertical sector slices (along meridians)
     * @return mesh declaration
     */
    fun sphere(radius: Float = 1.0f, slices: Int = 32, sectors: Int = 32): MeshDeclaration

    /**
     * Creates a cylindrical surface mesh declaration (a cylinder without bases).
     *
     * @param height cylinder height (in y direction)
     * @param radius base radius (base is in xz plane)
     * @param sectors number of stripes
     * @return mesh declaration
     */
    fun cylinderSide(height: Float = 1f, radius: Float = 1f, sectors: Int = 32): MeshDeclaration

    /**
     * Creates a conical surface mesh declaration (a cone without base).
     *
     * @param height cone height (in y direction)
     * @param radius base radius (base is in xz plane)
     * @param sectors number of slices
     * @return mesh declaration
     */
    fun coneTop(height: Float = 1f, radius: Float = 1f, sectors: Int = 32): MeshDeclaration

    /**
     * Creates a disk mesh declaration (in xz plane).
     *
     * @param radius disk radius
     * @param sectors number of sector slices
     * @return mesh declaration
     */
    fun disk(radius: Float = 1f, sectors: Int = 32): MeshDeclaration

    /**
     * Creates a mesh declaration for a wavefront .obj resource file.
     *
     * Only loads a single mesh without materials.
     *
     * @param objFile .obj resource file name
     * @return mesh declaration
     */
    fun obj(objFile: String): MeshDeclaration

    /**
     * Creates a mesh declaration for a shape consisting of multiple connected cylinders (pipes).
     *
     * @param id unique declaration id
     * @param segments number of pipe segments
     * @param dynamic set to true if the content can change frame to frame
     * @param block pipe segment declaration block
     * @return mesh declaration
     */
    fun pipeMesh(id: String, segments: Int, dynamic: Boolean = false, block: PipeMeshScope.() -> Unit): MeshDeclaration

    /**
     * Creates a mesh declaration from a Mesh object.
     *
     * @param id unique declaration id
     * @param mesh Mesh object
     * @return mesh declaration
     */
    fun mesh(id: String, mesh: Mesh): MeshDeclaration

    /**
     * Creates a mesh declaration from an indexed triangle list.
     *
     * @param id unique declaration id
     * @param vertexCount number of vertices
     * @param indexCount number of indices
     * @param attributes mesh vertex attributes
     * @param dynamic set to true if content can change frame to frame
     * @param indexType index type (omit for auto)
     * @param block block declaring mesh vertices and indices
     * @return mesh declaration
     */
    fun customMesh(
        id: String,
        vertexCount: Int,
        indexCount: Int,
        vararg attributes: MeshAttribute<*>,
        dynamic: Boolean = false,
        indexType: IndexType? = null,
        block: MeshInitializer.() -> Unit,
    ): MeshDeclaration

    /**
     * Creates a composite mesh declaration by combining multiple prototype meshes,
     * where each prototype is instanced a specified number of times.
     *
     * @param id unique declaration id
     * @param prototypeMeshes list of pairs mapping each prototype Mesh to its maximum instance count
     * @param attributes mesh vertex attributes to be included in the composite mesh
     * @param instancingParameters set of instancing parameters defining which attributes are enabled
     * @param dynamic set to true if the instance attributes can change frame to frame
     * @param block configuration block to define the instances of the prototypes sequentially
     * @return composite mesh declaration
     */
    fun compositeMesh(
        id: String,
        prototypeMeshes: List<Pair<Mesh, Int>>,
        vararg attributes: MeshAttribute<*>,
        instancingParameters: Set<InstancingParameter>,
        dynamic: Boolean = false,
        block: InstancingScope.() -> Unit,
    ): MeshDeclaration


    /**
     * Loads a Mesh from a MeshDeclaration.
     * @param meshDeclaration mesh declaration
     * @return deferred mesh
     */
    fun loadMesh(meshDeclaration: MeshDeclaration): Deferred<Mesh>

    /**
     * Creates a heightfield mesh declaration.
     *
     * @param id unique declaration id
     * @param cellsX number of cells in x direction
     * @param cellsZ number of cells in z direction
     * @param cellWidth cell width (in x and z)
     * @param height height function (in y) by cell index in x and z
     * @return mesh declaration
     */
    fun heightField(id: String, cellsX: Int, cellsZ: Int, cellWidth: Float, height: (Int, Int) -> Float): MeshDeclaration


    /**
     * Creates gaussian blur post-processing effect.
     * @param radius blur radius in pixels
     * @return post processing effect
     */
    fun blur(radius: Float): PostProcessingEffect


}
