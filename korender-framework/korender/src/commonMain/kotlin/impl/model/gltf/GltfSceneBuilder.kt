package com.zakgof.korender.impl.model.gltf

import com.zakgof.korender.BaseMaterialScope
import com.zakgof.korender.KorenderException
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.engine.MeshInstance
import com.zakgof.korender.impl.engine.ModelDeclaration
import com.zakgof.korender.impl.engine.ModelInstance
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.geometry.CMesh
import com.zakgof.korender.impl.geometry.CustomCpuMesh
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.InternalInstancingParameter
import com.zakgof.korender.impl.geometry.InternalMeshDeclaration
import com.zakgof.korender.impl.material.InternalBaseMaterial
import com.zakgof.korender.impl.material.InternalByteArrayTextureDeclaration
import com.zakgof.korender.impl.material.InternalMaterial
import com.zakgof.korender.impl.material.Plugins
import com.zakgof.korender.impl.model.InternalModelInfo
import com.zakgof.korender.impl.projection.FrustumProjectionMode
import com.zakgof.korender.impl.projection.OrthoProjectionMode
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import kotlin.math.floor
import kotlin.math.tan

internal class InstanceData(nodes: Int) {
    val nodeMatrices = Array(nodes) { Transform.IDENTITY }
    val nodeLocalMatrices = Array(nodes) { Transform.IDENTITY }
    val nodeAnimations = Array(nodes) { NodeAnimation(null, null, null) }
    val jointMatrices = mutableListOf<List<Mat4>>()
}

internal class NodeAnimation(
    var translation: List<Float>?,
    var rotation: List<Float>?,
    var scale: List<Float>?,
) {
    fun populate(path: String, floats: List<Float>) = when (path) {
        "translation" -> translation = floats
        "rotation" -> rotation = floats
        "scale" -> scale = floats
        else -> {}
    }
}

internal class GltfSceneBuilder(private val cache: GltfCache, private val declaration: ModelDeclaration) {
    private val cameraTransforms = MutableList(cache.model.cameras?.size ?: 0) { Transform() }
    private val meshNodes = mutableListOf<Pair<Int, Int>>()
    private val instances = declaration.instancingDeclaration?.instancer?.invoke() ?: listOf(ModelInstance(Transform.IDENTITY, declaration.time, declaration.animation))
    private val instanceData: Array<InstanceData> = Array(instances.size) { InstanceData(cache.model.nodes?.size ?: 0) }

    fun build(): List<RenderableDeclaration> {

        val model = cache.model
        val scene = model.scenes!![model.scene]

        scene.nodes.forEach { collectMeshesFromNode(it) }

        instanceData.mapIndexed { index, instanceData ->
            calculateInstanceData(index, instanceData)
            scene.nodes.forEach { nodeIndex ->
                processNode(instanceData, Transform.IDENTITY, nodeIndex, model.nodes!![nodeIndex])
            }
            instanceData.jointMatrices += model.skins?.map { skin ->
                skin.joints.map { instanceData.nodeMatrices[it].mat4 }
            } ?: listOf() // TODO optimize
        }
        val meshIndexToRenderables = meshNodes.map {
            it.first to createRenderables(it.first, cache.model.nodes!![it.second].skin, it.second)
        }
        declaration.onUpdate?.invoke(calculateModelInfo(scene, meshIndexToRenderables.toMap()))
        return meshIndexToRenderables.flatMap { it.second }
    }

    private fun calculateModelInfo(scene: InternalGltfFileModel.Scene, meshIndexToRenderables: Map<Int, List<RenderableDeclaration>>): InternalModelInfo {
        val instances: List<InternalModelInfo.Node> = instanceData.map { instanceData ->
            val instanceNodes = scene.nodes.map { nodeIndex ->
                calculateNodeInfo(instanceData, nodeIndex, meshIndexToRenderables)
            }
            InternalModelInfo.Node("instances", null, instanceNodes, null)
        }
        return InternalModelInfo(
            instances,
            cache.model.animations?.map { InternalModelInfo.Animation(it.name) },
            cache.model.cameras?.mapIndexed { index, cam ->
                InternalModelInfo.Camera(
                    cam.name,
                    DefaultCamera(cameraTransforms[index].mat4),
                    cam.toProjection()
                )
            })
    }

    private fun calculateNodeInfo(instanceData: InstanceData, nodeIndex: Int, meshIndexToRenderables: Map<Int, List<RenderableDeclaration>>): InternalModelInfo.Node {
        val node: InternalGltfFileModel.Node = cache.model.nodes!![nodeIndex]
        val childNodes = node.children?.map { childNodeIndex ->
            calculateNodeInfo(instanceData, childNodeIndex, meshIndexToRenderables)
        }
        val renderables = node.mesh?.let { meshIndex: Int ->
            val mesh = cache.model.meshes!![meshIndex]
            meshIndexToRenderables[meshIndex]!!.map { renderable ->
                InternalModelInfo.Renderable(
                    mesh.name,
                    (renderable.mesh as CustomCpuMesh).mesh as CMesh,
                    (renderable.material as InternalBaseMaterial).toMaterialInfo()
                )
            }
        }
        return InternalModelInfo.Node(null, instanceData.nodeLocalMatrices[nodeIndex], childNodes, renderables)
    }

    private fun calculateInstanceData(instanceIndex: Int, instanceData: InstanceData) {
        if (cache.model.animations?.isNotEmpty() == true) {
            val instanceDeclaration = instances[instanceIndex]
            val animationIndex = (instanceDeclaration.animation ?: declaration.animation).coerceIn(0, cache.model.animations.size - 1)
            val animation = cache.model.animations[animationIndex]
            animation.channels.forEach { channel ->
                channel.target.node?.let {
                    val samplerValue = getSamplerValue(animation.samplers[channel.sampler], instanceDeclaration.time ?: declaration.time)
                    instanceData.nodeAnimations[channel.target.node].populate(channel.target.path, samplerValue)
                }
            }
        }
    }

    private fun getSamplerValue(sampler: InternalGltfFileModel.Animation.AnimationSampler, currentTime: Float): List<Float> {

        // TODO validate float input and output
        // TODO support other types of samplers
        val inputFloats = cache.accessors.floats[sampler.input]!!
        val outputValues = cache.accessors.floatArrays[sampler.output] ?: return listOf(0f) // TODO this is ugly fallback

        // TODO validate same lengths
        val max = inputFloats.last()
        val timeOffset = currentTime - floor(currentTime / max) * max

        var samplerPositionBefore =
            inputFloats.indexOfLast { timeOffset > it } // TODO linear scan - ineffective!
        if (samplerPositionBefore < 0) samplerPositionBefore = 0

        // TODO this is STEP, implement other interpolations
        val output = outputValues[samplerPositionBefore]
        return output
    }

    private fun collectMeshesFromNode(nodeIndex: Int) {
        val node = cache.model.nodes!![nodeIndex]
        node.mesh?.let { meshNodes.add(it to nodeIndex) }
        node.children?.forEach { childNodeIndex ->
            collectMeshesFromNode(childNodeIndex)
        }
    }

    private fun processNode(instanceData: InstanceData, parentTransform: Transform, nodeIndex: Int, node: InternalGltfFileModel.Node) {

        var localTransform = Transform.IDENTITY

        val na = instanceData.nodeAnimations[nodeIndex]

        val translation = na.translation ?: node.translation
        val rotation = na.rotation ?: node.rotation
        val scale = na.scale ?: node.scale

        scale?.let { localTransform = localTransform.scale(it[0], it[1], it[2]) }
        rotation?.let { localTransform = localTransform.rotate(Quaternion(it[3], Vec3(it[0], it[1], it[2]))) }
        translation?.let { localTransform = localTransform.translate(Vec3(it[0], it[1], it[2])) }

        node.matrix?.let { localTransform *= Transform(Mat4(it.toFloatArray())) }

        val transform = if (localTransform === Transform.IDENTITY) parentTransform else parentTransform * localTransform

        node.camera?.let {
            cameraTransforms[it] = transform
        }

        instanceData.nodeLocalMatrices[nodeIndex] = localTransform
        instanceData.nodeMatrices[nodeIndex] = transform

        node.children?.forEach { childNodeIndex ->
            processNode(instanceData, transform, childNodeIndex, cache.model.nodes!![childNodeIndex])
        }
    }

    private fun createRenderables(meshIndex: Int, skinIndex: Int?, nodeIndex: Int): List<RenderableDeclaration> =
        cache.model.meshes!![meshIndex].primitives.mapIndexed { primitiveIndex, primitive ->
            val meshDeclaration = createMeshDeclaration(meshIndex, primitiveIndex, skinIndex)
            val jointMatrices = skinIndex?.let {
                if (declaration.instancingDeclaration == null) instanceData[0].jointMatrices[it] else null
            }
            val transparencyToMaterial = createMaterial(primitive, skinIndex, jointMatrices, declaration.materialModifier)

            // TODO why this works
            val meshTransform = if (skinIndex == null) instanceData[0].nodeMatrices[nodeIndex].mat4 else Mat4.IDENTITY
            val transform = declaration.transform * Transform(meshTransform)
            RenderableDeclaration(
                transparencyToMaterial.second,
                mesh = meshDeclaration,
                transform = transform,
                transparent = transparencyToMaterial.first,
                nodeContext = declaration.nodeContext,
            )
        }

    private fun createMaterial(
        primitive: InternalGltfFileModel.Mesh.Primitive,
        skinIndex: Int?,
        jointMatrices: List<Mat4>?,
        materialModifier: BaseMaterialScope.() -> Unit,
    ): Pair<Boolean, InternalMaterial> {

        // TODO: split into 2 parts, precompute textures modifier, calc only skin modifier
        val material = primitive.material?.let { cache.model.materials!![it] }
        val matPbr = material?.pbrMetallicRoughness
        val matSpecularGlossiness = material?.extensions?.get("KHR_materials_pbrSpecularGlossiness")
                as? InternalGltfFileModel.KHRMaterialsPbrSpecularGlossiness

        // TODO: Precreate all except jointMatrices
        val mat = InternalBaseMaterial()

        if (skinIndex != null) {
            mat.plugin(Plugins.VPOSITION_SKINNING)
            mat.plugin(Plugins.VNORMAL_SKINNING)
            if (declaration.instancingDeclaration == null && jointMatrices != null) {
                val jointMatrixList = jointMatrices.mapIndexed { ind, jm ->
                    jm * cache.loadedSkins[skinIndex]!![ind]
                }
                mat.jntMatrices = jointMatrixList
            }
        }

        mat.color = (matSpecularGlossiness?.diffuseFactor ?: matPbr?.baseColorFactor)?.let {
            ColorRGBA(it[0], it[1], it[2], it[3])
        } ?: ColorRGBA.White

        (matPbr?.baseColorTexture ?: matSpecularGlossiness?.diffuseTexture)?.let { getTexture(it) }?.let {
            mat.colorTexture = it
        }
        val alphaCutoff = if (material?.alphaMode == "MASK") material.alphaCutoff ?: 0.001f else 0.001f
        mat.alphaCutoff = alphaCutoff

        mat.metallicFactor = matPbr?.metallicFactor ?: 0.1f
        mat.roughnessFactor = matPbr?.roughnessFactor ?: 0.5f

        mat.normalTexture = material?.normalTexture?.let { getTexture(it) }
        mat.metallicRoughnessTexture = matPbr?.metallicRoughnessTexture?.let { getTexture(it) }
        mat.occlusionTexture = material?.occlusionTexture?.let { getTexture(it) }
        mat.emissionTexture = material?.emissiveTexture?.let { getTexture(it) }

        matSpecularGlossiness?.let { sg ->
            mat.specularGlossiness {
                specularFactor = sg.specularFactor.let { ColorRGB(it[0], it[1], it[2]) }
                glossinessFactor = sg.glossinessFactor
                texture = sg.specularGlossinessTexture?.let { getTexture(it) }
            }
        }
        materialModifier.invoke(mat)
        return (material?.alphaMode == "BLEND") to mat
    }

    private fun createMeshDeclaration(meshIndex: Int, primitiveIndex: Int, skinIndex: Int?): InternalMeshDeclaration {

        val cpuMesh = cache.loadedMeshes[meshIndex to primitiveIndex]!!

        val meshDeclaration = CustomCpuMesh(
            "${declaration.resource}:$meshIndex:$primitiveIndex",
            cpuMesh,
            declaration.nodeContext
        )

        if (declaration.instancingDeclaration == null)
            return meshDeclaration

        return InstancedMesh(declaration.resource, declaration.instancingDeclaration.count, meshDeclaration, !declaration.instancingDeclaration.dynamic, false, declaration.nodeContext, listOf(InternalInstancingParameter.TRANSFORM_INSTANCING)) {
            declaration.instancingDeclaration.instancer().mapIndexed { i, it ->
                MeshInstance(it.transform, skinIndex?.let {
                    instanceData[i].jointMatrices[skinIndex].mapIndexed { ind, jm -> jm * cache.loadedSkins[skinIndex]!![ind] }
                }, null, null, null, null)
            }
        }
    }

    // TODO: redesign; uri is to big for key
    private fun getBufferBytes(buffer: InternalGltfFileModel.Buffer): ByteArray {
        return cache.loadedUris[buffer.uri ?: ""]!!
    }

    private fun getBufferViewBytes(bufferView: InternalGltfFileModel.BufferView): ByteArray {
        val buffer = cache.model.buffers!![bufferView.buffer]
        val bufferBytes = getBufferBytes(buffer)
        return bufferBytes.copyOfRange(
            bufferView.byteOffset,
            bufferView.byteOffset + bufferView.byteLength
        )
    }

    private fun getTexture(ti: GltfFileModel.TextureIndexProvider): TextureDeclaration? {
        val image = cache.model.textures?.get(ti.index)?.source
            ?.let { src -> cache.model.images!![src] }

        return image?.let { img ->
            InternalByteArrayTextureDeclaration(
                img.uri ?: "${declaration.resource} ${ti.index}", // TODO !!!
                TextureFilter.MipMap,
                TextureWrap.Repeat,
                1024,
                { getImageBytes(img) },
                img.mimeType?.split("/")?.last() ?: img.uri?.split(".")?.last() ?: "unknown",
                declaration.nodeContext
            )
        }
    }

    private fun getImageBytes(image: InternalGltfFileModel.Image): ByteArray {
        if (image.uri != null)
            return cache.loadedUris[image.uri]!!
        if (image.bufferView != null) {
            val bufferView = cache.model.bufferViews!![image.bufferView]
            return getBufferViewBytes(bufferView)
        }
        throw KorenderException("GLTF: image without uri or bufferView")
    }
}

private fun InternalGltfFileModel.Camera.toProjection(): ProjectionDeclaration =
    when (type) {
        "perspective" -> {
            val near = perspective!!.znear
            val aspect = perspective.aspectRatio ?: 1f // TODO: viewport aspect
            val top = near * tan(perspective.yfov * 0.5f)
            val height = 2f * top
            val width = height * aspect
            Projection(width, height, near, perspective.zfar ?: 10000f, FrustumProjectionMode) // TODO: logmode ??
        }

        "orthographic" -> {
            Projection(orthographic!!.xmag * 2f, orthographic.ymag * 2f, orthographic.znear, orthographic.zfar, OrthoProjectionMode) // TODO: logmode ??
        }

        else -> throw KorenderException("Unknown Gltf camera type")
    }
