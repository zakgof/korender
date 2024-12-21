package com.zakgof.korender.image


import com.zakgof.korender.getPlatform
import com.zakgof.korender.impl.ResourceLoader
import com.zakgof.korender.impl.resourceBytes
import kotlinx.coroutines.Deferred

object Images {
    suspend fun image(appResourceLoader: ResourceLoader, resource: String): Deferred<Image> =
        getPlatform().loadImage(resourceBytes(appResourceLoader, resource), resource.split(".").last())
}