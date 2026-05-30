package com.zakgof.korender.impl.context

import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.CubeTextureResources
import com.zakgof.korender.Image
import com.zakgof.korender.Image3D
import com.zakgof.korender.IndexType
import com.zakgof.korender.KorenderException
import com.zakgof.korender.Mesh
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.Texture3DDeclaration
import com.zakgof.korender.TextureArrayDeclaration
import com.zakgof.korender.TextureArrayImages
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.engine.MeshInstance
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
import com.zakgof.korender.impl.geometry.InternalInstancingParameter
import com.zakgof.korender.impl.geometry.InternalMeshDeclaration
import com.zakgof.korender.impl.geometry.MeshAttributes.COLOR
import com.zakgof.korender.impl.geometry.MeshAttributes.COLORTEXINDEX
import com.zakgof.korender.impl.geometry.MeshAttributes.METALLIC
import com.zakgof.korender.impl.geometry.MeshAttributes.NORMAL
import com.zakgof.korender.impl.geometry.MeshAttributes.POS
import com.zakgof.korender.impl.geometry.MeshAttributes.ROUGHNESS
import com.zakgof.korender.impl.geometry.ObjMesh
import com.zakgof.korender.impl.geometry.Quad
import com.zakgof.korender.impl.geometry.Sphere
import com.zakgof.korender.impl.image.InternalImage
import com.zakgof.korender.impl.image.impl.image.InternalImage3D
import com.zakgof.korender.impl.load
import com.zakgof.korender.impl.material.ImageCubeTextureDeclaration
import com.zakgof.korender.impl.material.ImageTexture3DDeclaration
import com.zakgof.korender.impl.material.InternalImageTextureArrayDeclaration
import com.zakgof.korender.impl.material.InternalImageTextureDeclaration
import com.zakgof.korender.impl.material.ResourceCubeTextureDeclaration
import com.zakgof.korender.impl.material.InternalResourceTextureArrayDeclaration
import com.zakgof.korender.impl.material.InternalResourceTextureDeclaration
import com.zakgof.korender.impl.material.simpleBlur
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform
import com.zakgof.korender.scope.InstancingParameter
import com.zakgof.korender.scope.InstancingScope
import com.zakgof.korender.scope.PipeMeshScope
import com.zakgof.korender.scope.ResourceScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

internal class NodeContext(
    val resourceLoader: ResourceLoader,
    val transform: Transform,
    override var retentionPolicy: RetentionPolicy,
    val time: Float? = null,
) : ResourceScope {

    override fun hashCode() = 0
    override fun equals(other: Any?) = true

    override fun texture(textureResource: String, filter: TextureFilter, wrap: TextureWrap, aniso: Int) =
        InternalResourceTextureDeclaration(textureResource, filter, wrap, aniso, this)

    override fun texture(id: String, image: Image, filter: TextureFilter, wrap: TextureWrap, aniso: Int) =
        InternalImageTextureDeclaration(id, image as InternalImage, filter, wrap, aniso, this)

    override fun texture3D(id: String, image: Image3D, filter: TextureFilter, wrap: TextureWrap, aniso: Int): Texture3DDeclaration =
        ImageTexture3DDeclaration(id, image as InternalImage3D, filter, wrap, aniso, this)

    override fun textureArray(vararg textureResources: String, filter: TextureFilter, wrap: TextureWrap, aniso: Int): TextureArrayDeclaration =
        InternalResourceTextureArrayDeclaration(textureResources.toList(), filter, wrap, aniso, this)

    override fun textureArray(id: String, images: TextureArrayImages, filter: TextureFilter, wrap: TextureWrap, aniso: Int): TextureArrayDeclaration =
        InternalImageTextureArrayDeclaration(id, images, filter, wrap, aniso, this)

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
    ): InternalMeshDeclaration =
        CustomMesh(id, vertexCount, indexCount, attributes.asList(), dynamic, indexType, this, block)

    @Suppress("UNCHECKED_CAST")
    override fun compositeMesh(id: String, prototypeMeshes: List<Pair<Mesh, Int>>, vararg attributes: MeshAttribute<*>, instancingParameters: Set<InstancingParameter>, dynamic: Boolean, block: InstancingScope.() -> Unit): MeshDeclaration {
        val additionalAttributes = (instancingParameters as Set<InternalInstancingParameter>).mapNotNull { it.composeMeshAttribute }
        return CustomMesh(
            id,
            prototypeMeshes.sumOf { it.first.vertices.size * it.second },
            prototypeMeshes.sumOf { (it.first.indices?.size ?: 0) * it.second },
            attributes.asList() + additionalAttributes,
            dynamic,
            indexType = null,
            this
        ) {
            val instances = mutableListOf<MeshInstance>()
            var indexOffset = 0
            var instanceIndex = 0
            DefaultInstancingScope(instances).block()

            additionalAttributes.forEach { attribute ->
                instanceIndex = 0
                prototypeMeshes.forEach { (proto, count) ->
                    repeat(count) {
                        val instance = instances[instanceIndex]
                        val instanceValue = when (attribute) {
                            COLOR -> instance.color ?: ColorRGBA.White
                            METALLIC -> instance.metallic ?: 0.0f
                            ROUGHNESS -> instance.roughness ?: 1.0f
                            COLORTEXINDEX -> instance.colorTextureIndex ?: 255
                            else -> throw KorenderException("Unknown instancing attribute $attribute")
                        }
                        repeat(proto.vertices.size) {
                            this.attr(attribute as MeshAttribute<Any>, instanceValue)
                        }
                        instanceIndex++
                    }
                }
            }

            instanceIndex = 0

            prototypeMeshes.forEach { (proto, count) ->
                repeat(count) {
                    val instance = instances[instanceIndex]
                    attributes.forEach { attribute ->
                        val transformablePos = attribute === POS && instancingParameters.contains(InternalInstancingParameter.TRANSFORM_INSTANCING)
                        val transformableNormal = attribute === NORMAL && instancingParameters.contains(InternalInstancingParameter.TRANSFORM_INSTANCING)
                        proto.vertices.forEach { protoVertex ->
                            if (transformablePos) {
                                val pos = protoVertex.pos!!
                                val transformedPos = (instance.transform ?: Transform.IDENTITY) * pos
                                this.pos(transformedPos)
                            } else if (transformableNormal) {
                                val normal = protoVertex.normal!!
                                val transformedNormal = (instance.transform ?: Transform.IDENTITY).mat4.invTranspose() * normal
                                this.normal(transformedNormal)
                            } else {
                                val attrValue = protoVertex[attribute]
                                this.attr(attribute as MeshAttribute<Any>, attrValue as Any)
                            }
                        }
                    }
                    proto.indices?.let {
                        index(*it.map { i -> i + indexOffset }.toIntArray())
                    }
                    indexOffset += proto.vertices.size
                    instanceIndex++

                }
            }
        }
    }


    override fun <T> load(resource: String, mapper: (ByteArray) -> T): Deferred<T> =
        CoroutineScope(Dispatchers.Default).async { mapper(resourceLoader.load(resource)) }

    override fun heightField(id: String, cellsX: Int, cellsZ: Int, cellWidth: Float, height: (Int, Int) -> Float): MeshDeclaration =
        HeightField(id, cellsX, cellsZ, cellWidth, height, this)

    override fun mesh(id: String, mesh: Mesh) =
        CustomCpuMesh(id, mesh, this)

    override fun loadMesh(meshDeclaration: MeshDeclaration): Deferred<Mesh> =
        Geometry.loadCpuMesh(meshDeclaration, resourceLoader)

    override fun pipeMesh(id: String, segments: Int, dynamic: Boolean, block: PipeMeshScope.() -> Unit) =
        createPipeMesh(id, segments, dynamic, this, block)

    override fun blur(radius: Float) =
        simpleBlur(radius, this)
}
