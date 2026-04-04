package com.zakgof.korender.impl.context

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
import com.zakgof.korender.Prefab
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.TerrainMaterial
import com.zakgof.korender.Texture3DDeclaration
import com.zakgof.korender.TextureArrayDeclaration
import com.zakgof.korender.TextureArrayImages
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.context.PipeMeshContext
import com.zakgof.korender.context.ResourceScope
import com.zakgof.korender.impl.engine.createPipeMesh
import com.zakgof.korender.impl.geometry.BiQuad
import com.zakgof.korender.impl.geometry.ConeTop
import com.zakgof.korender.impl.geometry.Cube
import com.zakgof.korender.impl.geometry.CustomCpuMesh
import com.zakgof.korender.impl.geometry.CustomMesh
import com.zakgof.korender.impl.geometry.CylinderSide
import com.zakgof.korender.impl.geometry.Disk
import com.zakgof.korender.impl.geometry.Geometry
import com.zakgof.korender.impl.geometry.HeightField
import com.zakgof.korender.impl.geometry.ObjMesh
import com.zakgof.korender.impl.geometry.Quad
import com.zakgof.korender.impl.geometry.Sphere
import com.zakgof.korender.impl.image.InternalImage
import com.zakgof.korender.impl.image.impl.image.InternalImage3D
import com.zakgof.korender.impl.load
import com.zakgof.korender.impl.material.ImageCubeTextureDeclaration
import com.zakgof.korender.impl.material.ImageTexture3DDeclaration
import com.zakgof.korender.impl.material.ImageTextureArrayDeclaration
import com.zakgof.korender.impl.material.ImageTextureDeclaration
import com.zakgof.korender.impl.material.ResourceCubeTextureDeclaration
import com.zakgof.korender.impl.material.ResourceTextureArrayDeclaration
import com.zakgof.korender.impl.material.ResourceTextureDeclaration
import com.zakgof.korender.impl.material.bloomMipEffect
import com.zakgof.korender.impl.material.bloomSimpleEffect
import com.zakgof.korender.impl.material.simpleBlur
import com.zakgof.korender.impl.material.ssrEffect
import com.zakgof.korender.impl.prefab.terrain.Clipmaps
import com.zakgof.korender.math.Transform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

internal class NodeContext(
    val resourceLoader: ResourceLoader,
    val transform: Transform,
    override var retentionPolicy: RetentionPolicy
) : ResourceScope {

    override fun texture(textureResource: String, filter: TextureFilter, wrap: TextureWrap, aniso: Int) =
        ResourceTextureDeclaration(textureResource, filter, wrap, aniso, this)

    override fun texture(id: String, image: Image, filter: TextureFilter, wrap: TextureWrap, aniso: Int) =
        ImageTextureDeclaration(id, image as InternalImage, filter, wrap, aniso, this)

    override fun texture3D(id: String, image: Image3D, filter: TextureFilter, wrap: TextureWrap, aniso: Int): Texture3DDeclaration =
        ImageTexture3DDeclaration(id, image as InternalImage3D, filter, wrap, aniso, this)

    override fun textureArray(vararg textureResources: String, filter: TextureFilter, wrap: TextureWrap, aniso: Int): TextureArrayDeclaration =
        ResourceTextureArrayDeclaration(textureResources.toList(), filter, wrap, aniso, this)

    override fun textureArray(id: String, images: TextureArrayImages, filter: TextureFilter, wrap: TextureWrap, aniso: Int): TextureArrayDeclaration =
        ImageTextureArrayDeclaration(id, images, filter, wrap, aniso, this)

    override fun cubeTexture(resources: CubeTextureResources) = ResourceCubeTextureDeclaration(resources, this)

    override fun cubeTexture(id: String, images: CubeTextureImages) = ImageCubeTextureDeclaration(id, images, this)

    override fun quad(halfSideX: Float, halfSideY: Float) = Quad(halfSideX, halfSideY, this)

    override fun biQuad(halfSideX: Float, halfSideY: Float) = BiQuad(halfSideX, halfSideY, this)

    override fun cube(halfSide: Float) = Cube(halfSide, this)

    override fun sphere(radius: Float, slices: Int, sectors: Int) = Sphere(radius, slices, sectors, this)

    override fun cylinderSide(height: Float, radius: Float, sectors: Int) = CylinderSide(height, radius, sectors, this)

    override fun coneTop(height: Float, radius: Float, sectors: Int) = ConeTop(height, radius, sectors, this)

    override fun disk(radius: Float, sectors: Int) = Disk(radius, sectors, this)

    override fun obj(objFile: String): MeshDeclaration = ObjMesh(objFile, this)

    override fun customMesh(
        id: String,
        vertexCount: Int,
        indexCount: Int,
        vararg attributes: MeshAttribute<*>,
        dynamic: Boolean,
        indexType: IndexType?,
        block: MeshInitializer.() -> Unit,
    ): MeshDeclaration =
        CustomMesh(id, vertexCount, indexCount, attributes.asList(), dynamic, indexType, this, block)

    override fun <T> load(resource: String, mapper: (ByteArray) -> T): Deferred<T> =
        CoroutineScope(Dispatchers.Default).async { mapper(resourceLoader.load(resource)) }

    override fun heightField(id: String, cellsX: Int, cellsZ: Int, cellWidth: Float, height: (Int, Int) -> Float): MeshDeclaration =
        HeightField(id, cellsX, cellsZ, cellWidth, height, this)

    override fun mesh(id: String, mesh: Mesh) =
        CustomCpuMesh(id, mesh, this)

    override fun loadMesh(meshDeclaration: MeshDeclaration): Deferred<Mesh> =
        Geometry.loadCpuMesh(meshDeclaration, resourceLoader)

    override fun pipeMesh(id: String, segments: Int, dynamic: Boolean, block: PipeMeshContext.() -> Unit) =
        createPipeMesh(id, segments, dynamic, this, block)

    override fun blur(radius: Float) =
        simpleBlur(radius, this)

    override fun ssr(
        downsample: Int,
        maxReflectionDistance: Float,
        linearSteps: Int,
        binarySteps: Int,
        lastStepRatio: Float,
        envTexture: CubeTextureDeclaration?,
    ) =
        ssrEffect(downsample, maxReflectionDistance, linearSteps, binarySteps, lastStepRatio, envTexture, this)

    override fun bloom(threshold: Float, amount: Float, radius: Float, downsample: Int) =
        bloomSimpleEffect(threshold, amount, radius, downsample, this)

    override fun bloomWide(threshold: Float, amount: Float, downsample: Int, mips: Int, offset: Float, highResolutionRatio: Float) =
        bloomMipEffect(threshold, amount, downsample, mips, offset, highResolutionRatio, this)

    override fun clipmapTerrainPrefab(id: String, cellSize: Float, hg: Int, rings: Int): Prefab<TerrainMaterial> =
        Clipmaps(this, id, cellSize, hg, rings)
}
