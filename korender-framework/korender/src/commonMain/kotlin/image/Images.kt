package com.zakgof.korender.image


import com.zakgof.korender.Platform
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.resourceBytes
import kotlinx.coroutines.Deferred

object Images {
    suspend fun image(appResourceLoader: ResourceLoader, resource: String): Deferred<Image> =
        Platform.loadImage(resourceBytes(appResourceLoader, resource), resource.split(".").last())
}