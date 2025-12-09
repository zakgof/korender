package com.zakgof.korender.impl.gltf

import com.zakgof.korender.KorenderException
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.gltf.GltfModel
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.engine.BaseMaterial
import com.zakgof.korender.impl.engine.GltfDeclaration
import com.zakgof.korender.impl.engine.GltfInstance
import com.zakgof.korender.impl.engine.MeshInstance
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.geometry.CustomCpuMesh
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.InternalMeshDeclaration
import com.zakgof.korender.impl.glgpu.Mat4List
import com.zakgof.korender.impl.material.ByteArrayTextureDeclaration
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.projection.FrustumProjectionMode
import com.zakgof.korender.impl.projection.OrthoProjectionMode
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import kotlin.math.floor
import kotlin.math.tan

internal class InstanceData(nodes: Int) {
    val nodeMatrices = Array(nodes) { Transform() }
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

internal class GltfSceneBuilder(
    private val declaration: GltfDeclaration,
    private val cache: GltfCache,
) {
    private val cameraTransforms = MutableList(cache.model.cameras?.size ?: 0) { Transform() }
    private val meshNodes = mutableListOf<Pair<Int, Int>>()
    private val instances = declaration.instancingDeclaration?.instancer?.invoke() ?: listOf(GltfInstance(Transform.IDENTITY, declaration.time, declaration.animation))
    private val instanceData: Array<InstanceData> = Array(instances.size) { InstanceData(cache.model.nodes?.size ?: 0) }

    fun build(): List<RenderableDeclaration> {
        val model = cache.model
        val scene = model.scenes!![model.scene]

        scene.nodes.forEach { collectMeshesFromNode(it) }

        val instancesUpdateDate = instanceData.mapIndexed { index, instanceData ->
            calculateInstanceData(index, instanceData)
            val nodeUpdateData = scene.nodes.map { nodeIndex ->
                processNode(instanceData, Transform.IDENTITY, nodeIndex, model.nodes!![nodeIndex])
            }
            instanceData.jointMatrices += model.skins?.map { skin ->
                skin.joints.map { instanceData.nodeMatrices[it].mat4 }
            } ?: listOf() // TODO optimize
            InternalUpdateData.Instance(InternalUpdateData.Node(Transform.IDENTITY, null, nodeUpdateData))
        }
        val renderables = meshNodes.flatMap {
            createRenderables(it.first,cache.model.nodes!![it.second].skin, it.second)
        }
        declaration.onUpdate(InternalUpdateData(cache.model.cameras?.mapIndexed { index, cam ->
            InternalUpdateData.InternalGltfCamera(
                cam.name,
                DefaultCamera(cameraTransforms[index].mat4),
                cam.toProjection()
            )
        } ?: listOf(), listOf(), instancesUpdateDate))
        return renderables
    }

    private fun calculateInstanceData(instanceIndex: Int, instanceData: InstanceData) {
        val instanceDeclaration = instances[instanceIndex]
        val animationIndex = instanceDeclaration.animation ?: declaration.animation
        if (animationIndex < (cache.model.animations?.size ?: 0)) {
            val animation = cache.model.animations!![animationIndex]
            animation.channels.forEach { channel ->
                val samplerValue = getSamplerValue(animation.samplers[channel.sampler], instanceDeclaration.time ?: declaration.time)
                instanceData.nodeAnimations[channel.target.node!!].populate(channel.target.path, samplerValue)
            }
        }
    }

    private fun getSamplerValue(sampler: InternalGltfModel.Animation.AnimationSampler, currentTime: Float): List<Float> {

        // TODO validate float input and output
        val inputFloats = cache.loadedAccessors.floats[sampler.input]!!
        val outputValues = cache.loadedAccessors.floatArrays[sampler.output]!!

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

    private fun processNode(instanceData: InstanceData, parentTransform: Transform, nodeIndex: Int, node: InternalGltfModel.Node): InternalUpdateData.Node {

        var transform = parentTransform

        val na = instanceData.nodeAnimations[nodeIndex]

        val translation = na.translation ?: node.translation
        val rotation = na.rotation ?: node.rotation
        val scale = na.scale ?: node.scale

        translation?.let { transform *= translate(Vec3(it[0], it[1], it[2])) }
        rotation?.let { transform *= rotate(Quaternion(it[3], Vec3(it[0], it[1], it[2]))) }
        scale?.let { transform *= scale(it[0], it[1], it[2]) }
        node.matrix?.let { transform *= Transform(Mat4(it.toFloatArray())) }

        node.camera?.let {
            cameraTransforms[it] = transform
        }

        instanceData.nodeMatrices[nodeIndex] = transform

        val children = node.children?.map { childNodeIndex ->
            processNode(instanceData, transform, childNodeIndex, cache.model.nodes!![childNodeIndex])
        } ?: listOf()

        val meshData: InternalUpdateData.Mesh? = node.mesh?.let { meshIndex ->
            val primitivesData = cache.model.meshes!![meshIndex].primitives.indices.map { primitiveIndex ->
                cache.loadedMeshes[meshIndex to primitiveIndex]!!
            }
            InternalUpdateData.Mesh(primitivesData)
        }

        return InternalUpdateData.Node(transform, meshData, children)
    }

    private fun createRenderables(meshIndex: Int, skinIndex: Int?, nodeIndex: Int): List<RenderableDeclaration> =
        cache.model.meshes!![meshIndex].primitives.mapIndexed { primitiveIndex, primitive ->
            val meshDeclaration = createMeshDeclaration(meshIndex, primitiveIndex, skinIndex)
            val jointMatrices = skinIndex?.let {
                if (declaration.instancingDeclaration == null) instanceData[0].jointMatrices[it] else null
            }
            val materialModifier = createMaterialModifiers(primitive, skinIndex, jointMatrices)

            // TODO why this works
            val meshTransform = if (skinIndex == null) instanceData[0].nodeMatrices[nodeIndex].mat4 else Mat4.IDENTITY
            val transform = declaration.transform * Transform(meshTransform)
            RenderableDeclaration(
                BaseMaterial.Renderable,
                listOf(materialModifier.second),
                mesh = meshDeclaration,
                transform = transform,
                materialModifier.first,
                declaration.retentionPolicy,
            )
        }

    private fun createMaterialModifiers(
        primitive: InternalGltfModel.Mesh.Primitive,
        skinIndex: Int?,
        jointMatrices: List<Mat4>?,
    ): Pair<Boolean, MaterialModifier> {

        // TODO: split into 2 parts, precompute textures modifier, calc only skin modifier
        val material = primitive.material?.let { cache.model.materials!![it] }
        val matPbr = material?.pbrMetallicRoughness
        val matSpecularGlossiness = material?.extensions?.get("KHR_materials_pbrSpecularGlossiness")
                as? InternalGltfModel.KHRMaterialsPbrSpecularGlossiness

        // TODO: Precreate all except jointMatrices
        val imm = InternalMaterialModifier { mb ->

            if (skinIndex != null) {
                mb.plugins["vposition"] = "!shader/plugin/vposition.skinning.vert"
                mb.plugins["vnormal"] = "!shader/plugin/vnormal.skinning.vert"
                if (declaration.instancingDeclaration == null && jointMatrices != null) {
                    val jointMatrixList = jointMatrices.mapIndexed { ind, jm ->
                        jm * cache.loadedSkins[skinIndex]!![ind]
                    }
                    mb.uniforms["jntMatrices[0]"] = Mat4List(jointMatrixList)
                }
            }

            mb.uniforms["baseColor"] = (matSpecularGlossiness?.diffuseFactor ?: matPbr?.baseColorFactor)?.let {
                ColorRGBA(it[0], it[1], it[2], it[3])
            } ?: ColorRGBA.White

            (matPbr?.baseColorTexture ?: matSpecularGlossiness?.diffuseTexture)?.let { getTexture(it) }?.let {
                mb.uniforms["baseColorTexture"] = it
                mb.shaderDefs += "BASE_COLOR_MAP";
            }
            val alphaCutoff = if (material?.alphaMode == "MASK") material.alphaCutoff else 0.001f
            mb.uniforms["alphaCutoff"] = alphaCutoff

            mb.uniforms["metallicFactor"] = matPbr?.metallicFactor ?: 0.1f
            mb.uniforms["roughnessFactor"] = matPbr?.roughnessFactor ?: 0.5f

            material?.normalTexture?.let { getTexture(it) }?.let {
                mb.plugins["normal"] = "!shader/plugin/normal.texture.frag"
                mb.uniforms["normalTexture"] = it
            }

            matPbr?.metallicRoughnessTexture?.let { getTexture(it) }?.let {
                mb.plugins["metallic_roughness"] = "!shader/plugin/metallic_roughness.texture.frag"
                mb.uniforms["metallicRoughnessTexture"] = it
            }

            // TODO
            val occlusionTexture = material?.occlusionTexture?.let { getTexture(it) }
            val emissiveTexture = material?.emissiveTexture?.let { getTexture(it) }

            matSpecularGlossiness?.let { sg ->
                mb.plugins["specular_glossiness"] = "!shader/plugin/specular_glossiness.factor.frag"
                mb.uniforms["specularFactor"] = sg.specularFactor.let { ColorRGB(it[0], it[1], it[2]) }
                mb.uniforms["glossinessFactor"] = sg.glossinessFactor
            }
        }
        return (material?.alphaMode == "BLEND") to imm
    }

    private fun createMeshDeclaration(meshIndex: Int, primitiveIndex: Int, skinIndex: Int?): InternalMeshDeclaration {

        val cpuMesh = cache.loadedMeshes[meshIndex to primitiveIndex]!!

        val meshDeclaration = CustomCpuMesh(
            "${declaration.resource}:$meshIndex:$primitiveIndex",
            cpuMesh,
            declaration.retentionPolicy
        )

        if (declaration.instancingDeclaration == null)
            return meshDeclaration

        return InstancedMesh(declaration.resource, declaration.instancingDeclaration.count, meshDeclaration, !declaration.instancingDeclaration.dynamic, false, declaration.retentionPolicy) {
            declaration.instancingDeclaration.instancer().mapIndexed { i, it ->
                MeshInstance(it.transform, skinIndex?.let {
                    instanceData[i].jointMatrices[skinIndex].mapIndexed { ind, jm -> jm * cache.loadedSkins[skinIndex]!![ind] }
                })
            }
        }
    }

    // TODO: redesign; uri is to big for key
    private fun getBufferBytes(buffer: InternalGltfModel.Buffer): ByteArray {
        return cache.loadedUris[buffer.uri ?: ""]!!
    }

    private fun getBufferViewBytes(bufferView: InternalGltfModel.BufferView): ByteArray {
        val buffer = cache.model.buffers!![bufferView.buffer]
        val bufferBytes = getBufferBytes(buffer)
        return bufferBytes.copyOfRange(
            bufferView.byteOffset,
            bufferView.byteOffset + bufferView.byteLength
        )
    }

    private fun getTexture(ti: GltfModel.TextureIndexProvider): TextureDeclaration? {
        val image = cache.model.textures?.get(ti.index)?.source
            ?.let { src -> cache.model.images!![src] }

        return image?.let { img ->
            ByteArrayTextureDeclaration(
                img.uri ?: "${cache.id} ${ti.index}", // TODO !!!
                TextureFilter.MipMap,
                TextureWrap.Repeat,
                1024,
                { getImageBytes(img) },
                img.mimeType!!.split("/").last(),
                declaration.retentionPolicy
            )
        }
    }

    private fun getImageBytes(image: InternalGltfModel.Image): ByteArray {
        if (image.uri != null)
            return cache.loadedUris[image.uri]!!
        if (image.bufferView != null) {
            val bufferView = cache.model.bufferViews!![image.bufferView]
            return getBufferViewBytes(bufferView)
        }
        throw KorenderException("GLTF: image without uri or bufferView")
    }
}

private fun InternalGltfModel.Camera.toProjection(): ProjectionDeclaration =
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
