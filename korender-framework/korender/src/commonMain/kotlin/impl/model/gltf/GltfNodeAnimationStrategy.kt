package com.zakgof.korender.impl.model.gltf

import com.zakgof.korender.AnimationDeclaration
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3

internal interface InternalAnimationDeclaration : AnimationDeclaration {
    fun strategy(nodeCount: Int): GltfNodeAnimationStrategy
}

internal class SingleAnimationDeclaration(val animationIndex: Int) : InternalAnimationDeclaration {
    override fun strategy(nodeCount: Int) = SingleAnimationStrategy(animationIndex, nodeCount)
}

internal class BlendedAnimationDeclaration(val animationIndex1: Int, val animationIndex2: Int, val weight: Float) : InternalAnimationDeclaration {
    override fun strategy(nodeCount: Int) = BlendedAnimationStrategy(animationIndex1, animationIndex2, weight, nodeCount)
}

internal interface GltfNodeAnimationStrategy {
    fun populate(cache: GltfCache, time: Float)
    fun transform(nodeIndex: Int, node: InternalGltfFileModel.Node): Transform
}

internal class SingleAnimationStrategy(val animationIndex: Int, nodeCount: Int) : GltfNodeAnimationStrategy {

    var nodeAnimations: Array<NodeAnimation> = Array(nodeCount) { NodeAnimation(null, null, null) }

    override fun populate(cache: GltfCache, time: Float) {
        val animation = cache.model.animations!![animationIndex]
        animation.channels.forEach { channel ->
            channel.target.node?.let {
                val samplerValue = cache.getSamplerValue(animation.samplers[channel.sampler], time)
                nodeAnimations[channel.target.node].populate(channel.target.path, samplerValue)
            }
        }
    }

    override fun transform(nodeIndex: Int, node: InternalGltfFileModel.Node): Transform {

        val na = nodeAnimations[nodeIndex]

        var localTransform = Transform.IDENTITY

        val translation = na.translation ?: node.translation
        val rotation = na.rotation ?: node.rotation
        val scale = na.scale ?: node.scale

        scale?.let { localTransform = localTransform.scale(it.vec3(Vec3(1f, 1f, 1f))) }
        rotation?.let { localTransform = localTransform.rotate(it.quat()) }
        translation?.let { localTransform = localTransform.translate(it.vec3(Vec3.ZERO)) }
        node.matrix?.let { localTransform *= Transform(Mat4(it.toFloatArray())) }

        return localTransform
    }
}

internal class BlendedAnimationStrategy(val animationIndex1: Int, val animationIndex2: Int, val weight: Float, nodeCount: Int) : GltfNodeAnimationStrategy {

    var nodeAnimations1: Array<NodeAnimation> = Array(nodeCount) { NodeAnimation(null, null, null) }
    var nodeAnimations2: Array<NodeAnimation> = Array(nodeCount) { NodeAnimation(null, null, null) }

    override fun populate(cache: GltfCache, time: Float) {
        val animation1 = cache.model.animations!![animationIndex1]
        animation1.channels.forEach { channel1 ->
            channel1.target.node?.let {
                val samplerValue = cache.getSamplerValue(animation1.samplers[channel1.sampler], time)
                nodeAnimations1[channel1.target.node].populate(channel1.target.path, samplerValue)
            }
        }
        val animation2 = cache.model.animations[animationIndex2]
        animation2.channels.forEach { channel2 ->
            channel2.target.node?.let {
                val samplerValue = cache.getSamplerValue(animation2.samplers[channel2.sampler], time)
                nodeAnimations2[channel2.target.node].populate(channel2.target.path, samplerValue)
            }
        }
    }

    override fun transform(nodeIndex: Int, node: InternalGltfFileModel.Node): Transform {

        val na1 = nodeAnimations1[nodeIndex]
        val na2 = nodeAnimations2[nodeIndex]

        val scale1 = (na1.scale ?: node.scale).vec3(Vec3(1f, 1f, 1f))
        val scale2 = (na2.scale ?: node.scale).vec3(Vec3(1f, 1f, 1f))
        val scale = scale1 + (scale2 - scale1) * weight

        val rotation1 = (na1.rotation ?: node.rotation).quat()
        val rotation2 = (na2.rotation ?: node.rotation).quat()
        val rotation = Quaternion.slerp(rotation1, rotation2, weight)

        val translation1 = (na1.translation ?: node.translation).vec3(Vec3.ZERO)
        val translation2 = (na2.translation ?: node.translation).vec3(Vec3.ZERO)
        val translation = translation1 + (translation2 - translation1) * weight

        var localTransform = scale(scale).rotate(rotation).translate(translation)
        node.matrix?.let { localTransform *= Transform(Mat4(it.toFloatArray())) }
        return localTransform
    }
}

private fun List<Float>?.vec3(dflt: Vec3) = if (this == null) dflt else Vec3(this[0], this[1], this[2])
private fun List<Float>?.quat() = if (this == null) Quaternion.IDENTITY else Quaternion(this[3], Vec3(this[0], this[1], this[2]))
