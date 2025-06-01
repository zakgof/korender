import com.zakgof.korender.Attributes
import com.zakgof.korender.IndexType
import com.zakgof.korender.KorenderException
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.engine.BaseMaterial
import com.zakgof.korender.impl.engine.GltfDeclaration
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.geometry.CustomMesh
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.InternalMeshDeclaration
import com.zakgof.korender.impl.gl.GLConstants
import com.zakgof.korender.impl.glgpu.Mat4List
import com.zakgof.korender.impl.glgpu.toGL
import com.zakgof.korender.impl.gltf.Gltf
import com.zakgof.korender.impl.gltf.GltfLoaded
import com.zakgof.korender.impl.gltf.asNativeFloatList
import com.zakgof.korender.impl.gltf.elementComponentSize
import com.zakgof.korender.impl.material.ByteArrayTextureDeclaration
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import kotlin.math.floor

internal class InstanceData {
    val nodeMatrices = mutableMapOf<Int, Transform>()
    val nodeAnimations = mutableMapOf<Int, MutableMap<String, List<Float>>>()
    val jointMatrices = mutableListOf<List<Mat4>>()
}

internal class GltfSceneBuilder(
    private val declaration: GltfDeclaration,
    private val gltfLoaded: GltfLoaded,
    private val instanceData: Array<InstanceData> = Array(declaration.instances.size) { InstanceData() },
) {

    private val meshNodes = mutableListOf<Pair<Int, Int>>()

    fun build(): List<RenderableDeclaration> {
        val model = gltfLoaded.model
        val scene = model.scenes!![model.scene]

        scene.nodes.forEach { collectMeshesFromNode(it) }

        instanceData.forEachIndexed { index, instanceData ->

            val instanceDeclaration = declaration.instances[index]

            if (instanceDeclaration.animation < (model.animations?.size ?: 0)) {
                val animation = model.animations!![instanceDeclaration.animation]
                val samplerValues = animation.samplers.map { sampler -> getSamplerValue(sampler, instanceDeclaration.time) }
                animation.channels.forEach { channel ->
                    instanceData.nodeAnimations.getOrPut(channel.target.node!!) {
                        mutableMapOf()
                    }[channel.target.path] = samplerValues[channel.sampler]
                }
            }

            scene.nodes.forEach { nodeIndex ->
                processNode(
                    instanceData,
                    instanceDeclaration.transform,
                    nodeIndex,
                    model.nodes!![nodeIndex]
                )
            }
            instanceData.jointMatrices += model.skins?.map { skin ->
                skin.joints.map { instanceData.nodeMatrices[it]!!.mat4 }
            } ?: listOf() // TODO optimize
        }
        return meshNodes.flatMap {
            processMesh(
                gltfLoaded.model.meshes!![it.first],
                it.first,
                gltfLoaded.model.nodes!![it.second].skin,
            )
        }
    }

    private fun getSamplerValue(sampler: Gltf.Animation.AnimationSampler, currentTime: Float): List<Float> {

        val inputBytes = gltfLoaded.loadedAccessors[sampler.input]!!
        val inputFloats = inputBytes.asNativeFloatList()

        val outputBytes = gltfLoaded.loadedAccessors[sampler.output]!!
        val outputFloats = outputBytes.asNativeFloatList()
        // TODO validate float input and output
        val outputValues = getAccessorFloatBasedElements(outputFloats, gltfLoaded.model.accessors!![sampler.output].type)
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

    private fun getAccessorFloatBasedElements(raw: List<Float>, type: String): List<List<Float>> =
        when (type) {
            "VEC4" -> List(raw.size / 4) { List(4) { i -> raw[i + it * 4] } }
            "VEC3" -> List(raw.size / 3) { List(3) { i -> raw[i + it * 3] } }
            else -> throw KorenderException("GLTF: Unknown accessor element type for sampler: $type")
        }

    private fun collectMeshesFromNode(nodeIndex: Int) {
        val node = gltfLoaded.model.nodes!![nodeIndex]
        node.mesh?.let { meshNodes.add(it to nodeIndex) }
        node.children?.forEach { childNodeIndex ->
            collectMeshesFromNode(childNodeIndex)
        }
    }

    private fun processNode(instanceData: InstanceData, parentTransform: Transform, nodeIndex: Int, node: Gltf.Node) {

        var transform = parentTransform

        val na = instanceData.nodeAnimations[nodeIndex]

        val translation = na?.get("translation") ?: node.translation
        val rotation = na?.get("rotation") ?: node.rotation
        val scale = na?.get("scale") ?: node.scale

        translation?.let { transform *= Transform.translate(Vec3(it[0], it[1], it[2])) }
        rotation?.let {
            transform *= Transform.rotate(Quaternion(it[3], Vec3(it[0], it[1], it[2])))
        }
        scale?.let { transform *= Transform.scale(it[0], it[1], it[2]) }

        node.matrix?.let { transform *= Transform(Mat4(it.toFloatArray())) }
        instanceData.nodeMatrices[nodeIndex] = transform

        node.children?.forEach { childNodeIndex ->
            processNode(instanceData, transform, childNodeIndex, gltfLoaded.model.nodes!![childNodeIndex])
        }
    }

    private fun processMesh(
        mesh: Gltf.Mesh,
        meshIndex: Int,
        skinIndex: Int?
    ): List<RenderableDeclaration> =
        mesh.primitives.mapIndexed { primitiveIndex, primitive ->
            val meshDeclaration = createMeshDeclaration(primitive, meshIndex, primitiveIndex)
            val jointMatrices = skinIndex?.let {
                if (declaration.instanceCount == null) instanceData[0].jointMatrices[it] else null
            }
            val materialModifier = createMaterialModifiers(primitive, skinIndex, jointMatrices)
            RenderableDeclaration(
                BaseMaterial.Renderable,
                listOf(materialModifier),
                mesh = meshDeclaration,
                transform = if (declaration.instanceCount == null) declaration.instances[0].transform else Transform(),
                declaration.retentionPolicy
            )
        }


    private fun createMaterialModifiers(
        primitive: Gltf.Mesh.Primitive,
        skinIndex: Int?,
        jointMatrices: List<Mat4>?
    ): MaterialModifier {

        // TODO: split into 2 parts, precompute textures modifier, calc only skin modifier
        val material = primitive.material?.let { gltfLoaded.model.materials!![it] }
        val matPbr = material?.pbrMetallicRoughness
        val matSpecularGlossiness = material?.extensions?.get("KHR_materials_pbrSpecularGlossiness")
                as? Gltf.KHRMaterialsPbrSpecularGlossiness

        // TODO: Precreate all except jointMatrices
        return InternalMaterialModifier { mb ->

            if (jointMatrices != null) {
                mb.plugins["vposition"] = "!shader/plugin/vposition.skinning.vert"
                mb.plugins["vnormal"] = "!shader/plugin/vnormal.skinning.vert"
                val jointMatrixList = jointMatrices.mapIndexed { ind, jm ->
                    jm * gltfLoaded.loadedSkins[skinIndex]!![ind]
                }
                mb.uniforms["jntMatrices[0]"] = Mat4List(jointMatrixList)
            }

            mb.uniforms["baseColor"] = (matSpecularGlossiness?.diffuseFactor ?: matPbr?.baseColorFactor)?.let {
                ColorRGBA(it[0], it[1], it[2], it[3])
            } ?: ColorRGBA.White

            (matPbr?.baseColorTexture ?: matSpecularGlossiness?.diffuseTexture)?.let { getTexture(it) }?.let {
                mb.uniforms["baseColorTexture"] = it
                mb.shaderDefs += "BASE_COLOR_MAP";
            }

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
    }

    private fun createMeshDeclaration(primitive: Gltf.Mesh.Primitive, meshIndex: Int, primitiveIndex: Int): InternalMeshDeclaration {
        // TODO: optimize accessor stuff
        val indicesAccessor = primitive.indices?.let { gltfLoaded.model.accessors!![it] }
        val verticesAttributeAccessors = primitive.attributes
            .mapNotNull { p ->
                val accessor = gltfLoaded.model.accessors!![p.value]
                attributeForAccessor(p.key, accessor)?.let { it to p.value }
            }

        val meshDeclaration = CustomMesh(
            "${declaration.gltfResource}:$meshIndex:$primitiveIndex",
            gltfLoaded.model.accessors!![verticesAttributeAccessors.first().second].count,
            indicesAccessor?.count ?: 0,
            verticesAttributeAccessors.map { it.first },
            false,
            accessorComponentTypeToIndexType(indicesAccessor?.componentType),
            declaration.retentionPolicy
        ) {
            indicesAccessor?.let { indexBytes(gltfLoaded.loadedAccessors[primitive.indices]!!) }
            verticesAttributeAccessors.forEach {
                attrBytes(it.first, gltfLoaded.loadedAccessors[it.second]!!)
            }
        }

        if (declaration.instanceCount == null)
            return meshDeclaration

        // TODO dynamic/static
        return InstancedMesh(declaration.gltfResource, declaration.instanceCount, meshDeclaration, false, false, declaration.retentionPolicy) {
            // TODO rewrite without blocks in internal class
            declaration.instances.forEach {
                Instance(it.transform)
            }
        }
    }

    private fun attributeForAccessor(key: String, accessor: Gltf.Accessor): MeshAttribute<*>? {
        val candidates = when (key) {
            "POSITION" -> listOf(Attributes.POS)
            "NORMAL" -> listOf(Attributes.NORMAL)
            "TEXCOORD_0" -> listOf(Attributes.TEX)
            "JOINTS_0" -> listOf(Attributes.JOINTS_BYTE, Attributes.JOINTS_SHORT, Attributes.JOINTS_INT)
            "WEIGHTS_0" -> listOf(Attributes.WEIGHTS)
            else -> null
        }
        return candidates?.firstOrNull {
            it.structSize == accessor.elementComponentSize()
                    && it.primitiveType.toGL() == accessor.componentType
        }
    }

    private fun accessorComponentTypeToIndexType(componentType: Int?) =
        when (componentType) {
            null -> null
            GLConstants.GL_UNSIGNED_BYTE -> IndexType.Byte
            GLConstants.GL_UNSIGNED_SHORT -> IndexType.Short
            GLConstants.GL_UNSIGNED_INT -> IndexType.Int
            else -> throw KorenderException("GLTF: Unsupported componentType for index: $componentType")
        }

    // TODO: redesign; uri is to big for key
    private fun getBufferBytes(buffer: Gltf.Buffer): ByteArray {
        return gltfLoaded.loadedUris[buffer.uri ?: ""]!!
    }

    private fun getBufferViewBytes(bufferView: Gltf.BufferView): ByteArray {
        val buffer = gltfLoaded.model.buffers!![bufferView.buffer]
        val bufferBytes = getBufferBytes(buffer)
        return bufferBytes.copyOfRange(
            bufferView.byteOffset,
            bufferView.byteOffset + bufferView.byteLength
        )
    }

    private fun getTexture(ti: Gltf.TextureIndexProvider): TextureDeclaration? {
        val image = gltfLoaded.model.textures?.get(ti.index)?.source
            ?.let { src -> gltfLoaded.model.images!![src] }

        return image?.let { img ->
            ByteArrayTextureDeclaration(
                img.uri ?: "${gltfLoaded.id} ${ti.index}", // TODO !!!
                TextureFilter.MipMap,
                TextureWrap.Repeat,
                1024,
                { getImageBytes(img) },
                img.mimeType!!.split("/").last(),
                declaration.retentionPolicy
            )
        }
    }

    private fun getImageBytes(image: Gltf.Image): ByteArray {
        if (image.uri != null)
            return gltfLoaded.loadedUris[image.uri]!!
        if (image.bufferView != null) {
            val bufferView = gltfLoaded.model.bufferViews!![image.bufferView]
            return getBufferViewBytes(bufferView)
        }
        throw KorenderException("GLTF: image without uri or bufferView")
    }
}