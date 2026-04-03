package com.zakgof.korender.impl.context

import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.material.ResourceTextureDeclaration
import com.zakgof.korender.math.Transform

internal class NodeContext(
    val resourceLoader: ResourceLoader,
    val transform: Transform,
    override var retentionPolicy: RetentionPolicy,
) : NodeScope {

    override fun texture(textureResource: String, filter: TextureFilter, wrap: TextureWrap, aniso: Int) =
        ResourceTextureDeclaration(textureResource, filter, wrap, aniso, retentionPolicy)
}

internal interface NodeScope {
    var retentionPolicy: RetentionPolicy
    fun texture(textureResource: String, filter: TextureFilter, wrap: TextureWrap, aniso: Int): TextureDeclaration
}